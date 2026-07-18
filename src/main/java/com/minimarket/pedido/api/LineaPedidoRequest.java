package com.minimarket.pedido.api;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record LineaPedidoRequest(
        @NotNull(message = "El producto es obligatorio") Long productoId,
        @NotNull(message = "La cantidad es obligatoria") @Min(value = 1, message = "La cantidad debe ser al menos 1") Integer cantidad) {

    @JsonAnySetter
    public void rechazarCampoControladoPorServidor(String nombre, Object valor) {
        throw new IllegalArgumentException("El campo '" + nombre + "' no está permitido en el detalle del pedido");
    }
}
