package com.minimarket.sucursal.api.dto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
public record MovimientoStockRequest(@NotNull @Positive Long productoId, @NotNull @Positive Integer cantidad) { }
