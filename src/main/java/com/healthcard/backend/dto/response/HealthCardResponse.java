package com.healthcard.backend.dto.response;

import com.healthcard.backend.entity.enums.HealthCardStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthCardResponse {
    private String healthCardNumber;
    /** Only ever populated when returned to the card owner themselves. */
    private String healthCardId;
    private String patientName;
    private HealthCardStatus status;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String qrCodeBase64;
}
