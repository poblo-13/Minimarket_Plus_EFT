package com.minimarket.abastecimiento;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdenCompraConsultaServiceTest {
    @Mock private OrdenCompraRepository ordenCompraRepository;
    @InjectMocks private OrdenCompraConsultaService service;

    @Test
    void listaLasOrdenesPersistidas() {
        List<OrdenCompra> ordenes = List.of(new OrdenCompra());
        when(ordenCompraRepository.findAll()).thenReturn(ordenes);

        assertSame(ordenes, service.listar());
    }
}
