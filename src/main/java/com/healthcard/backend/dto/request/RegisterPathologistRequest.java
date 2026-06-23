package com.healthcard.backend.dto.request;

import jakarta.validation.constraints.*;

public record RegisterPathologistRequest(
        @NotBlank(message = "Full name is required") String fullName,
        @NotBlank @Email(message = "A valid email is required") String email,
        @NotBlank @Size(min = 8, message = "Password must be at least 8 characters") String password,
        @NotBlank(message = "Phone number is required") String phone,
        @NotBlank(message = "Lab license number is required") String labLicenseNumber,
        String labName,
        String labAddress
) {}
