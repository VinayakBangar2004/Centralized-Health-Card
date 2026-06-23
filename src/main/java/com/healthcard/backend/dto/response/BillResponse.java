package com.healthcard.backend.dto.response;

import com.healthcard.backend.entity.enums.BillStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillResponse {
    private Long id;
    private Long patientId;
    private String patientName;
    private String raisedBy;
    private String description;
    private BigDecimal amount;
    private BillStatus status;
    private LocalDateTime createdAt;
}
