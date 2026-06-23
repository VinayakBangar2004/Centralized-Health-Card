package com.healthcard.backend.service;

import com.healthcard.backend.dto.request.CreateLabTestRequestDto;
import com.healthcard.backend.dto.response.LabReportResponse;
import com.healthcard.backend.dto.response.LabTestRequestResponse;
import com.healthcard.backend.entity.*;
import com.healthcard.backend.entity.enums.AuditAction;
import com.healthcard.backend.entity.enums.LabTestStatus;
import com.healthcard.backend.exception.ResourceNotFoundException;
import com.healthcard.backend.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LabService {

    private final LabTestRequestRepository labTestRequestRepository;
    private final LabReportRepository labReportRepository;
    private final PathologistRepository pathologistRepository;
    private final DoctorService doctorService;
    private final PatientAccessService patientAccessService;
    private final AuditLogRepository auditLogRepository;
    private final NotificationService notificationService;
    private final FileStorageService fileStorageService;

    @Transactional
    public LabTestRequestResponse requestLabTest(Long doctorUserId, CreateLabTestRequestDto req, HttpServletRequest httpRequest) {
        Patient patient = patientAccessService.resolvePatientForDoctor(doctorUserId, req.healthCardNumber(), req.healthCardId(), httpRequest);
        Doctor doctor = doctorService.getDoctorByUserId(doctorUserId);

        LabTestRequest request = LabTestRequest.builder()
                .patient(patient)
                .doctor(doctor)
                .testName(req.testName())
                .clinicalNotes(req.clinicalNotes())
                .status(LabTestStatus.REQUESTED)
                .build();
        request = labTestRequestRepository.save(request);

        return toRequestResponse(request);
    }

    /** Pathologist picks up an unassigned request from the open queue. */
    @Transactional
    public LabTestRequestResponse claimRequest(Long pathologistUserId, Long requestId) {
        Pathologist pathologist = pathologistRepository.findByUserId(pathologistUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Pathologist profile not found"));

        LabTestRequest request = labTestRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Lab test request not found"));

        request.setPathologist(pathologist);
        request.setStatus(LabTestStatus.SAMPLE_COLLECTED);
        labTestRequestRepository.save(request);

        return toRequestResponse(request);
    }

    @Transactional
    public LabReportResponse uploadReport(Long pathologistUserId, Long labTestRequestId, String findings, String remarks, MultipartFile attachment) {
        Pathologist pathologist = pathologistRepository.findByUserId(pathologistUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Pathologist profile not found"));

        LabTestRequest request = labTestRequestRepository.findById(labTestRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Lab test request not found"));

        String path = null;
        String originalName = null;
        if (attachment != null && !attachment.isEmpty()) {
            path = fileStorageService.store(attachment, request.getPatient().getId());
            originalName = attachment.getOriginalFilename();
        }

        LabReport report = LabReport.builder()
                .labTestRequest(request)
                .pathologist(pathologist)
                .patient(request.getPatient())
                .findings(findings)
                .remarks(remarks)
                .attachmentPath(path)
                .attachmentOriginalName(originalName)
                .build();
        report = labReportRepository.save(report);

        request.setStatus(LabTestStatus.COMPLETED);
        labTestRequestRepository.save(request);

        auditLogRepository.save(AuditLog.builder()
                .actor(pathologist.getUser())
                .targetPatient(request.getPatient())
                .action(AuditAction.LAB_REPORT_UPLOADED)
                .details("Lab report uploaded for test: " + request.getTestName())
                .build());

        notificationService.notifyUser(request.getPatient().getUser(), "Lab report ready",
                "Your " + request.getTestName() + " report is now available.");
        notificationService.notifyUser(request.getDoctor().getUser(), "Lab report ready",
                request.getTestName() + " report for " + request.getPatient().getUser().getFullName() + " is now available.");

        return toReportResponse(report);
    }

    @Transactional(readOnly = true)
    public List<LabTestRequestResponse> getOpenQueue() {
        return labTestRequestRepository.findByPathologistIsNullAndStatus(LabTestStatus.REQUESTED).stream()
                .map(this::toRequestResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<LabTestRequestResponse> getForDoctor(Long doctorId) {
        return labTestRequestRepository.findByDoctorIdOrderByRequestedAtDesc(doctorId).stream()
                .map(this::toRequestResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<LabTestRequestResponse> getForPathologist(Long pathologistId) {
        return labTestRequestRepository.findByPathologistIdOrderByRequestedAtDesc(pathologistId).stream()
                .map(this::toRequestResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<LabReportResponse> getReportsForPatient(Long patientId) {
        return labReportRepository.findByPatientIdOrderByReportDateDesc(patientId).stream()
                .map(this::toReportResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<LabReportResponse> getReportsForPathologist(Long pathologistId) {
        return labReportRepository.findByPathologistIdOrderByReportDateDesc(pathologistId).stream()
                .map(this::toReportResponse).toList();
    }

    @Transactional(readOnly = true)
    public LabTestRequestResponse toRequestResponse(LabTestRequest r) {
        return LabTestRequestResponse.builder()
                .id(r.getId())
                .patientId(r.getPatient().getId())
                .patientName(r.getPatient().getUser().getFullName())
                .doctorId(r.getDoctor().getId())
                .doctorName(r.getDoctor().getUser().getFullName())
                .pathologistId(r.getPathologist() != null ? r.getPathologist().getId() : null)
                .pathologistName(r.getPathologist() != null ? r.getPathologist().getUser().getFullName() : null)
                .testName(r.getTestName())
                .clinicalNotes(r.getClinicalNotes())
                .status(r.getStatus())
                .requestedAt(r.getRequestedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public LabReportResponse toReportResponse(LabReport r) {
        return LabReportResponse.builder()
                .id(r.getId())
                .labTestRequestId(r.getLabTestRequest().getId())
                .testName(r.getLabTestRequest().getTestName())
                .patientId(r.getPatient().getId())
                .patientName(r.getPatient().getUser().getFullName())
                .pathologistId(r.getPathologist().getId())
                .pathologistName(r.getPathologist().getUser().getFullName())
                .findings(r.getFindings())
                .remarks(r.getRemarks())
                .attachmentUrl(r.getAttachmentPath() != null ? "/uploads/lab-reports/" + r.getAttachmentPath() : null)
                .reportDate(r.getReportDate())
                .build();
    }
}
