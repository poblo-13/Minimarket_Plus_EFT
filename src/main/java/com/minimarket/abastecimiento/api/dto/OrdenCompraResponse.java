package com.minimarket.abastecimiento.api.dto;

import com.minimarket.abastecimiento.EstadoOrdenCompra;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Consulta administrativa de una orden de compra, sin referencias a entidades.")
public record OrdenCompraResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "2") Long sucursalId,
        @Schema(example = "10") Long productoId,
        @Schema(example = "3") Long proveedorId,
        @Schema(example = "12") Integer cantidadSolicitada,
        EstadoOrdenCompra estado) {
}
