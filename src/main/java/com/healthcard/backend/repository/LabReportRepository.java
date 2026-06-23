package com.healthcard.backend.repository;

import com.healthcard.backend.entity.LabReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LabReportRepository extends JpaRepository<LabReport, Long> {
    List<LabReport> findByPatientIdOrderByReportDateDesc(Long patientId);
    List<LabReport> findByPathologistIdOrderByReportDateDesc(Long pathologistId);
    Optional<LabReport> findByLabTestRequestId(Long labTestRequestId);
}
