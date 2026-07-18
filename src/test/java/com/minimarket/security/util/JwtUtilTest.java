package com.minimarket.security.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import io.jsonwebtoken.JwtException;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {

    private JwtUtil jwtUtil;
    private UserDetails userDetails;

    @BeforeEach
    public void setUp() {
        jwtUtil = new JwtUtil("test-secret-with-at-least-thirty-two-utf8-bytes", 3_600_000);
        userDetails = new User("goleador_admin", "clave123", new ArrayList<>());
    }

    @Test
    public void testGenerateAndValidateToken() {
        // Act: Generamos un token
        String token = jwtUtil.generateToken(userDetails);

        // Assert: Validamos que no esté vacío y que sea válido para el usuario
        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token, userDetails));
        assertEquals("goleador_admin", jwtUtil.extractUsername(token));
    }

    @Test
    void rejectsShortSecretsAndNonPositiveExpiration() {
        assertThrows(IllegalStateException.class, () -> new JwtUtil("short", 1));
        assertThrows(IllegalStateException.class,
                () -> new JwtUtil("test-secret-with-at-least-thirty-two-utf8-bytes", 0));
    }

    @Test
    void rejectsSignatureWhoseDecodedBytesActuallyChange() {
        String token = jwtUtil.generateToken(userDetails);
        String[] segments = token.split("\\.");
        String signature = segments[2];
        char replacement = signature.charAt(0) == 'A' ? 'B' : 'A';
        String manipulated = segments[0] + "." + segments[1] + "." + replacement + signature.substring(1);

        assertNotEquals(token, manipulated);
        assertThrows(JwtException.class, () -> jwtUtil.extractUsername(manipulated));
    }
}
