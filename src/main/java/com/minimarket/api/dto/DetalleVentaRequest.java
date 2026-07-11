package com.minimarket.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record DetalleVentaRequest(
        @NotNull @Positive Long ventaId,
        @NotNull @Positive Long productoId,
        @NotNull @Min(1) Integer cantidad,
        @NotNull @DecimalMin("0.01") BigDecimal precio) { }
