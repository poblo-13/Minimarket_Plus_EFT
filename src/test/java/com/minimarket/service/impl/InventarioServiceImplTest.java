package com.minimarket.service.impl;

import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import com.minimarket.exception.InsufficientStockException;
import com.minimarket.repository.InventarioRepository;
import com.minimarket.repository.ProductoRepository;
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
class InventarioServiceImplTest {

    @Mock private InventarioRepository inventarioRepository;
    @Mock private ProductoRepository productoRepository;
    @InjectMocks private InventarioServiceImpl inventarioService;

    private Producto producto;

    @BeforeEach
    void setUp() {
        producto = new Producto();
        producto.setId(2L);
        producto.setStock(10);
    }

    @Test
    void saveLocksProductAndRecordsEntrada() {
        Inventario movement = movement("Entrada", 5);
        when(productoRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(producto));
        when(inventarioRepository.save(movement)).thenReturn(movement);

        inventarioService.save(movement);

        assertEquals(15, producto.getStock());
        assertSame(producto, movement.getProducto());
        verify(productoRepository).findByIdForUpdate(2L);
        verify(productoRepository).save(producto);
        verify(inventarioRepository).save(movement);
    }

    @Test
    void saveRejectsSalidaWhenLockedStockIsInsufficient() {
        Inventario movement = movement("Salida", 11);
        when(productoRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(producto));

        assertThrows(InsufficientStockException.class, () -> inventarioService.save(movement));

        assertEquals(10, producto.getStock());
        verify(inventarioRepository, never()).save(any());
        verify(productoRepository, never()).save(any());
    }

    @Test
    void sequentialSalidasUseLockedCurrentStock() {
        Inventario first = movement("Salida", 6);
        Inventario second = movement("Salida", 5);
        when(productoRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(producto));
        when(inventarioRepository.save(first)).thenReturn(first);

        inventarioService.save(first);

        assertEquals(4, producto.getStock());
        assertThrows(InsufficientStockException.class, () -> inventarioService.save(second));
        assertEquals(4, producto.getStock());
        verify(inventarioRepository).save(first);
        verify(inventarioRepository, never()).save(second);
    }

    @Test
    void existingMovementCannotBeUpdated() {
        Inventario movement = movement("Entrada", 1);
        movement.setId(15L);

        assertThrows(UnsupportedOperationException.class, () -> inventarioService.save(movement));
        verifyNoInteractions(productoRepository, inventarioRepository);
    }

    @Test
    void movementsCannotBeDeleted() {
        assertThrows(UnsupportedOperationException.class, () -> inventarioService.deleteById(15L));
        verifyNoInteractions(productoRepository, inventarioRepository);
    }

    private Inventario movement(String type, int quantity) {
        Inventario movement = new Inventario();
        Producto requestProduct = new Producto();
        requestProduct.setId(2L);
        movement.setProducto(requestProduct);
        movement.setTipoMovimiento(type);
        movement.setCantidad(quantity);
        return movement;
    }
}
