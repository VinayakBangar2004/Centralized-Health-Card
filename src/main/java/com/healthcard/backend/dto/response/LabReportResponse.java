package com.healthcard.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabReportResponse {
    private Long id;
    private Long labTestRequestId;
    private String testName;
    private Long patientId;
    private String patientName;
    private Long pathologistId;
    private String pathologistName;
    private String findings;
    private String remarks;
    private String attachmentUrl;
    private LocalDateTime reportDate;
}
