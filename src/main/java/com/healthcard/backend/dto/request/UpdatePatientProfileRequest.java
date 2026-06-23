package com.healthcard.backend.dto.request;

public record UpdatePatientProfileRequest(
        String address,
        String bloodGroup,
        String emergencyContactName,
        String emergencyContactPhone,
        String knownAllergies,
        String chronicConditions
) {}
