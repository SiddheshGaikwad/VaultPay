package com.vaultpay.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

    JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        ReflectionTestUtils.setField(
                jwtService,
                "secretKey",
                "VaultPaySecretKeyForJWTAuthentication123456789"
        );
    }

    @Test
    void generateTokenShouldBeSuccessful() {
        String token = jwtService.generateToken("testuser", "CUSTOMER");
        assertNotNull(token);
    }

    @Test
    void extractUsernameShouldBeSuccessful() {
        String token = jwtService.generateToken("testuser", "CUSTOMER");
        String username = jwtService.extractUsername(token);

        assertEquals("testuser", username);
    }

    @Test
    void extractRoleShouldBeSuccessful() {
        String token = jwtService.generateToken("testuser", "CUSTOMER");
        String role = jwtService.extractRole(token);

        assertEquals("CUSTOMER", role);
    }

    @Test
    void adminTokenShouldContainAdminRole() {
        String token = jwtService.generateToken("admin", "ADMIN");

        assertEquals("admin", jwtService.extractUsername(token));
        assertEquals("ADMIN", jwtService.extractRole(token));
    }
}

