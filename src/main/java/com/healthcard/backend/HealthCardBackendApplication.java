package com.healthcard.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class HealthCardBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(HealthCardBackendApplication.class, args);
    }
}
