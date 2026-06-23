package com.healthcard.backend.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record RegisterPatientRequest(
        @NotBlank(message = "Full name is required") String fullName,
        @NotBlank @Email(message = "A valid email is required") String email,
        @NotBlank @Size(min = 8, message = "Password must be at least 8 characters") String password,
        @NotBlank(message = "Phone number is required") String phone,
        @NotNull(message = "Date of birth is required") @Past LocalDate dateOfBirth,
        @NotBlank String gender,
        String bloodGroup,
        String address,
        String emergencyContactName,
        String emergencyContactPhone
) {}
