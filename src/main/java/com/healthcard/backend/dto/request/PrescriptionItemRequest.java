package com.healthcard.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PrescriptionItemRequest(
        @NotBlank(message = "Medicine name is required") String medicineName,
        String dosage,
        String frequency,
        String duration,
        String instructions
) {}
