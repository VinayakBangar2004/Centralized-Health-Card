package com.healthcard.backend.service;

import com.healthcard.backend.dto.response.AuthorizationResponse;
import com.healthcard.backend.entity.AuditLog;
import com.healthcard.backend.entity.Doctor;
import com.healthcard.backend.entity.DoctorPatientAuthorization;
import com.healthcard.backend.entity.Patient;
import com.healthcard.backend.entity.enums.AuditAction;
import com.healthcard.backend.exception.ResourceNotFoundException;
import com.healthcard.backend.repository.AuditLogRepository;
import com.healthcard.backend.repository.DoctorPatientAuthorizationRepository;
import com.healthcard.backend.repository.DoctorRepository;
import com.healthcard.backend.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Lets a patient grant a doctor standing consent, so that doctor doesn't need
 * the patient to recite the secret health card ID on every single visit.
 * Purely additive convenience layer on top of PatientAccessService's hard gate -
 * it never replaces the health-card-number+ID check for *first* contact.
 *
 * Every method here takes plain IDs and loads its own entities inside its own
 * transaction, rather than accepting pre-fetched Patient/Doctor objects from
 * the controller - a detached entity handed in from outside this transaction
 * would throw LazyInitializationException the moment a lazy field on it
 * (like patient.getUser()) is accessed.
 */
@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final DoctorPatientAuthorizationRepository authorizationRepository;
    private final AuditLogRepository auditLogRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    @Transactional
    public AuthorizationResponse authorizeDoctor(Long patientUserId, Long doctorId) {
        Patient patient = patientRepository.findByUserId(patientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        DoctorPatientAuthorization auth = authorizationRepository
                .findByDoctorIdAndPatientIdAndActiveTrue(doctor.getId(), patient.getId())
                .orElseGet(() -> DoctorPatientAuthorization.builder()
                        .doctor(doctor)
                        .patient(patient)
                        .build());
        auth.setActive(true);
        auth.setAuthorizedAt(LocalDateTime.now());
        auth.setRevokedAt(null);
        authorizationRepository.save(auth);

        auditLogRepository.save(AuditLog.builder()
                .actor(patient.getUser())
                .targetPatient(patient)
                .action(AuditAction.DOCTOR_AUTHORIZED_BY_PATIENT)
                .details("Patient granted standing access to Dr. " + doctor.getUser().getFullName())
                .build());

        return toResponse(auth);
    }

    @Transactional
    public void revoke(Long patientUserId, Long doctorId) {
        Patient patient = patientRepository.findByUserId(patientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));

        authorizationRepository.findByDoctorIdAndPatientIdAndActiveTrue(doctorId, patient.getId())
                .ifPresent(auth -> {
                    auth.setActive(false);
                    auth.setRevokedAt(LocalDateTime.now());
                    authorizationRepository.save(auth);

                    auditLogRepository.save(AuditLog.builder()
                            .actor(patient.getUser())
                            .targetPatient(patient)
                            .action(AuditAction.DOCTOR_AUTHORIZATION_REVOKED)
                            .details("Patient revoked access for Dr. " + auth.getDoctor().getUser().getFullName())
                            .build());
                });
    }

    @Transactional(readOnly = true)
    public boolean isAuthorized(Long doctorId, Long patientId) {
        return authorizationRepository.findByDoctorIdAndPatientIdAndActiveTrue(doctorId, patientId).isPresent();
    }

    @Transactional(readOnly = true)
    public List<AuthorizationResponse> listForPatient(Long patientId) {
        return authorizationRepository.findByPatientIdAndActiveTrue(patientId).stream()
                .map(this::toResponse)
                .toList();
    }

    private AuthorizationResponse toResponse(DoctorPatientAuthorization a) {
        return AuthorizationResponse.builder()
                .id(a.getId())
                .doctorId(a.getDoctor().getId())
                .doctorName(a.getDoctor().getUser().getFullName())
                .specialization(a.getDoctor().getSpecialization())
                .active(a.isActive())
                .authorizedAt(a.getAuthorizedAt())
                .build();
    }
}
