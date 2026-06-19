package com.minimarket.service.impl;

import com.minimarket.entity.DetalleVenta;
import com.minimarket.repository.DetalleVentaRepository;
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
public class DetalleVentaServiceImplTest {

    @Mock
    private DetalleVentaRepository detalleVentaRepository;

    @InjectMocks
    private DetalleVentaServiceImpl detalleVentaService;

    private DetalleVenta detalleVentaMock;

    @BeforeEach
    public void setUp() {
        // Preparamos nuestro objeto simulado
        detalleVentaMock = new DetalleVenta();
        detalleVentaMock.setId(5L);
        detalleVentaMock.setCantidad(3);
        detalleVentaMock.setPrecio(1500.0);
    }

    @Test
    public void testFindAll() {
        // Act
        when(detalleVentaRepository.findAll()).thenReturn(Arrays.asList(detalleVentaMock));

        // Ejecutamos
        List<DetalleVenta> resultado = detalleVentaService.findAll();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(5L, resultado.get(0).getId());
        verify(detalleVentaRepository, times(1)).findAll();
    }

    @Test
    public void testFindById_Encontrado() {
        // Act
        when(detalleVentaRepository.findById(5L)).thenReturn(Optional.of(detalleVentaMock));

        // Ejecutamos
        DetalleVenta resultado = detalleVentaService.findById(5L);

        // Assert
        assertNotNull(resultado);
        assertEquals(5L, resultado.getId());
        verify(detalleVentaRepository, times(1)).findById(5L);
    }

    @Test
    public void testFindById_NoEncontrado() {
        // Act
        when(detalleVentaRepository.findById(99L)).thenReturn(Optional.empty());

        // Ejecutamos
        DetalleVenta resultado = detalleVentaService.findById(99L);

        // Assert
        assertNull(resultado);
        verify(detalleVentaRepository, times(1)).findById(99L);
    }

    @Test
    public void testSave() {
        // Act
        when(detalleVentaRepository.save(any(DetalleVenta.class))).thenReturn(detalleVentaMock);

        // Ejecutamos
        DetalleVenta resultado = detalleVentaService.save(detalleVentaMock);

        // Assert
        assertNotNull(resultado);
        assertEquals(1500.0, resultado.getPrecio());
        verify(detalleVentaRepository, times(1)).save(detalleVentaMock);
    }

    @Test
    public void testDeleteById() {
        // Act
        detalleVentaService.deleteById(5L);

        // Assert
        verify(detalleVentaRepository, times(1)).deleteById(5L);
    }

    @Test
    public void testFindByVentaId() {
        // Act
        when(detalleVentaRepository.findByVentaId(20L)).thenReturn(Arrays.asList(detalleVentaMock));

        // Ejecutamos
        List<DetalleVenta> resultado = detalleVentaService.findByVentaId(20L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(detalleVentaRepository, times(1)).findByVentaId(20L);
    }
}