package com.minimarket.security.model;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CustomUserDetailsTest {

    private CustomUserDetails customUserDetails;
    private Usuario usuario;

    @BeforeEach
    public void setUp() {
        // Preparamos nuestro usuario titular para la prueba
        usuario = new Usuario();
        usuario.setUsername("goleador_admin");
        usuario.setPassword("superSecreta123");

        Rol rolAdmin = new Rol();
        rolAdmin.setNombre("ROLE_ADMIN");

        Set<Rol> roles = new HashSet<>();
        roles.add(rolAdmin);
        usuario.setRoles(roles);

        // Instanciamos la clase que vamos a probar
        customUserDetails = new CustomUserDetails(usuario);
    }

    @Test
    public void testGetAuthorities() {
        // Ejecutamos
        Collection<? extends GrantedAuthority> authorities = customUserDetails.getAuthorities();

        // Assert: Validamos que el rol se tradujo correctamente
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertEquals("ROLE_ADMIN", authorities.iterator().next().getAuthority());
    }

    @Test
    public void testGetUsernameAndPassword() {
        // Validamos que entregue las credenciales correctas
        assertEquals("goleador_admin", customUserDetails.getUsername());
        assertEquals("superSecreta123", customUserDetails.getPassword());
    }

    @Test
    public void testAccountStatusFlags() {
        // Validamos que todas las banderas de seguridad estén en verde (true)
        assertTrue(customUserDetails.isAccountNonExpired());
        assertTrue(customUserDetails.isAccountNonLocked());
        assertTrue(customUserDetails.isCredentialsNonExpired());
        assertTrue(customUserDetails.isEnabled());
    }
}