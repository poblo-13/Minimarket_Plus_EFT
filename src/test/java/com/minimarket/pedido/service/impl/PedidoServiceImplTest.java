package com.minimarket.pedido.service.impl;

import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.pedido.api.CheckoutRequest;
import com.minimarket.pedido.domain.EstadoPedido;
import com.minimarket.pedido.domain.Pedido;
import com.minimarket.pedido.domain.TipoEntrega;
import com.minimarket.pedido.integration.PedidoVentaIntegration;
import com.minimarket.pedido.repository.PedidoRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.repository.CarritoRepository;
import com.minimarket.sucursal.SucursalRepository;
import com.minimarket.sucursal.StockSucursal;
import com.minimarket.sucursal.StockSucursalRepository;
import com.minimarket.entity.Carrito;
import com.minimarket.promocion.PromocionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceImplTest {
    @Mock PedidoRepository pedidoRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock PedidoVentaIntegration pedidoVentaIntegration;
    @Mock SucursalRepository sucursalRepository;
    @Mock CarritoRepository carritoRepository;
    @Mock StockSucursalRepository stockSucursalRepository;
    @Mock PromocionService promocionService;
    @InjectMocks PedidoServiceImpl pedidoService;

    @BeforeEach
    void precioPromocionalPorDefecto() {
        lenient().when(promocionService.calcularPrecioEfectivo(anyLong(), any())).thenReturn(new BigDecimal("1.10"));
    }

    @Test
    void checkoutCreaPedidoConSnapshotYVaciaCarritoSinDescontarStock() {
        Usuario cliente = cliente(7L);
        Producto producto = producto(11L, "Pan", 1.10D);
        Carrito item = new Carrito();
        item.setUsuario(cliente); item.setProducto(producto); item.setCantidad(3);
        StockSucursal stock = new StockSucursal(); stock.setDisponible(3); stock.setStockMinimo(0);
        when(usuarioRepository.findByUsernameForUpdate("cliente")).thenReturn(Optional.of(cliente));
        when(carritoRepository.findByUsuarioId(7L)).thenReturn(List.of(item));
        when(sucursalRepository.existsById(3L)).thenReturn(true);
        when(stockSucursalRepository.findBySucursalIdAndProductoId(3L, 11L)).thenReturn(Optional.of(stock));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pedido pedido = pedidoService.checkout("cliente", new CheckoutRequest(TipoEntrega.RETIRO_TIENDA, 3L, null));

        assertEquals(EstadoPedido.PENDIENTE, pedido.getEstado());
        assertEquals(new BigDecimal("3.30"), pedido.getTotal());
        assertEquals(3, stock.getDisponible());
        verify(carritoRepository).deleteAll(List.of(item));
        verifyNoInteractions(pedidoVentaIntegration);
    }

    @Test
    void checkoutFallidoConservaCarrito() {
        Usuario cliente = cliente(7L);
        Producto producto = producto(11L, "Pan", 1.10D);
        Carrito item = new Carrito(); item.setUsuario(cliente); item.setProducto(producto); item.setCantidad(2);
        StockSucursal stock = new StockSucursal(); stock.setDisponible(1); stock.setStockMinimo(0);
        when(usuarioRepository.findByUsernameForUpdate("cliente")).thenReturn(Optional.of(cliente));
        when(carritoRepository.findByUsuarioId(7L)).thenReturn(List.of(item));
        when(sucursalRepository.existsById(3L)).thenReturn(true);
        when(stockSucursalRepository.findBySucursalIdAndProductoId(3L, 11L)).thenReturn(Optional.of(stock));

        assertThrows(IllegalStateException.class, () -> pedidoService.checkout("cliente", new CheckoutRequest(TipoEntrega.RETIRO_TIENDA, 3L, null)));
        verify(carritoRepository, never()).deleteAll(any());
        verifyNoInteractions(pedidoRepository, pedidoVentaIntegration);
    }

    @Test
    void checkoutValidaReglasDeEntregaAntesDeConsultarOPersistir() {
        assertThrows(IllegalArgumentException.class, () -> pedidoService.checkout("cliente",
                new CheckoutRequest(TipoEntrega.RETIRO_TIENDA, 3L, "Av. Siempre Viva")));
        assertThrows(IllegalArgumentException.class, () -> pedidoService.checkout("cliente",
                new CheckoutRequest(TipoEntrega.DESPACHO_DOMICILIO, 3L, "  ")));
        assertThrows(IllegalArgumentException.class, () -> pedidoService.checkout("cliente",
                new CheckoutRequest(TipoEntrega.DESPACHO_DOMICILIO, 3L, "x".repeat(501))));
        verifyNoInteractions(usuarioRepository, carritoRepository, pedidoRepository);
    }

    @Test
    void permiteSoloTransicionesSecuencialesYCancelaSoloPendiente() {
        Pedido pedido = pedido(20L, EstadoPedido.PENDIENTE);
        when(pedidoRepository.findById(20L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(pedidoVentaIntegration.confirmarVenta(20L)).thenAnswer(invocation -> {
            pedido.setEstado(EstadoPedido.CONFIRMADO);
            return pedido;
        });

        assertEquals(EstadoPedido.CONFIRMADO, pedidoService.cambiarEstado(20L, EstadoPedido.CONFIRMADO).getEstado());
        verify(pedidoVentaIntegration).confirmarVenta(20L);
        assertThrows(IllegalStateException.class, () -> pedidoService.cambiarEstado(20L, EstadoPedido.LISTO));
        assertThrows(IllegalStateException.class, () -> pedidoService.cancelarOperativo(20L));

        Pedido pendiente = pedido(21L, EstadoPedido.PENDIENTE);
        when(pedidoRepository.findById(21L)).thenReturn(Optional.of(pendiente));
        assertEquals(EstadoPedido.CANCELADO, pedidoService.cancelarOperativo(21L).getEstado());
    }

    @Test
    void clientePuedeCancelarSoloSuPedidoPendiente() {
        Pedido pedido = pedido(22L, EstadoPedido.PENDIENTE);
        pedido.getUsuario().setUsername("cliente");
        when(pedidoRepository.findById(22L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(pedido)).thenReturn(pedido);

        assertEquals(EstadoPedido.CANCELADO, pedidoService.cancelar(22L, "cliente").getEstado());
    }

    @Test
    void listaOperativaAplicaFiltrosDeEstadoYSucursal() {
        Pedido pedido = pedido(30L, EstadoPedido.CONFIRMADO);
        when(pedidoRepository.findByEstadoAndSucursalIdOrderByCreadoEnAsc(EstadoPedido.CONFIRMADO, 4L))
                .thenReturn(List.of(pedido));

        List<Pedido> resultado = pedidoService.listarOperativos(EstadoPedido.CONFIRMADO, 4L);

        assertEquals(List.of(pedido), resultado);
        verify(pedidoRepository).findByEstadoAndSucursalIdOrderByCreadoEnAsc(EstadoPedido.CONFIRMADO, 4L);
    }

    @Test
    void listaOperativaPuedeFiltrarPorSucursalYDevolverVacia() {
        when(pedidoRepository.findBySucursalIdOrderByCreadoEnAsc(8L)).thenReturn(List.of());

        assertEquals(List.of(), pedidoService.listarOperativos(null, 8L));
        verify(pedidoRepository).findBySucursalIdOrderByCreadoEnAsc(8L);
    }

    @Test
    void colaSinFiltrosIncluyeSoloEstadosActivosOrdenadosPorCreacion() {
        Pedido pendiente = pedido(31L, EstadoPedido.PENDIENTE);
        Pedido listo = pedido(32L, EstadoPedido.LISTO);
        when(pedidoRepository.findByEstadoInOrderByCreadoEnAsc(List.of(
                EstadoPedido.PENDIENTE, EstadoPedido.CONFIRMADO, EstadoPedido.EN_PREPARACION, EstadoPedido.LISTO)))
                .thenReturn(List.of(pendiente, listo));

        assertEquals(List.of(pendiente, listo), pedidoService.listarOperativos(null, null));
    }

    private Usuario cliente(Long id) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setUsername("cliente");
        usuario.setPassword("password");
        return usuario;
    }

    private Producto producto(Long id, String nombre, Double precio) {
        Producto producto = new Producto();
        producto.setId(id);
        producto.setNombre(nombre);
        producto.setPrecio(precio);
        return producto;
    }

    private Pedido pedido(Long id, EstadoPedido estado) {
        Pedido pedido = new Pedido();
        pedido.setId(id);
        pedido.setUsuario(cliente(7L));
        pedido.setEstado(estado);
        return pedido;
    }
}
