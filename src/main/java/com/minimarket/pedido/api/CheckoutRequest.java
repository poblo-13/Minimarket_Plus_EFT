package com.minimarket.pedido.api;

import com.minimarket.pedido.domain.TipoEntrega;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CheckoutRequest(
        @NotNull TipoEntrega tipoEntrega,
        @NotNull @Positive Long sucursalId,
        String direccionEntrega) { }
