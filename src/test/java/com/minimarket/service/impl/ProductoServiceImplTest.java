package com.minimarket.service.impl;

import com.minimarket.entity.Producto;
import com.minimarket.repository.ProductoRepository;
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
public class ProductoServiceImplTest {

    // Simulamos la base de datos
    @Mock
    private ProductoRepository productoRepository;

    // Inyectamos el simulacro en el servicio real
    @InjectMocks
    private ProductoServiceImpl productoService;

    private Producto productoMock;

    @BeforeEach
    public void setUp() {
        // Preparamos nuestro jugador estrella para las jugadas
        productoMock = new Producto();
        productoMock.setId(1L);
        productoMock.setNombre("Bebida Energética");
        productoMock.setPrecio(2500.0);
        productoMock.setStock(50);
    }

    @Test
    public void testFindAll() {
        // Act: Le decimos al simulacro qué devolver
        when(productoRepository.findAll()).thenReturn(Arrays.asList(productoMock));

        // Ejecutamos la jugada
        List<Producto> resultado = productoService.findAll();

        // Assert: Validamos
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Bebida Energética", resultado.get(0).getNombre());
        verify(productoRepository, times(1)).findAll();
    }

    @Test
    public void testFindById_Encontrado() {
        // Act
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoMock));

        // Ejecutamos
        Producto resultado = productoService.findById(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(productoRepository, times(1)).findById(1L);
    }

    @Test
    public void testFindById_NoEncontrado() {
        // Act: Simulamos que la base de datos no encuentra el ID
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        // Ejecutamos
        Producto resultado = productoService.findById(99L);

        // Assert: El servicio debería devolver null
        assertNull(resultado);
        verify(productoRepository, times(1)).findById(99L);
    }

    @Test
    public void testSave() {
        // Act
        when(productoRepository.save(any(Producto.class))).thenReturn(productoMock);

        // Ejecutamos
        Producto resultado = productoService.save(productoMock);

        // Assert
        assertNotNull(resultado);
        assertEquals("Bebida Energética", resultado.getNombre());
        verify(productoRepository, times(1)).save(productoMock);
    }

    @Test
    public void testDeleteById() {
        // Act: Ejecutamos directamente porque deleteById no devuelve nada (void)
        productoService.deleteById(1L);

        // Assert: Verificamos que el repositorio efectivamente llamó al método deleteById una vez
        verify(productoRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testFindByCategoriaId() {
        // Act
        when(productoRepository.findByCategoriaId(5L)).thenReturn(Arrays.asList(productoMock));

        // Ejecutamos
        List<Producto> resultado = productoService.findByCategoriaId(5L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(productoRepository, times(1)).findByCategoriaId(5L);
    }
}