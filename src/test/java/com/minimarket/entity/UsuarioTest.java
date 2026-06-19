package com.minimarket.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public class UsuarioTest {

    private Usuario usuario;
    private Rol rolMock;

    @BeforeEach
    public void setUp() {
        // Inicializamos el objeto principal
        usuario = new Usuario();
        
        // Creamos un rol simulado (usando el constructor que vimos en tu clase Rol)
        rolMock = new Rol("ADMIN");
        rolMock.setId(1L);
    }

    @Test
    public void testGettersYSetters() {
        // Act: Asignamos todos los valores al usuario
        usuario.setId(99L);
        usuario.setUsername("admin_user");
        usuario.setPassword("claveSuperSegura123");
        usuario.setRoles(Set.of(rolMock));

        // Assert: Validamos los campos básicos
        assertEquals(99L, usuario.getId(), "El ID debe ser 99L");
        assertEquals("admin_user", usuario.getUsername(), "El username debe coincidir");
        assertEquals("claveSuperSegura123", usuario.getPassword(), "El password debe coincidir");
        
        // Assert: Validamos la relación con la colección de Roles
        assertNotNull(usuario.getRoles(), "La lista de roles no debe ser nula");
        assertEquals(1, usuario.getRoles().size(), "Debe tener exactamente 1 rol asignado");
        assertTrue(usuario.getRoles().contains(rolMock), "El rol asignado debe estar presente en la lista");
    }
}