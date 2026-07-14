package com.minimarket.service.impl;

import com.minimarket.repository.DetalleVentaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class DetalleVentaServiceImplTest {
    @Mock DetalleVentaRepository repository;
    @InjectMocks DetalleVentaServiceImpl service;

    @Test void exposesReadOnlyQueries() {
        when(repository.findAll()).thenReturn(java.util.List.of());
        assertTrue(service.findAll().isEmpty());
        verify(repository).findAll();
    }
}
