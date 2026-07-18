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

import java.util.List;
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

    @Test
    void listaTodoElStockDeUnaSucursalConVariosProductos() {
        StockSucursal primero = stock(1L, 10L, 7, 2);
        StockSucursal segundo = stock(1L, 11L, 3, 1);
        when(sucursalRepository.existsById(1L)).thenReturn(true);
        when(stockSucursalRepository.findBySucursalId(1L)).thenReturn(List.of(primero, segundo));

        List<StockSucursal> resultado = service.listarStockPorSucursal(1L);

        assertEquals(2, resultado.size());
        assertEquals(10L, resultado.getFirst().getProducto().getId());
        assertEquals(3, resultado.getLast().getDisponible());
    }

    @Test
    void sucursalExistenteSinStockDevuelveListaVacia() {
        when(sucursalRepository.existsById(1L)).thenReturn(true);
        when(stockSucursalRepository.findBySucursalId(1L)).thenReturn(List.of());

        assertEquals(List.of(), service.listarStockPorSucursal(1L));
    }

    @Test
    void sucursalAusenteAlListarStockDevuelveNoEncontrada() {
        when(sucursalRepository.existsById(99L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> service.listarStockPorSucursal(99L));
    }

    private StockSucursal stock(Long sucursalId, Long productoId, int disponible, int minimo) {
        Sucursal sucursal = new Sucursal();
        sucursal.setId(sucursalId);
        Producto producto = new Producto();
        producto.setId(productoId);
        StockSucursal stock = new StockSucursal();
        stock.setSucursal(sucursal);
        stock.setProducto(producto);
        stock.setDisponible(disponible);
        stock.setStockMinimo(minimo);
        return stock;
    }
}
