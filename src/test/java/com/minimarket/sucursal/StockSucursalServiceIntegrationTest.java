package com.minimarket.sucursal;

import com.minimarket.abastecimiento.OrdenCompra;
import com.minimarket.abastecimiento.OrdenCompraRepository;
import com.minimarket.abastecimiento.Proveedor;
import com.minimarket.abastecimiento.ProveedorRepository;
import com.minimarket.entity.Categoria;
import com.minimarket.entity.Producto;
import com.minimarket.repository.CategoriaRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.sucursal.exception.StockSucursalInsuficienteException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

/** H2/JPA coverage for branch availability, replenishment and stock concurrency. */
@SpringBootTest
@ActiveProfiles("dev")
class StockSucursalServiceIntegrationTest {
    @Autowired StockSucursalService stockSucursalService;
    @Autowired StockSucursalRepository stockSucursalRepository;
    @Autowired SucursalRepository sucursalRepository;
    @Autowired OrdenCompraRepository ordenCompraRepository;
    @Autowired ProveedorRepository proveedorRepository;
    @Autowired CategoriaRepository categoriaRepository;
    @Autowired ProductoRepository productoRepository;

    @Test
    void consultaDisponibilidadYEntradaNoTocanStockGlobalLegado() {
        Fixture fixture = fixture(4, 2);

        assertEquals(4, stockSucursalService.consultarDisponibilidad(fixture.sucursal().getId(), fixture.producto().getId()));
        stockSucursalService.aplicarEntrada(fixture.sucursal().getId(), fixture.producto().getId(), 3);

        assertEquals(7, stockSucursalService.consultarDisponibilidad(fixture.sucursal().getId(), fixture.producto().getId()));
        assertEquals(99, productoRepository.findById(fixture.producto().getId()).orElseThrow().getStock());
    }

    @Test
    void salidaEnMinimoCreaOrdenAbierta() {
        Fixture fixture = fixture(3, 2);

        stockSucursalService.aplicarSalida(fixture.sucursal().getId(), fixture.producto().getId(), fixture.proveedor().getId(), 1);

        OrdenCompra orden = ordenCompraRepository.findAll().stream()
                .filter(candidate -> candidate.getSucursal().getId().equals(fixture.sucursal().getId()))
                .findFirst().orElseThrow();
        assertEquals(2, stockSucursalService.consultarDisponibilidad(fixture.sucursal().getId(), fixture.producto().getId()));
        assertEquals(com.minimarket.abastecimiento.EstadoOrdenCompra.ABIERTA, orden.getEstado());
        assertEquals(1, orden.getCantidadSolicitada());
    }

    @Test
    void noDuplicaOrdenAbiertaParaMismaSucursalProductoYProveedor() {
        Fixture fixture = fixture(3, 2);

        stockSucursalService.aplicarSalida(fixture.sucursal().getId(), fixture.producto().getId(), fixture.proveedor().getId(), 1);
        stockSucursalService.aplicarSalida(fixture.sucursal().getId(), fixture.producto().getId(), fixture.proveedor().getId(), 1);

        assertEquals(1, ordenCompraRepository.findAll().stream()
                .filter(order -> order.getSucursal().getId().equals(fixture.sucursal().getId()))
                .count());
    }

    @Test
    void rechazaSalidaConStockInsuficienteYCantidadNoPositiva() {
        Fixture fixture = fixture(1, 0);

        assertThrows(StockSucursalInsuficienteException.class, () -> stockSucursalService.aplicarSalida(
                fixture.sucursal().getId(), fixture.producto().getId(), fixture.proveedor().getId(), 2));
        assertThrows(IllegalArgumentException.class, () -> stockSucursalService.aplicarEntrada(
                fixture.sucursal().getId(), fixture.producto().getId(), 0));
        assertEquals(1, stockSucursalService.consultarDisponibilidad(fixture.sucursal().getId(), fixture.producto().getId()));
    }

    @Test
    void salidasConcurrentesSeSerializanYVersionProtegeActualizacionesObsoletas() throws Exception {
        Fixture fixture = fixture(1, 0);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Boolean> first = executor.submit(() -> salidaConcurrente(fixture, ready, start));
            Future<Boolean> second = executor.submit(() -> salidaConcurrente(fixture, ready, start));
            ready.await();
            start.countDown();

            assertEquals(1, (first.get() ? 1 : 0) + (second.get() ? 1 : 0));
            StockSucursal persisted = stockSucursalRepository.findById(fixture.stock().getId()).orElseThrow();
            assertEquals(0, persisted.getDisponible());
            assertEquals(1L, persisted.getVersion());

            StockSucursal stale = stockSucursalRepository.findById(fixture.stock().getId()).orElseThrow();
            StockSucursal current = stockSucursalRepository.findById(fixture.stock().getId()).orElseThrow();
            current.setDisponible(2);
            stockSucursalRepository.saveAndFlush(current);
            stale.setDisponible(3);
            assertThrows(OptimisticLockingFailureException.class, () -> stockSucursalRepository.saveAndFlush(stale));
        } finally {
            executor.shutdownNow();
        }
    }

    private boolean salidaConcurrente(Fixture fixture, CountDownLatch ready, CountDownLatch start) throws InterruptedException {
        ready.countDown();
        start.await();
        try {
            stockSucursalService.aplicarSalida(fixture.sucursal().getId(), fixture.producto().getId(), fixture.proveedor().getId(), 1);
            return true;
        } catch (StockSucursalInsuficienteException expected) {
            return false;
        }
    }

    private Fixture fixture(int disponible, int minimo) {
        Categoria categoria = new Categoria();
        categoria.setNombre("categoria-sucursal-" + System.nanoTime());
        categoria = categoriaRepository.save(categoria);
        Producto producto = new Producto();
        producto.setNombre("producto-sucursal-" + System.nanoTime());
        producto.setPrecio(100D);
        producto.setStock(99);
        producto.setCategoria(categoria);
        producto = productoRepository.save(producto);
        Sucursal sucursal = new Sucursal();
        sucursal.setNombre("sucursal-" + System.nanoTime());
        sucursal = sucursalRepository.save(sucursal);
        Proveedor proveedor = new Proveedor();
        proveedor.setNombre("proveedor-" + System.nanoTime());
        proveedor = proveedorRepository.save(proveedor);
        StockSucursal stock = new StockSucursal();
        stock.setSucursal(sucursal);
        stock.setProducto(producto);
        stock.setDisponible(disponible);
        stock.setStockMinimo(minimo);
        stock = stockSucursalRepository.saveAndFlush(stock);
        return new Fixture(sucursal, producto, proveedor, stock);
    }

    private record Fixture(Sucursal sucursal, Producto producto, Proveedor proveedor, StockSucursal stock) { }
}
