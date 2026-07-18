package com.minimarket.sucursal.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representación pública de una sucursal.")
public record SucursalResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "Sucursal Centro") String nombre) {
}
