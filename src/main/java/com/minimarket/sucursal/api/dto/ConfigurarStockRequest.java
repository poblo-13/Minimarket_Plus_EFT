package com.minimarket.sucursal.api.dto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
public record ConfigurarStockRequest(@NotNull @Positive Long productoId, @NotNull @Min(0) Integer disponible,
                                     @NotNull @Min(0) Integer minimo, @NotNull @Positive Long proveedorId) { }
