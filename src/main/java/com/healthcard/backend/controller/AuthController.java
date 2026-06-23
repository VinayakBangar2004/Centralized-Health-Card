package com.healthcard.backend.controller;

import com.healthcard.backend.dto.request.*;
import com.healthcard.backend.dto.response.AuthResponse;
import com.healthcard.backend.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Registration & login for patients, doctors and pathologists")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register/patient")
    public ResponseEntity<AuthResponse> registerPatient(@Valid @RequestBody RegisterPatientRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerPatient(req));
    }

    @PostMapping("/register/doctor")
    public ResponseEntity<AuthResponse> registerDoctor(@Valid @RequestBody RegisterDoctorRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerDoctor(req));
    }

    @PostMapping("/register/pathologist")
    public ResponseEntity<AuthResponse> registerPathologist(@Valid @RequestBody RegisterPathologistRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerPathologist(req));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }
}
