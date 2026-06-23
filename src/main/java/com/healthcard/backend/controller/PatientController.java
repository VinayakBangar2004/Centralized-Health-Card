package com.healthcard.backend.controller;

import com.healthcard.backend.dto.request.*;
import com.healthcard.backend.dto.response.*;
import com.healthcard.backend.entity.Patient;
import com.healthcard.backend.entity.User;
import com.healthcard.backend.service.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PATIENT')")
@Tag(name = "Patient", description = "Patient self-service: profile, health card, appointments, prescriptions, lab reports, bills, access log")
public class PatientController {

    private final PatientService patientService;
    private final AppointmentService appointmentService;
    private final PrescriptionService prescriptionService;
    private final LabService labService;
    private final BillingService billingService;
    private final AuditLogService auditLogService;
    private final AuthorizationService authorizationService;
    private final DoctorService doctorService;
    private final NotificationService notificationService;

    @GetMapping("/me")
    public PatientProfileResponse getMyProfile(@AuthenticationPrincipal User user) {
        return patientService.getMyProfile(user.getId());
    }

    @PutMapping("/me")
    public PatientProfileResponse updateMyProfile(@AuthenticationPrincipal User user, @RequestBody UpdatePatientProfileRequest req) {
        return patientService.updateMyProfile(user.getId(), req);
    }

    @GetMapping("/doctors")
    public List<DoctorSummaryResponse> browseVerifiedDoctors() {
        return doctorService.listVerifiedDoctors();
    }

    @PostMapping("/appointments")
    public AppointmentResponse bookAppointment(@AuthenticationPrincipal User user, @Valid @RequestBody BookAppointmentRequest req) {
        return appointmentService.bookAppointment(user.getId(), req);
    }

    @GetMapping("/appointments")
    public List<AppointmentResponse> myAppointments(@AuthenticationPrincipal User user) {
        Patient patient = patientService.getPatientByUserId(user.getId());
        return appointmentService.getForPatient(patient.getId());
    }

    @GetMapping("/prescriptions")
    public List<PrescriptionResponse> myPrescriptions(@AuthenticationPrincipal User user) {
        Patient patient = patientService.getPatientByUserId(user.getId());
        return prescriptionService.getForPatient(patient.getId());
    }

    @GetMapping("/lab-reports")
    public List<LabReportResponse> myLabReports(@AuthenticationPrincipal User user) {
        Patient patient = patientService.getPatientByUserId(user.getId());
        return labService.getReportsForPatient(patient.getId());
    }

    @GetMapping("/bills")
    public List<BillResponse> myBills(@AuthenticationPrincipal User user) {
        Patient patient = patientService.getPatientByUserId(user.getId());
        return billingService.getForPatient(patient.getId());
    }

    @PostMapping("/bills/pay")
    public PaymentResponse payBill(@AuthenticationPrincipal User user, @Valid @RequestBody MakePaymentRequest req) {
        return billingService.payBill(user.getId(), req);
    }

    @GetMapping("/access-log")
    public List<AuditLogResponse> myAccessLog(@AuthenticationPrincipal User user) {
        Patient patient = patientService.getPatientByUserId(user.getId());
        return auditLogService.getForPatient(patient.getId());
    }

    @GetMapping("/authorized-doctors")
    public List<AuthorizationResponse> myAuthorizedDoctors(@AuthenticationPrincipal User user) {
        Patient patient = patientService.getPatientByUserId(user.getId());
        return authorizationService.listForPatient(patient.getId());
    }

    @PostMapping("/authorized-doctors")
    public AuthorizationResponse authorizeDoctor(@AuthenticationPrincipal User user, @Valid @RequestBody AuthorizeDoctorRequest req) {
        return authorizationService.authorizeDoctor(user.getId(), req.doctorId());
    }

    @DeleteMapping("/authorized-doctors/{doctorId}")
    public void revokeDoctor(@AuthenticationPrincipal User user, @PathVariable Long doctorId) {
        authorizationService.revoke(user.getId(), doctorId);
    }

    @GetMapping("/notifications")
    public List<NotificationResponse> myNotifications(@AuthenticationPrincipal User user) {
        return notificationService.getMyNotifications(user.getId());
    }

    @PatchMapping("/notifications/{id}/read")
    public void markNotificationRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
    }
}
