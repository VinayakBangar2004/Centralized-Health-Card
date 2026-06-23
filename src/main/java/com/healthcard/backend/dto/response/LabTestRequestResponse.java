package com.healthcard.backend.dto.response;

import com.healthcard.backend.entity.enums.LabTestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabTestRequestResponse {
    private Long id;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private Long pathologistId;
    private String pathologistName;
    private String testName;
    private String clinicalNotes;
    private LabTestStatus status;
    private LocalDateTime requestedAt;
}
