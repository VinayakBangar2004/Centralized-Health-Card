package com.healthcard.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.healthcard.backend.entity.enums.HealthCardStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * The core artifact of the system. Every patient is issued exactly one card.
 *
 * healthCardNumber  -> public-facing identifier, printed/displayed on the card,
 *                       safe to show on screen (e.g. HC-7F3K-9D21-XQ4P).
 * healthCardId      -> secret verification ID (like a PIN), never displayed in
 *                       full anywhere except to the patient. A doctor MUST supply
 *                       BOTH values correctly to pull a patient's medical record.
 *
 * This two-factor design means a health card number alone (which could be seen
 * on a physical card glimpsed by anyone) cannot unlock records.
 */
@Entity
@Table(name = "health_cards")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false, unique = true)
    private Patient patient;

    @Column(nullable = false, unique = true, updatable = false)
    private String healthCardNumber;

    @Column(nullable = false, unique = true, updatable = false)
    @JsonIgnore
    private String healthCardId;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private HealthCardStatus status = HealthCardStatus.ACTIVE;

    @Builder.Default
    private LocalDate issueDate = LocalDate.now();

    private LocalDate expiryDate;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String qrCodeBase64;
}
