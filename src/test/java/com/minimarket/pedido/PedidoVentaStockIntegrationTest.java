package com.minimarket.pedido;

import com.minimarket.entity.Categoria;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.pedido.api.CheckoutRequest;
import com.minimarket.pedido.domain.EstadoPedido;
import com.minimarket.pedido.domain.Pedido;
import com.minimarket.pedido.domain.TipoEntrega;
import com.minimarket.pedido.repository.PedidoRepository;
import com.minimarket.pedido.service.PedidoService;
import com.minimarket.repository.CategoriaRepository;
import com.minimarket.repository.CarritoRepository;
import com.minimarket.repository.DetalleVentaRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.repository.VentaRepository;
import com.minimarket.sucursal.StockSucursal;
import com.minimarket.sucursal.StockSucursalRepository;
import com.minimarket.sucursal.Sucursal;
import com.minimarket.sucursal.SucursalRepository;
import com.minimarket.sucursal.exception.StockSucursalInsuficienteException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PedidoVentaStockIntegrationTest {
    @Autowired PedidoService pedidoService;
    @Autowired PedidoRepository pedidoRepository;
    @Autowired VentaRepository ventaRepository;
    @Autowired DetalleVentaRepository detalleVentaRepository;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired CategoriaRepository categoriaRepository;
    @Autowired ProductoRepository productoRepository;
    @Autowired SucursalRepository sucursalRepository;
    @Autowired StockSucursalRepository stockSucursalRepository;
    @Autowired CarritoRepository carritoRepository;

    @Test
    void confirmaConSnapshotsDelPedidoCreaUnaVentaYDescuentaStockUnaVez() {
        Fixture fixture = fixture(5, 99D);
        Pedido pedido = crearPedido(fixture, 2);
        long ventasAntes = ventaRepository.count();
        fixture.producto().setPrecio(1D);
        productoRepository.saveAndFlush(fixture.producto());

        pedidoService.cambiarEstado(pedido.getId(), EstadoPedido.CONFIRMADO);

        assertEquals(EstadoPedido.CONFIRMADO, pedidoRepository.findById(pedido.getId()).orElseThrow().getEstado());
        assertEquals(ventasAntes + 1, ventaRepository.count());
        assertEquals(99D, detalleVentaRepository.findByVentaId(ventaRepository.findAll().getLast().getId())
                .getFirst().getPrecio());
        assertEquals(3, stockSucursalRepository.findById(fixture.stock().getId()).orElseThrow().getDisponible());
        assertEquals(99, productoRepository.findById(fixture.producto().getId()).orElseThrow().getStock());
    }

    @Test
    void stockInsuficienteRevierteVentaStockYEstadoPendiente() {
        Fixture fixture = fixture(2, 25D);
        Pedido pedido = crearPedido(fixture, 2);
        StockSucursal agotadoAntesDeConfirmar = stockSucursalRepository.findById(fixture.stock().getId()).orElseThrow();
        agotadoAntesDeConfirmar.setDisponible(1);
        stockSucursalRepository.saveAndFlush(agotadoAntesDeConfirmar);
        long ventasAntes = ventaRepository.count();

        assertThrows(StockSucursalInsuficienteException.class,
                () -> pedidoService.cambiarEstado(pedido.getId(), EstadoPedido.CONFIRMADO));

        assertEquals(EstadoPedido.PENDIENTE, pedidoRepository.findById(pedido.getId()).orElseThrow().getEstado());
        assertEquals(ventasAntes, ventaRepository.count());
        assertEquals(1, stockSucursalRepository.findById(fixture.stock().getId()).orElseThrow().getDisponible());
    }

    @Test
    void dobleConfirmacionConcurrenteNoDuplicaVentaNiDescuentaDosVeces() throws Exception {
        Fixture fixture = fixture(2, 10D);
        Pedido pedido = crearPedido(fixture, 1);
        long ventasAntes = ventaRepository.count();
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Boolean> first = executor.submit(() -> confirmar(pedido.getId(), ready, start));
            Future<Boolean> second = executor.submit(() -> confirmar(pedido.getId(), ready, start));
            ready.await();
            start.countDown();

            assertEquals(1, (first.get() ? 1 : 0) + (second.get() ? 1 : 0));
            assertEquals(ventasAntes + 1, ventaRepository.count());
            assertEquals(1, stockSucursalRepository.findById(fixture.stock().getId()).orElseThrow().getDisponible());
            assertEquals(EstadoPedido.CONFIRMADO, pedidoRepository.findById(pedido.getId()).orElseThrow().getEstado());
        } finally {
            executor.shutdownNow();
        }
    }

    private boolean confirmar(Long pedidoId, CountDownLatch ready, CountDownLatch start) throws InterruptedException {
        ready.countDown();
        start.await();
        try {
            pedidoService.cambiarEstado(pedidoId, EstadoPedido.CONFIRMADO);
            return true;
        } catch (IllegalStateException expected) {
            return false;
        }
    }

    private Pedido crearPedido(Fixture fixture, int cantidad) {
        com.minimarket.entity.Carrito carrito = new com.minimarket.entity.Carrito();
        carrito.setUsuario(fixture.usuario());
        carrito.setProducto(fixture.producto());
        carrito.setCantidad(cantidad);
        carritoRepository.saveAndFlush(carrito);
        return pedidoService.checkout(fixture.usuario().getUsername(),
                new CheckoutRequest(TipoEntrega.RETIRO_TIENDA, fixture.sucursal().getId(), null));
    }

    private Fixture fixture(int disponible, double precio) {
        Usuario usuario = new Usuario();
        usuario.setUsername("pedido-confirmacion-user-" + System.nanoTime());
        usuario.setPassword("password");
        usuario.setRoles(Set.of());
        usuario = usuarioRepository.save(usuario);
        Categoria categoria = new Categoria();
        categoria.setNombre("pedido-confirmacion-category-" + System.nanoTime());
        categoria = categoriaRepository.save(categoria);
        Producto producto = new Producto();
        producto.setNombre("pedido-confirmacion-product-" + System.nanoTime());
        producto.setPrecio(precio);
        producto.setStock(99);
        producto.setCategoria(categoria);
        producto = productoRepository.save(producto);
        Sucursal sucursal = new Sucursal();
        sucursal.setNombre("pedido-confirmacion-sucursal-" + System.nanoTime());
        sucursal = sucursalRepository.save(sucursal);
        StockSucursal stock = new StockSucursal();
        stock.setSucursal(sucursal);
        stock.setProducto(producto);
        stock.setDisponible(disponible);
        stock.setStockMinimo(0);
        stock = stockSucursalRepository.saveAndFlush(stock);
        return new Fixture(usuario, producto, sucursal, stock);
    }

    private record Fixture(Usuario usuario, Producto producto, Sucursal sucursal, StockSucursal stock) { }
}
