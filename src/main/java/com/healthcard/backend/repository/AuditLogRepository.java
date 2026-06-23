package com.healthcard.backend.repository;

import com.healthcard.backend.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByTargetPatientIdOrderByTimestampDesc(Long patientId);
    List<AuditLog> findByActorIdOrderByTimestampDesc(Long actorId);
    List<AuditLog> findAllByOrderByTimestampDesc();
}
