package com.minimarket.pedido.service;

import com.minimarket.pedido.api.CrearPedidoRequest;
import com.minimarket.pedido.domain.EstadoPedido;
import com.minimarket.pedido.domain.Pedido;

public interface PedidoService {
    Pedido crear(Long clienteId, CrearPedidoRequest request);
    Pedido crear(String usernameCliente, CrearPedidoRequest request);
    Pedido cancelar(Long pedidoId, Long clienteId);
    Pedido cambiarEstado(Long pedidoId, EstadoPedido nuevoEstado);
}
