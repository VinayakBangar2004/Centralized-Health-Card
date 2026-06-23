package com.healthcard.backend.service;

import com.healthcard.backend.dto.request.*;
import com.healthcard.backend.dto.response.AuthResponse;
import com.healthcard.backend.entity.*;
import com.healthcard.backend.entity.enums.AuditAction;
import com.healthcard.backend.entity.enums.Role;
import com.healthcard.backend.exception.DuplicateResourceException;
import com.healthcard.backend.repository.*;
import com.healthcard.backend.security.JwtService;
import com.healthcard.backend.util.HealthCardIdGenerator;
import com.healthcard.backend.util.QrCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final PathologistRepository pathologistRepository;
    private final HealthCardRepository healthCardRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final NotificationService notificationService;

    @Transactional
    public AuthResponse registerPatient(RegisterPatientRequest req) {
        ensureEmailAvailable(req.email());

        User user = User.builder()
                .fullName(req.fullName())
                .email(req.email())
                .password(passwordEncoder.encode(req.password()))
                .phone(req.phone())
                .role(Role.PATIENT)
                .build();
        user = userRepository.save(user);

        Patient patient = Patient.builder()
                .user(user)
                .dateOfBirth(req.dateOfBirth())
                .gender(req.gender())
                .bloodGroup(req.bloodGroup())
                .address(req.address())
                .emergencyContactName(req.emergencyContactName())
                .emergencyContactPhone(req.emergencyContactPhone())
                .build();
        patient = patientRepository.save(patient);

        String cardNumber = generateUniqueCardNumber();
        String cardId = generateUniqueCardId();
        String qr = QrCodeGenerator.generateBase64Qr(cardNumber, 280);

        HealthCard healthCard = HealthCard.builder()
                .patient(patient)
                .healthCardNumber(cardNumber)
                .healthCardId(cardId)
                .expiryDate(java.time.LocalDate.now().plusYears(5))
                .qrCodeBase64(qr)
                .build();
        healthCardRepository.save(healthCard);

        notificationService.notifyUser(user, "Welcome to Centralized Health Card System",
                "Your health card " + cardNumber + " has been issued. Keep your health card ID confidential - " +
                        "only share it with doctors you trust during a consultation.");

        return buildAuthResponse(user, true);
    }

    @Transactional
    public AuthResponse registerDoctor(RegisterDoctorRequest req) {
        ensureEmailAvailable(req.email());

        User user = User.builder()
                .fullName(req.fullName())
                .email(req.email())
                .password(passwordEncoder.encode(req.password()))
                .phone(req.phone())
                .role(Role.DOCTOR)
                .build();
        user = userRepository.save(user);

        Doctor doctor = Doctor.builder()
                .user(user)
                .medicalLicenseNumber(req.medicalLicenseNumber())
                .specialization(req.specialization())
                .hospitalName(req.hospitalName())
                .experienceYears(req.experienceYears())
                .qualifications(req.qualifications())
                .verified(false)
                .build();
        doctorRepository.save(doctor);

        notificationService.notifyUser(user, "Registration received",
                "Your doctor account is pending verification by an administrator. " +
                        "You'll be able to access patient records once your medical license is verified.");

        return buildAuthResponse(user, false);
    }

    @Transactional
    public AuthResponse registerPathologist(RegisterPathologistRequest req) {
        ensureEmailAvailable(req.email());

        User user = User.builder()
                .fullName(req.fullName())
                .email(req.email())
                .password(passwordEncoder.encode(req.password()))
                .phone(req.phone())
                .role(Role.PATHOLOGIST)
                .build();
        user = userRepository.save(user);

        Pathologist pathologist = Pathologist.builder()
                .user(user)
                .labLicenseNumber(req.labLicenseNumber())
                .labName(req.labName())
                .labAddress(req.labAddress())
                .verified(false)
                .build();
        pathologistRepository.save(pathologist);

        notificationService.notifyUser(user, "Registration received",
                "Your pathologist account is pending verification by an administrator.");

        return buildAuthResponse(user, false);
    }

    public AuthResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password()));

        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new IllegalStateException("User vanished after authentication"));

        boolean verified = isVerified(user);

        auditLogRepository.save(AuditLog.builder()
                .actor(user)
                .action(AuditAction.LOGIN)
                .details(user.getRole() + " login")
                .build());

        return buildAuthResponse(user, verified);
    }

    private boolean isVerified(User user) {
        return switch (user.getRole()) {
            case DOCTOR -> doctorRepository.findByUserId(user.getId()).map(Doctor::isVerified).orElse(false);
            case PATHOLOGIST -> pathologistRepository.findByUserId(user.getId()).map(Pathologist::isVerified).orElse(false);
            default -> true;
        };
    }

    private AuthResponse buildAuthResponse(User user, boolean verified) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("uid", user.getId());
        String token = jwtService.generateToken(user, claims);

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .verified(verified)
                .build();
    }

    private void ensureEmailAvailable(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("An account with this email already exists");
        }
    }

    private String generateUniqueCardNumber() {
        String number;
        do {
            number = HealthCardIdGenerator.generateHealthCardNumber();
        } while (healthCardRepository.existsByHealthCardNumber(number));
        return number;
    }

    private String generateUniqueCardId() {
        String id;
        do {
            id = HealthCardIdGenerator.generateHealthCardId();
        } while (healthCardRepository.existsByHealthCardId(id));
        return id;
    }
}
