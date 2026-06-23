package com.healthcard.backend.service;

import com.healthcard.backend.dto.request.UpdatePatientProfileRequest;
import com.healthcard.backend.dto.response.HealthCardResponse;
import com.healthcard.backend.dto.response.PatientProfileResponse;
import com.healthcard.backend.entity.HealthCard;
import com.healthcard.backend.entity.Patient;
import com.healthcard.backend.exception.ResourceNotFoundException;
import com.healthcard.backend.repository.HealthCardRepository;
import com.healthcard.backend.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final HealthCardRepository healthCardRepository;

    @Transactional(readOnly = true)
    public Patient getPatientByUserId(Long userId) {
        return patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));
    }

    @Transactional(readOnly = true)
    public PatientProfileResponse getMyProfile(Long userId) {
        Patient p = getPatientByUserId(userId);
        return toProfileResponse(p, true);
    }

    @Transactional
    public PatientProfileResponse updateMyProfile(Long userId, UpdatePatientProfileRequest req) {
        Patient p = getPatientByUserId(userId);
        if (req.address() != null) p.setAddress(req.address());
        if (req.bloodGroup() != null) p.setBloodGroup(req.bloodGroup());
        if (req.emergencyContactName() != null) p.setEmergencyContactName(req.emergencyContactName());
        if (req.emergencyContactPhone() != null) p.setEmergencyContactPhone(req.emergencyContactPhone());
        if (req.knownAllergies() != null) p.setKnownAllergies(req.knownAllergies());
        if (req.chronicConditions() != null) p.setChronicConditions(req.chronicConditions());
        patientRepository.save(p);
        return toProfileResponse(p, true);
    }

    @Transactional(readOnly = true)
    public PatientProfileResponse toProfileResponse(Patient p, boolean includeSecretCardId) {
        HealthCard card = p.getHealthCard() != null ? p.getHealthCard()
                : healthCardRepository.findByPatientId(p.getId()).orElse(null);

        HealthCardResponse cardResponse = null;
        if (card != null) {
            cardResponse = HealthCardResponse.builder()
                    .healthCardNumber(card.getHealthCardNumber())
                    .healthCardId(includeSecretCardId ? card.getHealthCardId() : null)
                    .patientName(p.getUser().getFullName())
                    .status(card.getStatus())
                    .issueDate(card.getIssueDate())
                    .expiryDate(card.getExpiryDate())
                    .qrCodeBase64(card.getQrCodeBase64())
                    .build();
        }

        return PatientProfileResponse.builder()
                .patientId(p.getId())
                .fullName(p.getUser().getFullName())
                .email(p.getUser().getEmail())
                .phone(p.getUser().getPhone())
                .dateOfBirth(p.getDateOfBirth())
                .gender(p.getGender())
                .bloodGroup(p.getBloodGroup())
                .address(p.getAddress())
                .emergencyContactName(p.getEmergencyContactName())
                .emergencyContactPhone(p.getEmergencyContactPhone())
                .knownAllergies(p.getKnownAllergies())
                .chronicConditions(p.getChronicConditions())
                .healthCard(cardResponse)
                .build();
    }
}
