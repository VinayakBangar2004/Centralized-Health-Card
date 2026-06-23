package com.healthcard.backend.controller;

import com.healthcard.backend.dto.request.*;
import com.healthcard.backend.dto.response.*;
import com.healthcard.backend.entity.Doctor;
import com.healthcard.backend.entity.User;
import com.healthcard.backend.service.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR')")
@Tag(name = "Doctor", description = "Doctor workflows: patient lookup via health card, prescriptions, lab test requests, appointments, billing")
public class DoctorController {

    private final DoctorService doctorService;
    private final PatientRecordService patientRecordService;
    private final PrescriptionService prescriptionService;
    private final LabService labService;
    private final AppointmentService appointmentService;
    private final BillingService billingService;
    private final NotificationService notificationService;

    @GetMapping("/me")
    public DoctorProfileResponse getMyProfile(@AuthenticationPrincipal User user) {
        return doctorService.getMyProfile(user.getId());
    }

    /**
     * THE core endpoint of the whole system: only a verified doctor who supplies
     * BOTH the health card number and the secret health card ID gets the patient's
     * full medical bundle back. Every call - successful or not - is audited.
     */
    @PostMapping("/patients/lookup")
    public PatientRecordResponse lookupPatient(@AuthenticationPrincipal User user,
                                                @Valid @RequestBody HealthCardLookupRequest req,
                                                HttpServletRequest httpRequest) {
        return patientRecordService.lookup(user.getId(), req, httpRequest);
    }

    @PostMapping("/prescriptions")
    public PrescriptionResponse createPrescription(@AuthenticationPrincipal User user,
                                                     @Valid @RequestBody CreatePrescriptionRequest req,
                                                     HttpServletRequest httpRequest) {
        return prescriptionService.createPrescription(user.getId(), req, httpRequest);
    }

    @GetMapping("/prescriptions")
    public List<PrescriptionResponse> myIssuedPrescriptions(@AuthenticationPrincipal User user) {
        Doctor doctor = doctorService.getDoctorByUserId(user.getId());
        return prescriptionService.getForDoctor(doctor.getId());
    }

    @PostMapping("/lab-tests")
    public LabTestRequestResponse requestLabTest(@AuthenticationPrincipal User user,
                                                   @Valid @RequestBody CreateLabTestRequestDto req,
                                                   HttpServletRequest httpRequest) {
        return labService.requestLabTest(user.getId(), req, httpRequest);
    }

    @GetMapping("/lab-tests")
    public List<LabTestRequestResponse> myLabTestRequests(@AuthenticationPrincipal User user) {
        Doctor doctor = doctorService.getDoctorByUserId(user.getId());
        return labService.getForDoctor(doctor.getId());
    }

    @PostMapping("/bills")
    public BillResponse createBill(@AuthenticationPrincipal User user,
                                    @Valid @RequestBody CreateBillRequest req,
                                    HttpServletRequest httpRequest) {
        return billingService.createBillByDoctor(user.getId(), req, httpRequest);
    }

    @GetMapping("/bills")
    public List<BillResponse> myRaisedBills(@AuthenticationPrincipal User user) {
        Doctor doctor = doctorService.getDoctorByUserId(user.getId());
        return billingService.getForDoctor(doctor.getId());
    }

    @GetMapping("/appointments")
    public List<AppointmentResponse> myAppointments(@AuthenticationPrincipal User user) {
        Doctor doctor = doctorService.getDoctorByUserId(user.getId());
        return appointmentService.getForDoctor(doctor.getId());
    }

    @PatchMapping("/appointments/{id}/status")
    public AppointmentResponse updateAppointmentStatus(@AuthenticationPrincipal User user,
                                                        @PathVariable Long id,
                                                        @Valid @RequestBody UpdateAppointmentStatusRequest req) {
        return appointmentService.updateStatus(user.getId(), id, req);
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
