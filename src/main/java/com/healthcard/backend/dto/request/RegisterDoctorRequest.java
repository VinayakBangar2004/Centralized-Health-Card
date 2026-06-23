package com.healthcard.backend.dto.request;

import jakarta.validation.constraints.*;

public record RegisterDoctorRequest(
        @NotBlank(message = "Full name is required") String fullName,
        @NotBlank @Email(message = "A valid email is required") String email,
        @NotBlank @Size(min = 8, message = "Password must be at least 8 characters") String password,
        @NotBlank(message = "Phone number is required") String phone,
        @NotBlank(message = "Medical license number is required") String medicalLicenseNumber,
        @NotBlank(message = "Specialization is required") String specialization,
        String hospitalName,
        Integer experienceYears,
        String qualifications
) {}
