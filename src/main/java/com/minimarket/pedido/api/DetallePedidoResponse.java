package com.minimarket.pedido.api;

import com.minimarket.pedido.domain.DetallePedido;

import java.math.BigDecimal;

/** Snapshot inmutable de una línea: el precio procede del pedido, nunca del cliente. */
public record DetallePedidoResponse(Long productoId, String nombreProducto, BigDecimal precioUnitario,
                                   Integer cantidad, BigDecimal subtotal) {
    static DetallePedidoResponse desde(DetallePedido detalle) {
        return new DetallePedidoResponse(detalle.getProducto().getId(), detalle.getNombreProducto(),
                detalle.getPrecioUnitario(), detalle.getCantidad(), detalle.getSubtotal());
    }
}
