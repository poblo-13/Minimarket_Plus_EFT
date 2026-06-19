package com.minimarket.service.impl;

import com.minimarket.entity.Usuario;
import com.minimarket.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private Usuario usuarioMock;

    @BeforeEach
    public void setUp() {
        // Preparamos a nuestro jugador simulado
        usuarioMock = new Usuario();
        usuarioMock.setId(1L);
        usuarioMock.setUsername("goleador99");
        usuarioMock.setPassword("claveSegura123");
    }

    @Test
    public void testFindAll() {
        // Act
        when(usuarioRepository.findAll()).thenReturn(Arrays.asList(usuarioMock));

        // Ejecutamos
        List<Usuario> resultado = usuarioService.findAll();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("goleador99", resultado.get(0).getUsername());
        verify(usuarioRepository, times(1)).findAll();
    }

    @Test
    public void testFindById_Encontrado() {
        // Act
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));

        // Ejecutamos
        Optional<Usuario> resultado = usuarioService.findById(1L);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getId());
        verify(usuarioRepository, times(1)).findById(1L);
    }

    @Test
    public void testFindById_NoEncontrado() {
        // Act
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        // Ejecutamos
        Optional<Usuario> resultado = usuarioService.findById(99L);

        // Assert
        assertFalse(resultado.isPresent());
        verify(usuarioRepository, times(1)).findById(99L);
    }

    @Test
    public void testFindByUsername_Encontrado() {
        // Act
        when(usuarioRepository.findByUsername("goleador99")).thenReturn(Optional.of(usuarioMock));

        // Ejecutamos
        Optional<Usuario> resultado = usuarioService.findByUsername("goleador99");

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("goleador99", resultado.get().getUsername());
        verify(usuarioRepository, times(1)).findByUsername("goleador99");
    }

    @Test
    public void testFindByUsername_NoEncontrado() {
        // Act
        when(usuarioRepository.findByUsername("fantasma")).thenReturn(Optional.empty());

        // Ejecutamos
        Optional<Usuario> resultado = usuarioService.findByUsername("fantasma");

        // Assert
        assertFalse(resultado.isPresent());
        verify(usuarioRepository, times(1)).findByUsername("fantasma");
    }

    @Test
    public void testSave() {
        // Act
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioMock);

        // Ejecutamos
        Usuario resultado = usuarioService.save(usuarioMock);

        // Assert
        assertNotNull(resultado);
        assertEquals("goleador99", resultado.getUsername());
        verify(usuarioRepository, times(1)).save(usuarioMock);
    }

    @Test
    public void testDeleteById() {
        // Act
        usuarioService.deleteById(1L);

        // Assert
        verify(usuarioRepository, times(1)).deleteById(1L);
    }
}