package com.healthcard.backend.service;

import com.healthcard.backend.dto.request.HealthCardLookupRequest;
import com.healthcard.backend.dto.response.PatientRecordResponse;
import com.healthcard.backend.entity.Patient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Assembles the full bundle a verified doctor sees after a successful
 * health-card-number + health-card-ID lookup: profile, prescriptions,
 * lab reports and appointment history. All gated through PatientAccessService.
 */
@Service
@RequiredArgsConstructor
public class PatientRecordService {

    private final PatientAccessService patientAccessService;
    private final PatientService patientService;
    private final PrescriptionService prescriptionService;
    private final LabService labService;
    private final com.healthcard.backend.repository.AppointmentRepository appointmentRepository;
    private final AppointmentService appointmentService;

    /**
     * Plain @Transactional (NOT readOnly) and deliberately wraps the entire
     * bundle assembly: resolvePatientForDoctor writes an audit log row, and
     * every sub-service call below touches lazy associations (patient.getUser(),
     * prescription.getDoctor(), etc.) on the SAME Patient entity. Without one
     * shared transaction here, each call below would run in its own
     * transaction against an already-detached Patient and throw
     * LazyInitializationException the moment it tried to read a related field.
     */
    @Transactional
    public PatientRecordResponse lookup(Long doctorUserId, HealthCardLookupRequest req, HttpServletRequest httpRequest) {
        Patient patient = patientAccessService.resolvePatientForDoctor(
                doctorUserId, req.healthCardNumber(), req.healthCardId(), httpRequest);

        return PatientRecordResponse.builder()
                .profile(patientService.toProfileResponse(patient, false))
                .prescriptions(prescriptionService.getForPatient(patient.getId()))
                .labReports(labService.getReportsForPatient(patient.getId()))
                .appointments(appointmentService.getForPatient(patient.getId()))
                .build();
    }
}
