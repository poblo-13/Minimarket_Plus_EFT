package com.minimarket.service.impl;

import com.minimarket.api.dto.LineaVentaRequest;
import com.minimarket.api.dto.VentaRequest;
import com.minimarket.entity.Categoria;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.entity.Venta;
import com.minimarket.exception.InsufficientStockException;
import com.minimarket.repository.CategoriaRepository;
import com.minimarket.repository.InventarioRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.repository.VentaRepository;
import com.minimarket.service.VentaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

/** Uses H2/JPA to verify that the aggregate transaction reaches stock and movement persistence. */
@SpringBootTest
@ActiveProfiles("dev")
class VentaAggregateIntegrationTest {
    @Autowired VentaService ventaService;
    @Autowired VentaRepository ventaRepository;
    @Autowired ProductoRepository productoRepository;
    @Autowired CategoriaRepository categoriaRepository;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired InventarioRepository inventarioRepository;

    @Test void snapshotsServerPriceAndAggregatesDuplicateLinesIntoOneOutput() {
        Usuario user = user();
        Producto product = product(10, 1250D);
        long initialMovements = inventarioRepository.findByProductoId(product.getId()).size();

        Venta sale = ventaService.registrar(new VentaRequest(user.getId(), List.of(
                new LineaVentaRequest(product.getId(), 2), new LineaVentaRequest(product.getId(), 3))));

        assertEquals(1, sale.getDetalles().size());
        assertEquals(5, sale.getDetalles().getFirst().getCantidad());
        assertEquals(1250D, sale.getDetalles().getFirst().getPrecio());
        assertEquals(5, productoRepository.findById(product.getId()).orElseThrow().getStock());
        assertEquals(initialMovements + 1, inventarioRepository.findByProductoId(product.getId()).size());
        assertEquals("Salida", inventarioRepository.findByProductoId(product.getId()).getLast().getTipoMovimiento());
    }

    @Test void insufficientStockRollsBackSaleDetailsMovementAndStock() {
        Usuario user = user();
        Producto product = product(2, 500D);
        long salesBefore = ventaRepository.count();
        long movementsBefore = inventarioRepository.findByProductoId(product.getId()).size();

        assertThrows(InsufficientStockException.class, () -> ventaService.registrar(new VentaRequest(user.getId(),
                List.of(new LineaVentaRequest(product.getId(), 3)))));

        assertEquals(salesBefore, ventaRepository.count());
        assertEquals(movementsBefore, inventarioRepository.findByProductoId(product.getId()).size());
        assertEquals(2, productoRepository.findById(product.getId()).orElseThrow().getStock());
    }

    @Test void competingSalesSerializeOnTheLockedProductAndCannotOversell() throws Exception {
        Producto product = product(1, 500D);
        Usuario firstUser = user();
        Usuario secondUser = user();
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Boolean> first = executor.submit(() -> registerCompetingSale(firstUser.getId(), product.getId(), ready, start));
            Future<Boolean> second = executor.submit(() -> registerCompetingSale(secondUser.getId(), product.getId(), ready, start));
            ready.await();
            start.countDown();

            assertEquals(1, (first.get() ? 1 : 0) + (second.get() ? 1 : 0));
            assertEquals(0, productoRepository.findById(product.getId()).orElseThrow().getStock());
            assertEquals(1, inventarioRepository.findByProductoId(product.getId()).size());
        } finally {
            executor.shutdownNow();
        }
    }

    private boolean registerCompetingSale(Long userId, Long productId, CountDownLatch ready, CountDownLatch start)
            throws InterruptedException {
        ready.countDown();
        start.await();
        try {
            ventaService.registrar(new VentaRequest(userId, List.of(new LineaVentaRequest(productId, 1))));
            return true;
        } catch (InsufficientStockException expected) {
            return false;
        }
    }

    private Usuario user() {
        Usuario user = new Usuario();
        user.setUsername("sale-user-" + System.nanoTime());
        user.setPassword("password");
        user.setRoles(java.util.Set.of());
        return usuarioRepository.save(user);
    }

    private Producto product(int stock, double price) {
        Categoria category = new Categoria();
        category.setNombre("sale-category-" + System.nanoTime());
        category = categoriaRepository.save(category);
        Producto product = new Producto();
        product.setNombre("sale-product-" + System.nanoTime());
        product.setPrecio(price);
        product.setStock(stock);
        product.setCategoria(category);
        return productoRepository.save(product);
    }
}
