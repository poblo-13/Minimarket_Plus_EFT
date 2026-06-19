package com.minimarket.entity;

import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public class RolTest {

    @Test
    public void testConstructoresYGettersSetters() {
        // Preparamos un usuario simulado para la relación
        Usuario usuarioMock = new Usuario();
        usuarioMock.setId(1L);
        Set<Usuario> usuariosSet = Set.of(usuarioMock);

        // Act: Probamos el constructor completo
        Rol rolCompleto = new Rol(10L, "ADMIN", usuariosSet);

        // Assert: Validamos que el constructor llenó los datos correctamente
        assertEquals(10L, rolCompleto.getId(), "El ID debe ser 10L");
        assertEquals("ADMIN", rolCompleto.getNombre(), "El nombre debe ser ADMIN");
        assertNotNull(rolCompleto.getUsuarios(), "La lista de usuarios no debe ser nula");
        assertEquals(1, rolCompleto.getUsuarios().size(), "Debe tener 1 usuario asociado");

        // Act: Probamos el constructor simple y los Setters
        Rol rolSimple = new Rol("USER");
        rolSimple.setId(20L);
        rolSimple.setNombre("SUPER_USER");
        rolSimple.setUsuarios(usuariosSet);

        // Assert: Validamos que los Setters sobreescribieron los datos
        assertEquals(20L, rolSimple.getId(), "El ID actualizado debe ser 20L");
        assertEquals("SUPER_USER", rolSimple.getNombre(), "El nombre actualizado debe ser SUPER_USER");
        assertTrue(rolSimple.getUsuarios().contains(usuarioMock), "El usuario simulado debe estar en la lista");
    }
}