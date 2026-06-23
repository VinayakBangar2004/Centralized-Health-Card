package com.healthcard.backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreatePrescriptionRequest(
        @NotBlank(message = "Patient health card number is required") String healthCardNumber,
        @NotBlank(message = "Patient health card ID is required") String healthCardId,
        Long appointmentId,
        @NotBlank(message = "Diagnosis is required") String diagnosis,
        String advice,
        @NotEmpty(message = "At least one medicine must be added") @Valid List<PrescriptionItemRequest> items
) {}
