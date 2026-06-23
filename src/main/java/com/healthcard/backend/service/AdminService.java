package com.healthcard.backend.service;

import com.healthcard.backend.dto.response.*;
import com.healthcard.backend.entity.AuditLog;
import com.healthcard.backend.entity.Doctor;
import com.healthcard.backend.entity.Pathologist;
import com.healthcard.backend.entity.enums.AppointmentStatus;
import com.healthcard.backend.entity.enums.AuditAction;
import com.healthcard.backend.exception.ResourceNotFoundException;
import com.healthcard.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final DoctorRepository doctorRepository;
    private final PathologistRepository pathologistRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final LabReportRepository labReportRepository;
    private final AuditLogRepository auditLogRepository;
    private final DoctorService doctorService;
    private final PathologistService pathologistService;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<DoctorProfileResponse> getPendingDoctors() {
        return doctorRepository.findByVerifiedFalse().stream().map(doctorService::toProfileResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PathologistProfileResponse> getPendingPathologists() {
        return pathologistRepository.findByVerifiedFalse().stream().map(pathologistService::toProfileResponse).toList();
    }

    @Transactional
    public DoctorProfileResponse setDoctorVerified(Long doctorId, boolean verified) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
        doctor.setVerified(verified);
        doctorRepository.save(doctor);

        auditLogRepository.save(AuditLog.builder()
                .actor(doctor.getUser())
                .action(AuditAction.DOCTOR_VERIFIED)
                .details("Verification set to " + verified + " by admin")
                .build());

        notificationService.notifyUser(doctor.getUser(),
                verified ? "Account verified" : "Verification revoked",
                verified ? "Your doctor account has been verified. You can now access patient records using their health card."
                         : "Your doctor account verification has been revoked by an administrator.");

        return doctorService.toProfileResponse(doctor);
    }

    @Transactional
    public PathologistProfileResponse setPathologistVerified(Long pathologistId, boolean verified) {
        Pathologist pathologist = pathologistRepository.findById(pathologistId)
                .orElseThrow(() -> new ResourceNotFoundException("Pathologist not found"));
        pathologist.setVerified(verified);
        pathologistRepository.save(pathologist);

        auditLogRepository.save(AuditLog.builder()
                .actor(pathologist.getUser())
                .action(AuditAction.PATHOLOGIST_VERIFIED)
                .details("Verification set to " + verified + " by admin")
                .build());

        notificationService.notifyUser(pathologist.getUser(),
                verified ? "Account verified" : "Verification revoked",
                verified ? "Your pathologist account has been verified."
                         : "Your pathologist account verification has been revoked by an administrator.");

        return pathologistService.toProfileResponse(pathologist);
    }

    public DashboardStatsResponse getDashboardStats() {
        Map<String, Long> byStatus = new HashMap<>();
        for (AppointmentStatus status : AppointmentStatus.values()) {
            byStatus.put(status.name(), 0L);
        }
        appointmentRepository.findAll().forEach(a -> byStatus.merge(a.getStatus().name(), 1L, Long::sum));

        return DashboardStatsResponse.builder()
                .totalPatients(patientRepository.count())
                .totalDoctors(doctorRepository.count())
                .totalPathologists(pathologistRepository.count())
                .pendingDoctorVerifications(doctorRepository.findByVerifiedFalse().size())
                .pendingPathologistVerifications(pathologistRepository.findByVerifiedFalse().size())
                .totalAppointments(appointmentRepository.count())
                .totalPrescriptions(prescriptionRepository.count())
                .totalLabReports(labReportRepository.count())
                .appointmentsByStatus(byStatus)
                .build();
    }
}
