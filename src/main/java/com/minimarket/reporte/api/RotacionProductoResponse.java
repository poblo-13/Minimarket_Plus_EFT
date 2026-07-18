package com.minimarket.reporte.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Rotación agregada por producto en el rango consultado.")
public record RotacionProductoResponse(@Schema(example = "12") Long productoId, @Schema(example = "Arroz") String productoNombre,
                                       @Schema(example = "18") Long cantidadVendida, @Schema(example = "22500.00") BigDecimal importeVendido) { }
