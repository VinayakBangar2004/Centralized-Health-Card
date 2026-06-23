package com.healthcard.backend.controller;

import com.healthcard.backend.dto.request.VerifyAccountRequest;
import com.healthcard.backend.dto.response.*;
import com.healthcard.backend.service.AdminService;
import com.healthcard.backend.service.AuditLogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Verify doctors/pathologists, view audit trail, dashboard stats")
public class AdminController {

    private final AdminService adminService;
    private final AuditLogService auditLogService;

    @GetMapping("/doctors/pending")
    public List<DoctorProfileResponse> pendingDoctors() {
        return adminService.getPendingDoctors();
    }

    @GetMapping("/pathologists/pending")
    public List<PathologistProfileResponse> pendingPathologists() {
        return adminService.getPendingPathologists();
    }

    @PatchMapping("/doctors/verify")
    public DoctorProfileResponse verifyDoctor(@Valid @RequestBody VerifyAccountRequest req) {
        return adminService.setDoctorVerified(req.accountId(), req.verified());
    }

    @PatchMapping("/pathologists/verify")
    public PathologistProfileResponse verifyPathologist(@Valid @RequestBody VerifyAccountRequest req) {
        return adminService.setPathologistVerified(req.accountId(), req.verified());
    }

    @GetMapping("/dashboard-stats")
    public DashboardStatsResponse dashboardStats() {
        return adminService.getDashboardStats();
    }

    @GetMapping("/audit-logs")
    public List<AuditLogResponse> allAuditLogs() {
        return auditLogService.getAll();
    }
}
