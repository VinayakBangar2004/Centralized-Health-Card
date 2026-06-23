package com.healthcard.backend.config;

import com.healthcard.backend.entity.User;
import com.healthcard.backend.entity.enums.Role;
import com.healthcard.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds a default ADMIN account on first boot since there is no public
 * "register as admin" endpoint (admins must be provisioned out-of-band).
 * CHANGE THIS PASSWORD IMMEDIATELY in any real deployment.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String adminEmail = "admin@healthcard.system";
        if (userRepository.existsByEmail(adminEmail)) {
            return;
        }
        User admin = User.builder()
                .fullName("System Administrator")
                .email(adminEmail)
                .password(passwordEncoder.encode("Admin@123"))
                .phone("0000000000")
                .role(Role.ADMIN)
                .build();
        userRepository.save(admin);
        log.info("==============================================================");
        log.info(" Default admin account created: {} / Admin@123", adminEmail);
        log.info(" CHANGE THIS PASSWORD before deploying to production.");
        log.info("==============================================================");
    }
}
