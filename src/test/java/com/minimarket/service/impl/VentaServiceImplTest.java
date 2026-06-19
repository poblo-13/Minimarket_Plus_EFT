package com.minimarket.service.impl;

import com.minimarket.entity.Venta;
import com.minimarket.repository.VentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VentaServiceImplTest {

    @Mock
    private VentaRepository ventaRepository;

    @InjectMocks
    private VentaServiceImpl ventaService;

    private Venta ventaMock;
    private Date fechaPrueba;

    @BeforeEach
    public void setUp() {
        // Preparamos la venta simulada
        fechaPrueba = new Date();
        ventaMock = new Venta();
        ventaMock.setId(100L);
        ventaMock.setFecha(fechaPrueba);
    }

    @Test
    public void testFindAll() {
        // Act
        when(ventaRepository.findAll()).thenReturn(Arrays.asList(ventaMock));

        // Ejecutamos
        List<Venta> resultado = ventaService.findAll();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(100L, resultado.get(0).getId());
        verify(ventaRepository, times(1)).findAll();
    }

    @Test
    public void testFindById_Encontrada() {
        // Act
        when(ventaRepository.findById(100L)).thenReturn(Optional.of(ventaMock));

        // Ejecutamos
        Venta resultado = ventaService.findById(100L);

        // Assert
        assertNotNull(resultado);
        assertEquals(100L, resultado.getId());
        assertEquals(fechaPrueba, resultado.getFecha());
        verify(ventaRepository, times(1)).findById(100L);
    }

    @Test
    public void testFindById_NoEncontrada() {
        // Act
        when(ventaRepository.findById(99L)).thenReturn(Optional.empty());

        // Ejecutamos
        Venta resultado = ventaService.findById(99L);

        // Assert
        assertNull(resultado);
        verify(ventaRepository, times(1)).findById(99L);
    }

    @Test
    public void testSave() {
        // Act
        when(ventaRepository.save(any(Venta.class))).thenReturn(ventaMock);

        // Ejecutamos
        Venta resultado = ventaService.save(ventaMock);

        // Assert
        assertNotNull(resultado);
        assertEquals(100L, resultado.getId());
        verify(ventaRepository, times(1)).save(ventaMock);
    }

    @Test
    public void testFindByUsuarioId() {
        // Act
        when(ventaRepository.findByUsuarioId(1L)).thenReturn(Arrays.asList(ventaMock));

        // Ejecutamos
        List<Venta> resultado = ventaService.findByUsuarioId(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(ventaRepository, times(1)).findByUsuarioId(1L);
    }
}