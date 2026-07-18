package com.minimarket.service.impl;

import com.minimarket.abastecimiento.Proveedor;
import com.minimarket.abastecimiento.ProveedorRepository;
import com.minimarket.api.dto.LineaVentaRequest;
import com.minimarket.api.dto.VentaRequest;
import com.minimarket.entity.Categoria;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.entity.Venta;
import com.minimarket.repository.CategoriaRepository;
import com.minimarket.repository.InventarioRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.repository.VentaRepository;
import com.minimarket.service.VentaService;
import com.minimarket.sucursal.StockSucursal;
import com.minimarket.sucursal.StockSucursalRepository;
import com.minimarket.sucursal.Sucursal;
import com.minimarket.sucursal.SucursalRepository;
import com.minimarket.sucursal.exception.StockSucursalInsuficienteException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class VentaAggregateIntegrationTest {
    @Autowired VentaService ventaService;
    @Autowired VentaRepository ventaRepository;
    @Autowired ProductoRepository productoRepository;
    @Autowired CategoriaRepository categoriaRepository;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired InventarioRepository inventarioRepository;
    @Autowired SucursalRepository sucursalRepository;
    @Autowired StockSucursalRepository stockSucursalRepository;
    @Autowired ProveedorRepository proveedorRepository;

    @Test void snapshotsServerPriceAndAggregatesDuplicateLinesIntoOneBranchOutput() {
        Usuario user = user(); Fixture f = fixture(10, 1250D);
        Venta sale = ventaService.registrar(new VentaRequest(user.getId(), f.sucursal().getId(), List.of(
                new LineaVentaRequest(f.producto().getId(), 2), new LineaVentaRequest(f.producto().getId(), 3))));
        assertEquals(1, sale.getDetalles().size());
        assertEquals(5, sale.getDetalles().getFirst().getCantidad());
        assertEquals(1250D, sale.getDetalles().getFirst().getPrecio());
        assertEquals(5, stockSucursalRepository.findById(f.stock().getId()).orElseThrow().getDisponible());
        assertEquals(99, productoRepository.findById(f.producto().getId()).orElseThrow().getStock());
        assertEquals(1, inventarioRepository.findByProductoId(f.producto().getId()).size());
    }

    @Test void insufficientBranchStockRollsBackSaleMovementAndStock() {
        Usuario user = user(); Fixture f = fixture(2, 500D); long salesBefore = ventaRepository.count();
        assertThrows(StockSucursalInsuficienteException.class, () -> ventaService.registrar(new VentaRequest(user.getId(), f.sucursal().getId(), List.of(new LineaVentaRequest(f.producto().getId(), 3)))));
        assertEquals(salesBefore, ventaRepository.count());
        assertEquals(0, inventarioRepository.findByProductoId(f.producto().getId()).size());
        assertEquals(2, stockSucursalRepository.findById(f.stock().getId()).orElseThrow().getDisponible());
    }

    @Test void competingSalesShareSameBranchStockAndCannotOversell() throws Exception {
        Fixture f = fixture(1, 500D); CountDownLatch ready = new CountDownLatch(2), start = new CountDownLatch(1); ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Boolean> a = executor.submit(() -> sell(user().getId(), f, ready, start));
            Future<Boolean> b = executor.submit(() -> sell(user().getId(), f, ready, start));
            ready.await(); start.countDown();
            assertEquals(1, (a.get() ? 1 : 0) + (b.get() ? 1 : 0));
            assertEquals(0, stockSucursalRepository.findById(f.stock().getId()).orElseThrow().getDisponible());
            assertEquals(1, inventarioRepository.findByProductoId(f.producto().getId()).size());
        } finally { executor.shutdownNow(); }
    }

    private boolean sell(Long userId, Fixture f, CountDownLatch ready, CountDownLatch start) throws InterruptedException {
        ready.countDown(); start.await();
        try { ventaService.registrar(new VentaRequest(userId, f.sucursal().getId(), List.of(new LineaVentaRequest(f.producto().getId(), 1)))); return true; }
        catch (StockSucursalInsuficienteException expected) { return false; }
    }
    private Usuario user() { Usuario u = new Usuario(); u.setUsername("sale-user-" + System.nanoTime()); u.setPassword("password"); u.setRoles(java.util.Set.of()); return usuarioRepository.save(u); }
    private Fixture fixture(int available, double price) {
        Categoria c = new Categoria(); c.setNombre("sale-category-" + System.nanoTime()); c = categoriaRepository.save(c);
        Proveedor supplier = new Proveedor(); supplier.setNombre("sale-supplier-" + System.nanoTime()); supplier = proveedorRepository.save(supplier);
        Producto p = new Producto(); p.setNombre("sale-product-" + System.nanoTime()); p.setPrecio(price); p.setStock(99); p.setCategoria(c); p.setProveedorReposicion(supplier); p = productoRepository.save(p);
        Sucursal s = new Sucursal(); s.setNombre("sale-sucursal-" + System.nanoTime()); s = sucursalRepository.save(s);
        StockSucursal stock = new StockSucursal(); stock.setSucursal(s); stock.setProducto(p); stock.setDisponible(available); stock.setStockMinimo(0); stock = stockSucursalRepository.saveAndFlush(stock);
        return new Fixture(p, s, stock);
    }
    private record Fixture(Producto producto, Sucursal sucursal, StockSucursal stock) { }
}
