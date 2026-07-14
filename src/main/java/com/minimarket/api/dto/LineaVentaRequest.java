package com.minimarket.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record LineaVentaRequest(
        @NotNull @Positive Long productoId,
        @NotNull @Min(1) Integer cantidad) { }
