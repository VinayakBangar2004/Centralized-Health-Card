package com.healthcard.backend.repository;

import com.healthcard.backend.entity.Bill;
import com.healthcard.backend.entity.enums.BillStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findByPatientIdOrderByCreatedAtDesc(Long patientId);
    List<Bill> findByPatientIdAndStatus(Long patientId, BillStatus status);
    List<Bill> findByDoctorIdOrderByCreatedAtDesc(Long doctorId);
    List<Bill> findByPathologistIdOrderByCreatedAtDesc(Long pathologistId);
}
