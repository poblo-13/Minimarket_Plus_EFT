package com.minimarket.service.impl;

import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import com.minimarket.exception.InsufficientStockException;
import com.minimarket.repository.InventarioRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.sucursal.StockSucursal;
import com.minimarket.sucursal.StockSucursalService;
import com.minimarket.sucursal.Sucursal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioServiceImplTest {
    @Mock InventarioRepository inventarioRepository;
    @Mock ProductoRepository productoRepository;
    @Mock StockSucursalService stockSucursalService;
    @InjectMocks InventarioServiceImpl inventarioService;
    private Producto producto;
    private Sucursal sucursal;
    private StockSucursal stock;

    @BeforeEach void setUp() {
        producto = new Producto(); producto.setId(2L); producto.setStock(99);
        sucursal = new Sucursal(); sucursal.setId(3L); sucursal.setNombre("Centro");
        stock = new StockSucursal(); stock.setProducto(producto); stock.setSucursal(sucursal); stock.setDisponible(10); stock.setStockMinimo(0);
    }

    @Test void saveRegistersEntradaAndIncreasesBranchAvailability() {
        Inventario movement = movement("Entrada", 5);
        doAnswer(invocation -> { stock.aumentar(invocation.getArgument(2)); return stock; })
                .when(stockSucursalService).aplicarEntrada(3L, 2L, 5);
        when(inventarioRepository.save(movement)).thenReturn(movement);
        inventarioService.save(movement);
        assertEquals(15, stock.getDisponible());
        assertEquals(99, producto.getStock());
        verify(stockSucursalService).aplicarEntrada(3L, 2L, 5);
        verify(inventarioRepository).save(movement);
    }

    @Test void saveRejectsSalidaWhenBranchStockIsInsufficient() {
        Inventario movement = movement("Salida", 11);
        doThrow(new InsufficientStockException()).when(stockSucursalService).aplicarSalidaAdministrada(3L, 2L, 11);
        assertThrows(InsufficientStockException.class, () -> inventarioService.save(movement));
        assertEquals(10, stock.getDisponible());
        verify(inventarioRepository, never()).save(any());
    }

    @Test void sequentialSalidasUseSameBranchStock() {
        Inventario first = movement("Salida", 6), second = movement("Salida", 5);
        doAnswer(invocation -> { stock.disminuir(invocation.getArgument(2)); return stock; })
                .when(stockSucursalService).aplicarSalidaAdministrada(3L, 2L, 6);
        doThrow(new InsufficientStockException()).when(stockSucursalService).aplicarSalidaAdministrada(3L, 2L, 5);
        when(inventarioRepository.save(first)).thenReturn(first);
        inventarioService.save(first);
        assertEquals(4, stock.getDisponible());
        assertThrows(InsufficientStockException.class, () -> inventarioService.save(second));
        assertEquals(4, stock.getDisponible());
        verify(inventarioRepository).save(first);
        verify(inventarioRepository, never()).save(second);
    }

    @Test void existingMovementCannotBeUpdated() {
        Inventario movement = movement("Entrada", 1); movement.setId(15L);
        assertThrows(UnsupportedOperationException.class, () -> inventarioService.save(movement));
        verifyNoInteractions(productoRepository, inventarioRepository, stockSucursalService);
    }
    @Test void movementsCannotBeDeleted() { assertThrows(UnsupportedOperationException.class, () -> inventarioService.deleteById(15L)); }
    private Inventario movement(String type, int quantity) {
        Inventario movement = new Inventario(); movement.setProducto(producto); movement.setSucursal(sucursal);
        movement.setTipoMovimiento(type); movement.setCantidad(quantity); return movement;
    }
}
