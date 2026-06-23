package com.healthcard.backend.dto.request;

import com.healthcard.backend.entity.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public record MakePaymentRequest(
        @NotNull Long billId,
        @NotNull PaymentMethod paymentMethod
) {}
