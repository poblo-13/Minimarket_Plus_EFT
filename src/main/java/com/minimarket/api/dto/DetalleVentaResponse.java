package com.minimarket.api.dto;

import java.math.BigDecimal;

public record DetalleVentaResponse(Long id, Long ventaId, Long productoId, Integer cantidad, BigDecimal precio) { }
