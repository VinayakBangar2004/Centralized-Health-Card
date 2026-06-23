package com.healthcard.backend.dto.response;

import com.healthcard.backend.entity.enums.AuditAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    private Long id;
    private String actorName;
    private String actorRole;
    private String targetPatientName;
    private AuditAction action;
    private String details;
    private LocalDateTime timestamp;
}
