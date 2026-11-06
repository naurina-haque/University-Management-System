package com.example.university.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-key-at-least-32-characters-long";

    // Test 5
    @Test
    void generateToken_thenParse_returnsUsernameAndRole() {
        JwtService jwtService = new JwtService(SECRET, 3_600_000L);

        String token = jwtService.generateToken("admin", "ADMIN");
        Claims claims = jwtService.parse(token);

        assertThat(claims.getSubject()).isEqualTo("admin");
        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
    }

    // Test 6
    @Test
    void parse_shouldRejectExpiredToken() {
        JwtService jwtService = new JwtService(SECRET, -1000L);   // already expired
        String token = jwtService.generateToken("admin", "ADMIN");

        assertThatThrownBy(() -> jwtService.parse(token)).isInstanceOf(JwtException.class);
    }
}