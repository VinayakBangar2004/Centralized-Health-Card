package com.healthcard.backend.util;

import java.security.SecureRandom;
import java.util.UUID;

public class HealthCardIdGenerator {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // no 0/O/1/I to avoid confusion
    private static final SecureRandom RANDOM = new SecureRandom();

    /** Public-facing number printed on the card, e.g. HC-7F3K-9D21-XQ4P */
    public static String generateHealthCardNumber() {
        StringBuilder sb = new StringBuilder("HC-");
        for (int group = 0; group < 3; group++) {
            for (int i = 0; i < 4; i++) {
                sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
            }
            if (group < 2) sb.append("-");
        }
        return sb.toString();
    }

    /** Secret verification ID - acts like a PIN. Never shown in full except to the patient. */
    public static String generateHealthCardId() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
}
