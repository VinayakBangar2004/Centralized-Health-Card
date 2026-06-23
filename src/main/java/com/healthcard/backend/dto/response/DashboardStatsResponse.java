package com.healthcard.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private long totalPatients;
    private long totalDoctors;
    private long totalPathologists;
    private long pendingDoctorVerifications;
    private long pendingPathologistVerifications;
    private long totalAppointments;
    private long totalPrescriptions;
    private long totalLabReports;
    private Map<String, Long> appointmentsByStatus;
}
