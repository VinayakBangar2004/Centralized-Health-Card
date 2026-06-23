package com.healthcard.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Both fields are required for a doctor to unlock a patient record.
 * healthCardNumber is what's visible on the card; healthCardId is the
 * secret the patient must read out / share at the time of consultation.
 */
public record HealthCardLookupRequest(
        @NotBlank(message = "Health card number is required") String healthCardNumber,
        @NotBlank(message = "Health card ID is required") String healthCardId
) {}
