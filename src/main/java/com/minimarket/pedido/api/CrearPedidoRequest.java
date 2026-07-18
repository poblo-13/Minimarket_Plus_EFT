package com.minimarket.pedido.api;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.minimarket.pedido.domain.TipoEntrega;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** Datos controlados por el cliente; precio, estado y fechas son asignados por el servidor. */
public record CrearPedidoRequest(
        @NotNull(message = "El tipo de entrega es obligatorio") TipoEntrega tipoEntrega,
        Long sucursalId,
        String direccionEntrega,
        @NotEmpty(message = "El pedido debe tener al menos un detalle") @Valid List<LineaPedidoRequest> detalles) {

    @JsonAnySetter
    public void rechazarCampoControladoPorServidor(String nombre, Object valor) {
        throw new IllegalArgumentException("El campo '" + nombre + "' no está permitido al crear un pedido");
    }
}
