package com.minimarket.service.impl;

import com.minimarket.entity.Carrito;
import com.minimarket.repository.CarritoRepository;
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
public class CarritoServiceImplTest {

    @Mock
    private CarritoRepository carritoRepository;

    @InjectMocks
    private CarritoServiceImpl carritoService;

    private Carrito carritoMock;

    @BeforeEach
    public void setUp() {
        // Preparamos nuestro objeto simulado para las jugadas
        carritoMock = new Carrito();
        carritoMock.setId(10L);
        carritoMock.setCantidad(2);
    }

    @Test
    public void testFindAll() {
        // Act
        when(carritoRepository.findAll()).thenReturn(Arrays.asList(carritoMock));

        // Ejecutamos
        List<Carrito> resultado = carritoService.findAll();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(10L, resultado.get(0).getId());
        verify(carritoRepository, times(1)).findAll();
    }

    @Test
    public void testFindById_Encontrado() {
        // Act
        when(carritoRepository.findById(10L)).thenReturn(Optional.of(carritoMock));

        // Ejecutamos
        Carrito resultado = carritoService.findById(10L);

        // Assert
        assertNotNull(resultado);
        assertEquals(10L, resultado.getId());
        verify(carritoRepository, times(1)).findById(10L);
    }

    @Test
    public void testFindById_NoEncontrado() {
        // Act
        when(carritoRepository.findById(99L)).thenReturn(Optional.empty());

        // Ejecutamos
        Carrito resultado = carritoService.findById(99L);

        // Assert
        assertNull(resultado);
        verify(carritoRepository, times(1)).findById(99L);
    }

    @Test
    public void testSave() {
        // Act
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carritoMock);

        // Ejecutamos
        Carrito resultado = carritoService.save(carritoMock);

        // Assert
        assertNotNull(resultado);
        assertEquals(10L, resultado.getId());
        assertEquals(2, resultado.getCantidad());
        verify(carritoRepository, times(1)).save(carritoMock);
    }

    @Test
    public void testDeleteById() {
        // Act
        carritoService.deleteById(10L);

        // Assert
        verify(carritoRepository, times(1)).deleteById(10L);
    }

    @Test
    public void testFindByUsuarioId() {
        // Act
        when(carritoRepository.findByUsuarioId(5L)).thenReturn(Arrays.asList(carritoMock));

        // Ejecutamos
        List<Carrito> resultado = carritoService.findByUsuarioId(5L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(carritoRepository, times(1)).findByUsuarioId(5L);
    }
}