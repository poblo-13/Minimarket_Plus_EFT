package com.minimarket.service.impl;

import com.minimarket.entity.Inventario;
import com.minimarket.repository.InventarioRepository;
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
public class InventarioServiceImplTest {

    @Mock
    private InventarioRepository inventarioRepository;

    @InjectMocks
    private InventarioServiceImpl inventarioService;

    private Inventario inventarioMock;

    @BeforeEach
    public void setUp() {
        // Preparamos nuestro objeto simulado para las pruebas
        inventarioMock = new Inventario();
        inventarioMock.setId(15L);
        inventarioMock.setCantidad(50);
        inventarioMock.setTipoMovimiento("Entrada");
    }

    @Test
    public void testFindAll() {
        // Act
        when(inventarioRepository.findAll()).thenReturn(Arrays.asList(inventarioMock));

        // Ejecutamos
        List<Inventario> resultado = inventarioService.findAll();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(15L, resultado.get(0).getId());
        verify(inventarioRepository, times(1)).findAll();
    }

    @Test
    public void testFindById_Encontrado() {
        // Act
        when(inventarioRepository.findById(15L)).thenReturn(Optional.of(inventarioMock));

        // Ejecutamos
        Inventario resultado = inventarioService.findById(15L);

        // Assert
        assertNotNull(resultado);
        assertEquals("Entrada", resultado.getTipoMovimiento());
        verify(inventarioRepository, times(1)).findById(15L);
    }

    @Test
    public void testFindById_NoEncontrado() {
        // Act
        when(inventarioRepository.findById(99L)).thenReturn(Optional.empty());

        // Ejecutamos
        Inventario resultado = inventarioService.findById(99L);

        // Assert
        assertNull(resultado);
        verify(inventarioRepository, times(1)).findById(99L);
    }

    @Test
    public void testSave() {
        // Act
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventarioMock);

        // Ejecutamos
        Inventario resultado = inventarioService.save(inventarioMock);

        // Assert
        assertNotNull(resultado);
        assertEquals(50, resultado.getCantidad());
        verify(inventarioRepository, times(1)).save(inventarioMock);
    }

    @Test
    public void testDeleteById() {
        // Act
        inventarioService.deleteById(15L);

        // Assert
        verify(inventarioRepository, times(1)).deleteById(15L);
    }

    @Test
    public void testFindByProductoId() {
        // Act
        when(inventarioRepository.findByProductoId(2L)).thenReturn(Arrays.asList(inventarioMock));

        // Ejecutamos
        List<Inventario> resultado = inventarioService.findByProductoId(2L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(inventarioRepository, times(1)).findByProductoId(2L);
    }
}