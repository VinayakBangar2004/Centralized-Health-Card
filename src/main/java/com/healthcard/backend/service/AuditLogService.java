package com.healthcard.backend.service;

import com.healthcard.backend.dto.response.AuditLogResponse;
import com.healthcard.backend.entity.AuditLog;
import com.healthcard.backend.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /** Used on the patient dashboard: "who has looked at my record". */
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getForPatient(Long patientId) {
        return auditLogRepository.findByTargetPatientIdOrderByTimestampDesc(patientId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAll() {
        return auditLogRepository.findAllByOrderByTimestampDesc().stream()
                .map(this::toResponse).toList();
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .actorName(log.getActor() != null ? log.getActor().getFullName() : "Unknown")
                .actorRole(log.getActor() != null ? log.getActor().getRole().name() : null)
                .targetPatientName(log.getTargetPatient() != null ? log.getTargetPatient().getUser().getFullName() : null)
                .action(log.getAction())
                .details(log.getDetails())
                .timestamp(log.getTimestamp())
                .build();
    }
}
