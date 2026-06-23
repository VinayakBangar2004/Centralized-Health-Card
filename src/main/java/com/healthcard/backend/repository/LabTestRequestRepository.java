package com.healthcard.backend.repository;

import com.healthcard.backend.entity.LabTestRequest;
import com.healthcard.backend.entity.enums.LabTestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LabTestRequestRepository extends JpaRepository<LabTestRequest, Long> {
    List<LabTestRequest> findByPatientIdOrderByRequestedAtDesc(Long patientId);
    List<LabTestRequest> findByDoctorIdOrderByRequestedAtDesc(Long doctorId);
    List<LabTestRequest> findByPathologistIdOrderByRequestedAtDesc(Long pathologistId);
    List<LabTestRequest> findByStatusOrderByRequestedAtDesc(LabTestStatus status);
    List<LabTestRequest> findByPathologistIsNullAndStatus(LabTestStatus status);
}
