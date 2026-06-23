package com.healthcard.backend.service;

import com.healthcard.backend.entity.*;
import com.healthcard.backend.entity.enums.AuditAction;
import com.healthcard.backend.entity.enums.HealthCardStatus;
import com.healthcard.backend.exception.AccountNotVerifiedException;
import com.healthcard.backend.exception.ResourceNotFoundException;
import com.healthcard.backend.exception.UnauthorizedAccessException;
import com.healthcard.backend.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * THE GATEKEEPER.
 *
 * This is the single place in the codebase that decides whether a doctor (or
 * pathologist) is allowed to touch a patient's record. Every other service
 * that needs a Patient by health card credentials must go through here so the
 * rule is enforced uniformly and every attempt is audited.
 *
 * Access is granted only when ALL of the following hold:
 *   1. The requesting doctor's account has been verified by an admin.
 *   2. The supplied healthCardNumber AND healthCardId both match the same card.
 *   3. The card status is ACTIVE (not blocked/expired).
 *
 * Every successful AND failed attempt is written to the audit log so the
 * patient can see exactly who has looked at their record.
 */
@Service
@RequiredArgsConstructor
public class PatientAccessService {

    private final HealthCardRepository healthCardRepository;
    private final DoctorRepository doctorRepository;
    private final AuditLogRepository auditLogRepository;

    @Transactional
    public Patient resolvePatientForDoctor(Long doctorUserId, String healthCardNumber, String healthCardId, HttpServletRequest httpRequest) {
        Doctor doctor = doctorRepository.findByUserId(doctorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));

        if (!doctor.isVerified()) {
            throw new AccountNotVerifiedException(
                    "Your doctor account has not yet been verified by an administrator. " +
                    "You cannot access patient records until verification is complete.");
        }

        HealthCard card = healthCardRepository.findByHealthCardNumber(healthCardNumber).orElse(null);

        if (card == null || card.getHealthCardId() == null || !card.getHealthCardId().equalsIgnoreCase(healthCardId)) {
            logAttempt(doctor.getUser(), card != null ? card.getPatient() : null, AuditAction.PATIENT_RECORD_ACCESS_DENIED,
                    "Invalid health card number/ID combination", httpRequest);
            throw new UnauthorizedAccessException(
                    "Health card number and health card ID do not match. Access denied and logged.");
        }

        if (card.getStatus() != HealthCardStatus.ACTIVE) {
            logAttempt(doctor.getUser(), card.getPatient(), AuditAction.PATIENT_RECORD_ACCESS_DENIED,
                    "Card status is " + card.getStatus(), httpRequest);
            throw new UnauthorizedAccessException("This health card is " + card.getStatus().name().toLowerCase() + " and cannot be used.");
        }

        logAttempt(doctor.getUser(), card.getPatient(), AuditAction.PATIENT_RECORD_ACCESSED,
                "Accessed via health card number + ID verification", httpRequest);

        return card.getPatient();
    }

    private void logAttempt(User actor, Patient targetPatient, AuditAction action, String details, HttpServletRequest httpRequest) {
        auditLogRepository.save(AuditLog.builder()
                .actor(actor)
                .targetPatient(targetPatient)
                .action(action)
                .details(details)
                .ipAddress(httpRequest != null ? httpRequest.getRemoteAddr() : null)
                .build());
    }
}
