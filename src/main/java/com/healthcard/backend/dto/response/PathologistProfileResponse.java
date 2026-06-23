package com.healthcard.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PathologistProfileResponse {
    private Long pathologistId;
    private String fullName;
    private String email;
    private String phone;
    private String labLicenseNumber;
    private String labName;
    private String labAddress;
    private boolean verified;
}
