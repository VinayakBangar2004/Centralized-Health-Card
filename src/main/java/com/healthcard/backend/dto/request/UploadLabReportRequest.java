package com.healthcard.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UploadLabReportRequest(
        @NotNull(message = "Lab test request ID is required") Long labTestRequestId,
        @NotBlank(message = "Findings are required") String findings,
        String remarks
) {}
