package com.healthcard.backend.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record BookAppointmentRequest(
        @NotNull(message = "Doctor is required") Long doctorId,
        @NotNull(message = "Appointment time is required") @Future LocalDateTime appointmentTime,
        @NotBlank(message = "Reason for visit is required") String reasonForVisit
) {}
