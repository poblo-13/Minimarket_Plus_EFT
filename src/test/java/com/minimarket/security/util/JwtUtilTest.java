package com.minimarket.security.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {

    private JwtUtil jwtUtil;
    private UserDetails userDetails;

    @BeforeEach
    public void setUp() {
        jwtUtil = new JwtUtil();
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
}