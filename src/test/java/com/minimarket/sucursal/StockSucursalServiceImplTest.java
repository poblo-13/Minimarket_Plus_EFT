package com.minimarket.sucursal;

import com.minimarket.abastecimiento.OrdenCompraRepository;
import com.minimarket.abastecimiento.ProveedorRepository;
import com.minimarket.entity.Producto;
import com.minimarket.repository.ProductoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockSucursalServiceImplTest {
    @Mock private StockSucursalRepository stockSucursalRepository;
    @Mock private ProveedorRepository proveedorRepository;
    @Mock private OrdenCompraRepository ordenCompraRepository;
    @Mock private SucursalRepository sucursalRepository;
    @Mock private ProductoRepository productoRepository;
    @InjectMocks private StockSucursalServiceImpl service;

    @Test
    void consultaStockValidaIdsAntesDeConsultarDisponibilidad() {
        Sucursal sucursal = new Sucursal(); sucursal.setId(1L);
        Producto producto = new Producto(); producto.setId(2L);
        StockSucursal stock = new StockSucursal(); stock.setSucursal(sucursal); stock.setProducto(producto);
        stock.setDisponible(7); stock.setStockMinimo(2);
        when(sucursalRepository.existsById(1L)).thenReturn(true);
        when(productoRepository.existsById(2L)).thenReturn(true);
        when(stockSucursalRepository.findBySucursalIdAndProductoId(1L, 2L)).thenReturn(Optional.of(stock));

        assertEquals(7, service.consultarStock(1L, 2L).getDisponible());
    }

    @Test
    void consultaStockInexistenteSeTraduceAEntidadNoEncontrada() {
        when(sucursalRepository.existsById(99L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> service.consultarStock(99L, 2L));
    }
}
