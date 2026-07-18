package com.minimarket.promocion.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PromocionRequest(@NotNull Long productoId, @DecimalMin("0.01") @DecimalMax("100.00") BigDecimal porcentajeDescuento,
                               @DecimalMin("0.01") BigDecimal importeDescuento, @NotNull LocalDate inicio, @NotNull LocalDate fin, boolean activa) { }
