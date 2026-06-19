package com.minimarket.service.impl;

import com.minimarket.entity.Rol;
import com.minimarket.repository.RolRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RolServiceImplTest {

    @Mock
    private RolRepository rolRepository;

    @InjectMocks
    private RolServiceImpl rolService;

    private Rol rolMock;

    @BeforeEach
    public void setUp() {
        // Preparamos nuestro objeto simulado
        rolMock = new Rol();
        rolMock.setId(1L);
        rolMock.setNombre("ROLE_ADMIN");
    }

    @Test
    public void testFindByNombre_Encontrado() {
        // Act: Simulamos que la base de datos encuentra el rol
        when(rolRepository.findByNombre("ROLE_ADMIN")).thenReturn(Optional.of(rolMock));

        // Ejecutamos
        Optional<Rol> resultado = rolService.findByNombre("ROLE_ADMIN");

        // Assert: Validamos que el Optional contenga el rol
        assertTrue(resultado.isPresent(), "El rol debería ser encontrado");
        assertEquals("ROLE_ADMIN", resultado.get().getNombre());
        verify(rolRepository, times(1)).findByNombre("ROLE_ADMIN");
    }

    @Test
    public void testFindByNombre_NoEncontrado() {
        // Act: Simulamos que la base de datos no encuentra el rol
        when(rolRepository.findByNombre("ROLE_USER")).thenReturn(Optional.empty());

        // Ejecutamos
        Optional<Rol> resultado = rolService.findByNombre("ROLE_USER");

        // Assert: Validamos que el Optional esté vacío
        assertFalse(resultado.isPresent(), "El rol no debería ser encontrado");
        verify(rolRepository, times(1)).findByNombre("ROLE_USER");
    }
}