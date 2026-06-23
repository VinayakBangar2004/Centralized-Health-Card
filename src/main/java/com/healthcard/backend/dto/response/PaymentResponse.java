package com.healthcard.backend.dto.response;

import com.healthcard.backend.entity.enums.PaymentMethod;
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
public class PaymentResponse {
    private Long id;
    private Long billId;
    private BigDecimal amountPaid;
    private PaymentMethod paymentMethod;
    private String transactionId;
    private LocalDateTime paidAt;
}
