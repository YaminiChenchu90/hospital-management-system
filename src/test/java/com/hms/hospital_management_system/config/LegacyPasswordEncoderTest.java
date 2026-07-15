package com.hms.hospital_management_system.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyPasswordEncoderTest {

    private final PasswordEncoder encoder = new LegacyPasswordEncoder();

    @Test
    void matchesLegacyPlaintextPasswords() {
        assertTrue(encoder.matches("admin123", "admin123"));
    }

    @Test
    void matchesBcryptEncodedPasswords() {
        String encoded = encoder.encode("secret123");
        assertTrue(encoder.matches("secret123", encoded));
        assertFalse(encoder.matches("wrong", encoded));
    }
}
