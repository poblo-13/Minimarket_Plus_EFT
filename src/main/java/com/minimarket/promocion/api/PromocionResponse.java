package com.minimarket.promocion.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Promoción de producto sin exponer entidades.")
public record PromocionResponse(Long id, Long productoId, BigDecimal porcentajeDescuento, BigDecimal importeDescuento, LocalDate inicio, LocalDate fin, boolean activa) { }
