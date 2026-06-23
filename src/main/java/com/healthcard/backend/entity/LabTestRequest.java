package com.healthcard.backend.entity;

import com.healthcard.backend.entity.enums.LabTestStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "lab_test_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabTestRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pathologist_id")
    private Pathologist pathologist;

    @Column(nullable = false)
    private String testName;

    @Column(columnDefinition = "TEXT")
    private String clinicalNotes;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private LabTestStatus status = LabTestStatus.REQUESTED;

    @Builder.Default
    private LocalDateTime requestedAt = LocalDateTime.now();
}
