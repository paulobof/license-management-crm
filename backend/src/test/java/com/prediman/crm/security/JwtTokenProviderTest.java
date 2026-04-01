package com.prediman.crm.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        String secret = "test-only-prediman-crm-jwt-secret-key-must-be-at-least-256-bits-long-for-hs256";
        jwtTokenProvider = new JwtTokenProvider(secret, 900000L, 604800000L);
    }

    @Test
    void generateToken_shouldReturnValidToken() {
        String token = jwtTokenProvider.generateToken("admin@test.com", "ADMIN");

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void getEmailFromToken_shouldReturnCorrectEmail() {
        String token = jwtTokenProvider.generateToken("admin@test.com", "ADMIN");

        assertEquals("admin@test.com", jwtTokenProvider.getEmailFromToken(token));
    }

    @Test
    void getRoleFromToken_shouldReturnCorrectRole() {
        String token = jwtTokenProvider.generateToken("admin@test.com", "ADMIN");

        assertEquals("ADMIN", jwtTokenProvider.getRoleFromToken(token));
    }

    @Test
    void isAccessToken_shouldReturnTrueForAccessToken() {
        String token = jwtTokenProvider.generateToken("admin@test.com", "ADMIN");

        assertTrue(jwtTokenProvider.isAccessToken(token));
        assertFalse(jwtTokenProvider.isRefreshToken(token));
    }

    @Test
    void isRefreshToken_shouldReturnTrueForRefreshToken() {
        String token = jwtTokenProvider.generateRefreshToken("admin@test.com", "ADMIN");

        assertTrue(jwtTokenProvider.isRefreshToken(token));
        assertFalse(jwtTokenProvider.isAccessToken(token));
    }

    @Test
    void validateToken_shouldReturnFalseForInvalidToken() {
        assertFalse(jwtTokenProvider.validateToken("invalid.token.here"));
    }

    @Test
    void validateToken_shouldReturnFalseForNullToken() {
        assertFalse(jwtTokenProvider.validateToken(null));
    }

    @Test
    void generateToken_accessAndRefreshShouldBeDifferent() {
        String access = jwtTokenProvider.generateToken("user@test.com", "USUARIO");
        String refresh = jwtTokenProvider.generateRefreshToken("user@test.com", "USUARIO");

        assertNotEquals(access, refresh);
        assertEquals("access", jwtTokenProvider.getTokenType(access));
        assertEquals("refresh", jwtTokenProvider.getTokenType(refresh));
    }
}
