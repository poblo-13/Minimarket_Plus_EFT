package com.minimarket.pedido.integration;

import com.minimarket.pedido.domain.Pedido;

/**
 * Punto de integración futuro: al confirmar un pedido se podrá crear la venta y descontar inventario.
 * No se invoca aún para mantener Pedido independiente de VentaService e inventario.
 */
public interface PedidoVentaIntegration {
    void confirmarVenta(Pedido pedido);
}
