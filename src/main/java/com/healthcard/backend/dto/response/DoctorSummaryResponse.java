package com.healthcard.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorSummaryResponse {
    private Long doctorId;
    private String fullName;
    private String specialization;
    private String hospitalName;
    private Integer experienceYears;
    private boolean verified;
}
