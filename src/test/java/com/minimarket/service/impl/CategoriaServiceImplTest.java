package com.minimarket.service.impl;

import com.minimarket.entity.Categoria;
import com.minimarket.repository.CategoriaRepository;
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
public class CategoriaServiceImplTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaServiceImpl categoriaService;

    private Categoria categoriaMock;

    @BeforeEach
    public void setUp() {
        // Preparamos nuestro objeto simulado
        categoriaMock = new Categoria();
        categoriaMock.setId(1L);
        categoriaMock.setNombre("Abarrotes");
    }

    @Test
    public void testFindAll() {
        // Act
        when(categoriaRepository.findAll()).thenReturn(Arrays.asList(categoriaMock));

        // Ejecutamos
        List<Categoria> resultado = categoriaService.findAll();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Abarrotes", resultado.get(0).getNombre());
        verify(categoriaRepository, times(1)).findAll();
    }

    @Test
    public void testFindById_Encontrado() {
        // Act
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaMock));

        // Ejecutamos
        Categoria resultado = categoriaService.findById(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(categoriaRepository, times(1)).findById(1L);
    }

    @Test
    public void testFindById_NoEncontrado() {
        // Act
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        // Ejecutamos
        Categoria resultado = categoriaService.findById(99L);

        // Assert
        assertNull(resultado);
        verify(categoriaRepository, times(1)).findById(99L);
    }

    @Test
    public void testSave() {
        // Act
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoriaMock);

        // Ejecutamos
        Categoria resultado = categoriaService.save(categoriaMock);

        // Assert
        assertNotNull(resultado);
        assertEquals("Abarrotes", resultado.getNombre());
        verify(categoriaRepository, times(1)).save(categoriaMock);
    }

    @Test
    public void testDeleteById() {
        // Act
        categoriaService.deleteById(1L);

        // Assert
        verify(categoriaRepository, times(1)).deleteById(1L);
    }
}