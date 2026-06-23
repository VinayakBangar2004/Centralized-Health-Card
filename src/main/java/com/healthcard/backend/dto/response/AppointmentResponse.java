package com.healthcard.backend.dto.response;

import com.healthcard.backend.entity.enums.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {
    private Long id;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private String doctorSpecialization;
    private LocalDateTime appointmentTime;
    private String reasonForVisit;
    private String doctorNotes;
    private AppointmentStatus status;
    private LocalDateTime createdAt;
}
