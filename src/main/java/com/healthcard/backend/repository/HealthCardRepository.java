package com.healthcard.backend.repository;

import com.healthcard.backend.entity.HealthCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HealthCardRepository extends JpaRepository<HealthCard, Long> {
    Optional<HealthCard> findByPatientId(Long patientId);
    Optional<HealthCard> findByHealthCardNumber(String healthCardNumber);
    Optional<HealthCard> findByHealthCardNumberAndHealthCardId(String healthCardNumber, String healthCardId);
    boolean existsByHealthCardNumber(String healthCardNumber);
    boolean existsByHealthCardId(String healthCardId);
}
