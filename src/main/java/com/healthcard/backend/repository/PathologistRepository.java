package com.healthcard.backend.repository;

import com.healthcard.backend.entity.Pathologist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PathologistRepository extends JpaRepository<Pathologist, Long> {
    Optional<Pathologist> findByUserId(Long userId);
    Optional<Pathologist> findByUserEmail(String email);
    List<Pathologist> findByVerifiedFalse();
    List<Pathologist> findByVerifiedTrue();
}
