package com.healthcard.backend.service;

import com.healthcard.backend.dto.response.DoctorProfileResponse;
import com.healthcard.backend.dto.response.DoctorSummaryResponse;
import com.healthcard.backend.entity.Doctor;
import com.healthcard.backend.exception.ResourceNotFoundException;
import com.healthcard.backend.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * NOTE on @Transactional(readOnly = true): spring.jpa.open-in-view is disabled
 * (see application.yml), and every entity association in this codebase is
 * lazily fetched on purpose. That means any method that touches a lazy field
 * (e.g. d.getUser().getFullName()) MUST run inside an open Hibernate session,
 * otherwise it throws LazyInitializationException the instant that field is
 * accessed. Marking these read methods @Transactional keeps the session open
 * for the whole method, including the entity -> DTO mapping at the end.
 */
@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;

    @Transactional(readOnly = true)
    public Doctor getDoctorByUserId(Long userId) {
        return doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));
    }

    @Transactional(readOnly = true)
    public Doctor getDoctorById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id " + id));
    }

    @Transactional(readOnly = true)
    public DoctorProfileResponse getMyProfile(Long userId) {
        Doctor d = getDoctorByUserId(userId);
        return toProfileResponse(d);
    }

    /** Directory of verified doctors patients can browse to book an appointment with. */
    @Transactional(readOnly = true)
    public List<DoctorSummaryResponse> listVerifiedDoctors() {
        return doctorRepository.findByVerifiedTrue().stream()
                .map(d -> DoctorSummaryResponse.builder()
                        .doctorId(d.getId())
                        .fullName(d.getUser().getFullName())
                        .specialization(d.getSpecialization())
                        .hospitalName(d.getHospitalName())
                        .experienceYears(d.getExperienceYears())
                        .verified(d.isVerified())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public DoctorProfileResponse toProfileResponse(Doctor d) {
        return DoctorProfileResponse.builder()
                .doctorId(d.getId())
                .fullName(d.getUser().getFullName())
                .email(d.getUser().getEmail())
                .phone(d.getUser().getPhone())
                .medicalLicenseNumber(d.getMedicalLicenseNumber())
                .specialization(d.getSpecialization())
                .hospitalName(d.getHospitalName())
                .experienceYears(d.getExperienceYears())
                .qualifications(d.getQualifications())
                .verified(d.isVerified())
                .build();
    }
}
