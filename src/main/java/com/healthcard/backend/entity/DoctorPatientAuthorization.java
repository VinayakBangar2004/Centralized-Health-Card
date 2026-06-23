package com.healthcard.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standing consent a patient grants to a specific doctor so that doctor can
 * access the record again without re-entering the health card ID every visit.
 * Patients can revoke this at any time from their dashboard.
 */
@Entity
@Table(name = "doctor_patient_authorizations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"doctor_id", "patient_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorPatientAuthorization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    private LocalDateTime authorizedAt = LocalDateTime.now();

    private LocalDateTime revokedAt;

    private LocalDateTime expiresAt;
}
