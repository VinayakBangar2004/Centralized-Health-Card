package com.healthcard.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "doctors")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, unique = true)
    private String medicalLicenseNumber;

    private String specialization;

    private String hospitalName;

    private Integer experienceYears;

    @Column(columnDefinition = "TEXT")
    private String qualifications;

    /**
     * An unverified doctor cannot pull patient records even with a valid
     * health card number + health card ID. Admin must verify the license first.
     */
    @Builder.Default
    private boolean verified = false;
}
