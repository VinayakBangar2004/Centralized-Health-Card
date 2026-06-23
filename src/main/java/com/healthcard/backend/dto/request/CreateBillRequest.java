package com.healthcard.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateBillRequest(
        @NotBlank(message = "Patient health card number is required") String healthCardNumber,
        @NotBlank(message = "Patient health card ID is required") String healthCardId,
        @NotBlank(message = "Description is required") String description,
        @NotNull @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than zero") BigDecimal amount
) {}
