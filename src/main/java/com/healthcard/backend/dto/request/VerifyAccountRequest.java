package com.healthcard.backend.dto.request;

import jakarta.validation.constraints.NotNull;

public record VerifyAccountRequest(
        @NotNull Long accountId,
        @NotNull Boolean verified
) {}
