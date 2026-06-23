package com.healthcard.backend.dto.request;

import com.healthcard.backend.entity.enums.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateAppointmentStatusRequest(
        @NotNull AppointmentStatus status,
        String doctorNotes
) {}
