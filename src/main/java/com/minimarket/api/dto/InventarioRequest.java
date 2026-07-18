package com.minimarket.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

public record InventarioRequest(
        @NotNull @Positive Long sucursalId,
        @NotNull @Positive Long productoId,
        @NotNull @Min(1) Integer cantidad,
        @NotBlank @Pattern(regexp = "Entrada|Salida") String tipoMovimiento,
        @NotNull LocalDateTime fechaMovimiento) { }
