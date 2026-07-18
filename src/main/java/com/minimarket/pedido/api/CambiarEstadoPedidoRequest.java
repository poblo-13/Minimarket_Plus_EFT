package com.minimarket.pedido.api;

import com.minimarket.pedido.domain.EstadoPedido;
import jakarta.validation.constraints.NotNull;

/** Contrato reservado para los flujos de CAJERO/ADMIN. CANCELADO solo usa el endpoint del dueño. */
public record CambiarEstadoPedidoRequest(@NotNull EstadoPedido estado) {
}
