package com.minimarket.pedido.service;

import com.minimarket.pedido.api.CrearPedidoRequest;
import com.minimarket.pedido.domain.EstadoPedido;
import com.minimarket.pedido.domain.Pedido;

import java.util.List;

public interface PedidoService {
    Pedido crear(Long clienteId, CrearPedidoRequest request);
    Pedido crear(String usernameCliente, CrearPedidoRequest request);
    Pedido cancelar(Long pedidoId, Long clienteId);
    Pedido cancelar(Long pedidoId, String usernameCliente);
    Pedido cambiarEstado(Long pedidoId, EstadoPedido nuevoEstado);
    Pedido obtenerParaCliente(Long pedidoId, String usernameCliente);
    List<Pedido> listarParaCliente(String usernameCliente);
}
