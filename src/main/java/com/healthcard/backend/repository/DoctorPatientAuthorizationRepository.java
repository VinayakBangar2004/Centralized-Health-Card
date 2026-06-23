package com.healthcard.backend.repository;

import com.healthcard.backend.entity.DoctorPatientAuthorization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorPatientAuthorizationRepository extends JpaRepository<DoctorPatientAuthorization, Long> {
    Optional<DoctorPatientAuthorization> findByDoctorIdAndPatientIdAndActiveTrue(Long doctorId, Long patientId);
    List<DoctorPatientAuthorization> findByPatientIdAndActiveTrue(Long patientId);
    List<DoctorPatientAuthorization> findByDoctorIdAndActiveTrue(Long doctorId);
}
