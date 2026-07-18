package com.minimarket.pedido.service.impl;

import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.pedido.api.CrearPedidoRequest;
import com.minimarket.pedido.api.LineaPedidoRequest;
import com.minimarket.pedido.domain.EstadoPedido;
import com.minimarket.pedido.domain.Pedido;
import com.minimarket.pedido.domain.TipoEntrega;
import com.minimarket.pedido.repository.PedidoRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
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
    @Mock ProductoRepository productoRepository;
    @InjectMocks PedidoServiceImpl pedidoService;

    @Test
    void crearTomaSnapshotsDelProductoYCalculaTotalConBigDecimal() {
        Usuario cliente = cliente(7L);
        Producto producto = producto(11L, "Pan", 1.10D);
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(cliente));
        when(productoRepository.findById(11L)).thenReturn(Optional.of(producto));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pedido pedido = pedidoService.crear(7L, new CrearPedidoRequest(TipoEntrega.RETIRO_TIENDA, 3L, null,
                List.of(new LineaPedidoRequest(11L, 3))));

        assertEquals(EstadoPedido.PENDIENTE, pedido.getEstado());
        assertEquals(new BigDecimal("3.30"), pedido.getTotal());
        assertEquals("Pan", pedido.getDetalles().getFirst().getNombreProducto());
        assertEquals(new BigDecimal("1.10"), pedido.getDetalles().getFirst().getPrecioUnitario());
        assertEquals(new BigDecimal("3.30"), pedido.getDetalles().getFirst().getSubtotal());
        assertSame(pedido, pedido.getDetalles().getFirst().getPedido());
        verify(pedidoRepository).save(any(Pedido.class));
    }

    @Test
    void crearValidaDatosDeEntregaYCantidad() {
        assertThrows(IllegalArgumentException.class, () -> pedidoService.crear(1L,
                new CrearPedidoRequest(TipoEntrega.RETIRO_TIENDA, null, null, List.of(new LineaPedidoRequest(1L, 1)))));
        assertThrows(IllegalArgumentException.class, () -> pedidoService.crear(1L,
                new CrearPedidoRequest(TipoEntrega.DESPACHO_DOMICILIO, null, " ", List.of(new LineaPedidoRequest(1L, 1)))));
        assertThrows(IllegalArgumentException.class, () -> pedidoService.crear(1L,
                new CrearPedidoRequest(TipoEntrega.RETIRO_TIENDA, 1L, null, List.of(new LineaPedidoRequest(1L, 0)))));
        verifyNoInteractions(usuarioRepository, productoRepository, pedidoRepository);
    }

    @Test
    void permiteSoloTransicionesSecuencialesYCancelaSoloPendiente() {
        Pedido pedido = pedido(20L, EstadoPedido.PENDIENTE);
        when(pedidoRepository.findById(20L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals(EstadoPedido.CONFIRMADO, pedidoService.cambiarEstado(20L, EstadoPedido.CONFIRMADO).getEstado());
        assertThrows(IllegalStateException.class, () -> pedidoService.cambiarEstado(20L, EstadoPedido.LISTO));
        assertThrows(IllegalStateException.class, () -> pedidoService.cancelar(20L, 7L));

        Pedido pendiente = pedido(21L, EstadoPedido.PENDIENTE);
        when(pedidoRepository.findById(21L)).thenReturn(Optional.of(pendiente));
        assertEquals(EstadoPedido.CANCELADO, pedidoService.cancelar(21L, 7L).getEstado());
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
