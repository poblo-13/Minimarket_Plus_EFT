package com.minimarket.security.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LoginRequestTest {

    @Test
    public void testGettersAndSetters() {
        // Act: Creamos la petición vacía y le asignamos valores
        LoginRequest request = new LoginRequest();
        request.setUsername("goleador99");
        request.setPassword("clave123");

        // Assert: Validamos que los valores se guardaron bien
        assertEquals("goleador99", request.getUsername());
        assertEquals("clave123", request.getPassword());
    }

    @Test
    public void testAllArgsConstructor() {
        // Act: Usamos el constructor con parámetros que nos dio Lombok
        LoginRequest request = new LoginRequest("admin", "secreta");

        // Assert: Validamos
        assertEquals("admin", request.getUsername());
        assertEquals("secreta", request.getPassword());
    }
}