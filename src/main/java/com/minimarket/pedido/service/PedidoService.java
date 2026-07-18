package com.minimarket.pedido.service;

import com.minimarket.pedido.api.CheckoutRequest;
import com.minimarket.pedido.api.CrearPedidoRequest;
import com.minimarket.pedido.domain.EstadoPedido;
import com.minimarket.pedido.domain.Pedido;

import java.util.List;

public interface PedidoService {
    /** Compatibilidad de compilación para clientes retirados; el checkout es la única creación funcional. */
    @Deprecated(forRemoval = true) default Pedido crear(Long clienteId, CrearPedidoRequest request) { throw new UnsupportedOperationException("Creación manual retirada"); }
    @Deprecated(forRemoval = true) default Pedido crear(String username, CrearPedidoRequest request) { throw new UnsupportedOperationException("Creación manual retirada"); }
    @Deprecated(forRemoval = true) default Pedido cancelar(Long pedidoId, Long clienteId) { throw new UnsupportedOperationException("Use cancelación por username"); }
    Pedido checkout(String usernameCliente, CheckoutRequest request);
    Pedido cancelar(Long pedidoId, String usernameCliente);
    Pedido cancelarOperativo(Long pedidoId);
    Pedido cambiarEstado(Long pedidoId, EstadoPedido nuevoEstado);
    Pedido obtenerParaCliente(Long pedidoId, String usernameCliente);
    List<Pedido> listarParaCliente(String usernameCliente);
    List<Pedido> listarOperativos(EstadoPedido estado, Long sucursalId);
}
