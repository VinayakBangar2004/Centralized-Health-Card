package com.healthcard.backend.controller;

import com.healthcard.backend.dto.response.*;
import com.healthcard.backend.entity.Pathologist;
import com.healthcard.backend.entity.User;
import com.healthcard.backend.service.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/pathologists")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PATHOLOGIST')")
@Tag(name = "Pathologist", description = "Pathologist workflows: lab test queue, report upload, billing")
public class PathologistController {

    private final PathologistService pathologistService;
    private final LabService labService;
    private final BillingService billingService;
    private final NotificationService notificationService;

    @GetMapping("/me")
    public PathologistProfileResponse getMyProfile(@AuthenticationPrincipal User user) {
        return pathologistService.getMyProfile(user.getId());
    }

    /** Open queue of lab tests requested by doctors that no pathologist has claimed yet. */
    @GetMapping("/lab-tests/queue")
    public List<LabTestRequestResponse> openQueue() {
        return labService.getOpenQueue();
    }

    @PostMapping("/lab-tests/{id}/claim")
    public LabTestRequestResponse claim(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return labService.claimRequest(user.getId(), id);
    }

    @GetMapping("/lab-tests")
    public List<LabTestRequestResponse> myAssignedTests(@AuthenticationPrincipal User user) {
        Pathologist p = pathologistService.getByUserId(user.getId());
        return labService.getForPathologist(p.getId());
    }

    @PostMapping(value = "/lab-tests/{id}/report", consumes = "multipart/form-data")
    public LabReportResponse uploadReport(@AuthenticationPrincipal User user,
                                           @PathVariable("id") Long labTestRequestId,
                                           @RequestParam String findings,
                                           @RequestParam(required = false) String remarks,
                                           @RequestParam(required = false) MultipartFile attachment) {
        return labService.uploadReport(user.getId(), labTestRequestId, findings, remarks, attachment);
    }

    @GetMapping("/reports")
    public List<LabReportResponse> myUploadedReports(@AuthenticationPrincipal User user) {
        Pathologist p = pathologistService.getByUserId(user.getId());
        return labService.getReportsForPathologist(p.getId());
    }

    @PostMapping("/bills")
    public BillResponse createBill(@AuthenticationPrincipal User user,
                                    @RequestParam Long patientId,
                                    @RequestParam String description,
                                    @RequestParam BigDecimal amount) {
        return billingService.createBillByPathologist(user.getId(), patientId, description, amount);
    }

    @GetMapping("/bills")
    public List<BillResponse> myRaisedBills(@AuthenticationPrincipal User user) {
        Pathologist p = pathologistService.getByUserId(user.getId());
        return billingService.getForPathologist(p.getId());
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
