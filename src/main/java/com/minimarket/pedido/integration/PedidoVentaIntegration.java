package com.minimarket.pedido.integration;

import com.minimarket.pedido.domain.Pedido;

/** Comando interno que confirma un pedido junto con su venta y stock de sucursal. */
public interface PedidoVentaIntegration {
    Pedido confirmarVenta(Long pedidoId);
}
