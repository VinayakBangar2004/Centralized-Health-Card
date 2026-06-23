package com.healthcard.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * The bundle returned to a DOCTOR after a successful health-card-number +
 * health-card-ID lookup. Deliberately excludes the secret healthCardId itself
 * and excludes billing/payment internals.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientRecordResponse {
    private PatientProfileResponse profile;
    private List<PrescriptionResponse> prescriptions;
    private List<LabReportResponse> labReports;
    private List<AppointmentResponse> appointments;
}
