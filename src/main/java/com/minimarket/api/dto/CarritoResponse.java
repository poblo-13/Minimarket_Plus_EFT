package com.minimarket.api.dto;

import java.math.BigDecimal;
import java.util.List;

/** Vista del carrito del actor autenticado, con precios sólo estimados. */
public record CarritoResponse(List<Item> items, BigDecimal total) {
    public record Item(Long productoId, String nombreProducto, Integer cantidad,
                       BigDecimal precioUnitarioEstimado, BigDecimal subtotal) { }
}
