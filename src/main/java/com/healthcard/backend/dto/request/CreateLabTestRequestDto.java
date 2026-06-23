package com.healthcard.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateLabTestRequestDto(
        @NotBlank(message = "Patient health card number is required") String healthCardNumber,
        @NotBlank(message = "Patient health card ID is required") String healthCardId,
        @NotBlank(message = "Test name is required") String testName,
        String clinicalNotes
) {}
