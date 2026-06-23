package com.healthcard.backend.service;

import com.healthcard.backend.dto.request.BookAppointmentRequest;
import com.healthcard.backend.dto.request.UpdateAppointmentStatusRequest;
import com.healthcard.backend.dto.response.AppointmentResponse;
import com.healthcard.backend.entity.*;
import com.healthcard.backend.entity.enums.AppointmentStatus;
import com.healthcard.backend.entity.enums.AuditAction;
import com.healthcard.backend.exception.ResourceNotFoundException;
import com.healthcard.backend.repository.AppointmentRepository;
import com.healthcard.backend.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final com.healthcard.backend.repository.PatientRepository patientRepository;
    private final DoctorService doctorService;
    private final AuditLogRepository auditLogRepository;
    private final NotificationService notificationService;

    /**
     * Takes the patient's USER id (not a pre-fetched Patient entity) and loads
     * it here, inside this transaction. Passing in an already-fetched entity
     * from a non-transactional controller would hand us a detached object
     * whose lazy fields (patient.getUser(), etc.) can no longer be resolved.
     */
    @Transactional
    public AppointmentResponse bookAppointment(Long patientUserId, BookAppointmentRequest req) {
        Patient patient = patientRepository.findByUserId(patientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));
        Doctor doctor = doctorService.getDoctorById(req.doctorId());

        Appointment appt = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentTime(req.appointmentTime())
                .reasonForVisit(req.reasonForVisit())
                .status(AppointmentStatus.PENDING)
                .build();
        appt = appointmentRepository.save(appt);

        auditLogRepository.save(AuditLog.builder()
                .actor(patient.getUser())
                .targetPatient(patient)
                .action(AuditAction.APPOINTMENT_BOOKED)
                .details("Booked with Dr. " + doctor.getUser().getFullName() + " for " + req.appointmentTime())
                .build());

        notificationService.notifyUser(doctor.getUser(), "New appointment request",
                patient.getUser().getFullName() + " requested an appointment on " + req.appointmentTime());

        return toResponse(appt);
    }

    @Transactional
    public AppointmentResponse updateStatus(Long doctorUserId, Long appointmentId, UpdateAppointmentStatusRequest req) {
        Appointment appt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        Doctor doctor = doctorService.getDoctorByUserId(doctorUserId);
        if (!appt.getDoctor().getId().equals(doctor.getId())) {
            throw new ResourceNotFoundException("Appointment not found");
        }

        appt.setStatus(req.status());
        if (req.doctorNotes() != null) appt.setDoctorNotes(req.doctorNotes());
        appointmentRepository.save(appt);

        auditLogRepository.save(AuditLog.builder()
                .actor(doctor.getUser())
                .targetPatient(appt.getPatient())
                .action(AuditAction.APPOINTMENT_STATUS_CHANGED)
                .details("Status changed to " + req.status())
                .build());

        notificationService.notifyUser(appt.getPatient().getUser(), "Appointment update",
                "Your appointment with Dr. " + doctor.getUser().getFullName() + " is now " + req.status());

        return toResponse(appt);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getForPatient(Long patientId) {
        return appointmentRepository.findByPatientIdOrderByAppointmentTimeDesc(patientId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getForDoctor(Long doctorId) {
        return appointmentRepository.findByDoctorIdOrderByAppointmentTimeDesc(doctorId).stream()
                .map(this::toResponse).toList();
    }

    private AppointmentResponse toResponse(Appointment a) {
        return AppointmentResponse.builder()
                .id(a.getId())
                .patientId(a.getPatient().getId())
                .patientName(a.getPatient().getUser().getFullName())
                .doctorId(a.getDoctor().getId())
                .doctorName(a.getDoctor().getUser().getFullName())
                .doctorSpecialization(a.getDoctor().getSpecialization())
                .appointmentTime(a.getAppointmentTime())
                .reasonForVisit(a.getReasonForVisit())
                .doctorNotes(a.getDoctorNotes())
                .status(a.getStatus())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
