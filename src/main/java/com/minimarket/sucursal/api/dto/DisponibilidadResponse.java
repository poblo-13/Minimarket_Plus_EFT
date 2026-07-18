package com.minimarket.sucursal.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Disponibilidad operativa de un producto en una sucursal.")
public record DisponibilidadResponse(
        @Schema(example = "1") Long sucursalId,
        @Schema(example = "10") Long productoId,
        @Schema(example = "24") Integer cantidad,
        @Schema(example = "5") Integer minimo) {
}
