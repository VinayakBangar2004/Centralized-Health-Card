package com.healthcard.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorProfileResponse {
    private Long doctorId;
    private String fullName;
    private String email;
    private String phone;
    private String medicalLicenseNumber;
    private String specialization;
    private String hospitalName;
    private Integer experienceYears;
    private String qualifications;
    private boolean verified;
}
