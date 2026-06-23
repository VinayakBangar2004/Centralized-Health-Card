package com.healthcard.backend.dto.request;

import jakarta.validation.constraints.NotNull;

public record AuthorizeDoctorRequest(
        @NotNull(message = "Doctor ID is required") Long doctorId
) {}
