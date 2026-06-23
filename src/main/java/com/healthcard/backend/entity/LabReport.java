package com.healthcard.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "lab_reports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_test_request_id", nullable = false, unique = true)
    private LabTestRequest labTestRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pathologist_id", nullable = false)
    private Pathologist pathologist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(columnDefinition = "TEXT")
    private String findings;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    /** relative path under the configured upload dir, e.g. lab-reports/3/report.pdf */
    private String attachmentPath;

    private String attachmentOriginalName;

    @Builder.Default
    private LocalDateTime reportDate = LocalDateTime.now();
}
