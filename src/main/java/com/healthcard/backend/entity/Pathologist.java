package com.healthcard.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pathologists")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pathologist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, unique = true)
    private String labLicenseNumber;

    private String labName;

    private String labAddress;

    @Builder.Default
    private boolean verified = false;
}
