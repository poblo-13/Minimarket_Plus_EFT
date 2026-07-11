package com.minimarket.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductoRequest(
        @NotBlank @Size(max = 150) String nombre,
        @NotNull @DecimalMin("0.01") BigDecimal precio,
        @NotNull @PositiveOrZero Integer stock,
        @NotNull @Positive Long categoriaId) { }
