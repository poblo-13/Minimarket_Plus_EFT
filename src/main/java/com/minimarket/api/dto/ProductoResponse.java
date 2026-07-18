package com.minimarket.api.dto;

import java.math.BigDecimal;

/** stockLegado is a compatibility projection; consult StockSucursal for operational availability. */
public record ProductoResponse(Long id, String nombre, BigDecimal precio, Integer stockLegado, Long categoriaId) { }
