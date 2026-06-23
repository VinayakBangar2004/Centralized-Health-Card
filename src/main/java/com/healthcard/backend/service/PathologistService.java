package com.healthcard.backend.service;

import com.healthcard.backend.dto.response.PathologistProfileResponse;
import com.healthcard.backend.entity.Pathologist;
import com.healthcard.backend.exception.ResourceNotFoundException;
import com.healthcard.backend.repository.PathologistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PathologistService {

    private final PathologistRepository pathologistRepository;

    @Transactional(readOnly = true)
    public Pathologist getByUserId(Long userId) {
        return pathologistRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Pathologist profile not found"));
    }

    @Transactional(readOnly = true)
    public Pathologist getById(Long id) {
        return pathologistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pathologist not found with id " + id));
    }

    @Transactional(readOnly = true)
    public PathologistProfileResponse getMyProfile(Long userId) {
        Pathologist p = getByUserId(userId);
        return toProfileResponse(p);
    }

    @Transactional(readOnly = true)
    public PathologistProfileResponse toProfileResponse(Pathologist p) {
        return PathologistProfileResponse.builder()
                .pathologistId(p.getId())
                .fullName(p.getUser().getFullName())
                .email(p.getUser().getEmail())
                .phone(p.getUser().getPhone())
                .labLicenseNumber(p.getLabLicenseNumber())
                .labName(p.getLabName())
                .labAddress(p.getLabAddress())
                .verified(p.isVerified())
                .build();
    }
}
