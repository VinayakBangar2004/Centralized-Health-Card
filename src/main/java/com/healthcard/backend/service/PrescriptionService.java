package com.healthcard.backend.service;

import com.healthcard.backend.dto.request.CreatePrescriptionRequest;
import com.healthcard.backend.dto.request.PrescriptionItemRequest;
import com.healthcard.backend.dto.response.PrescriptionItemResponse;
import com.healthcard.backend.dto.response.PrescriptionResponse;
import com.healthcard.backend.entity.*;
import com.healthcard.backend.entity.enums.AuditAction;
import com.healthcard.backend.exception.ResourceNotFoundException;
import com.healthcard.backend.repository.AppointmentRepository;
import com.healthcard.backend.repository.AuditLogRepository;
import com.healthcard.backend.repository.PrescriptionRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorService doctorService;
    private final PatientAccessService patientAccessService;
    private final AuditLogRepository auditLogRepository;
    private final NotificationService notificationService;

    @Transactional
    public PrescriptionResponse createPrescription(Long doctorUserId, CreatePrescriptionRequest req, HttpServletRequest httpRequest) {
        // Re-validates health card number + ID even if the doctor already has standing access,
        // ensuring every prescription is tied to a freshly verified card.
        Patient patient = patientAccessService.resolvePatientForDoctor(doctorUserId, req.healthCardNumber(), req.healthCardId(), httpRequest);
        Doctor doctor = doctorService.getDoctorByUserId(doctorUserId);

        Appointment appointment = null;
        if (req.appointmentId() != null) {
            appointment = appointmentRepository.findById(req.appointmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
        }

        Prescription prescription = Prescription.builder()
                .patient(patient)
                .doctor(doctor)
                .appointment(appointment)
                .diagnosis(req.diagnosis())
                .advice(req.advice())
                .build();

        for (PrescriptionItemRequest item : req.items()) {
            prescription.getItems().add(PrescriptionItem.builder()
                    .prescription(prescription)
                    .medicineName(item.medicineName())
                    .dosage(item.dosage())
                    .frequency(item.frequency())
                    .duration(item.duration())
                    .instructions(item.instructions())
                    .build());
        }

        prescription = prescriptionRepository.save(prescription);

        auditLogRepository.save(AuditLog.builder()
                .actor(doctor.getUser())
                .targetPatient(patient)
                .action(AuditAction.PRESCRIPTION_CREATED)
                .details("Prescription #" + prescription.getId() + " - " + req.diagnosis())
                .build());

        notificationService.notifyUser(patient.getUser(), "New prescription",
                "Dr. " + doctor.getUser().getFullName() + " issued a new prescription for: " + req.diagnosis());

        return toResponse(prescription);
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getForPatient(Long patientId) {
        return prescriptionRepository.findByPatientIdOrderByCreatedAtDesc(patientId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getForDoctor(Long doctorId) {
        return prescriptionRepository.findByDoctorIdOrderByCreatedAtDesc(doctorId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PrescriptionResponse toResponse(Prescription p) {
        List<PrescriptionItemResponse> items = p.getItems().stream()
                .map(i -> PrescriptionItemResponse.builder()
                        .medicineName(i.getMedicineName())
                        .dosage(i.getDosage())
                        .frequency(i.getFrequency())
                        .duration(i.getDuration())
                        .instructions(i.getInstructions())
                        .build())
                .toList();

        return PrescriptionResponse.builder()
                .id(p.getId())
                .patientId(p.getPatient().getId())
                .patientName(p.getPatient().getUser().getFullName())
                .doctorId(p.getDoctor().getId())
                .doctorName(p.getDoctor().getUser().getFullName())
                .diagnosis(p.getDiagnosis())
                .advice(p.getAdvice())
                .items(items)
                .createdAt(p.getCreatedAt())
                .build();
    }
}
