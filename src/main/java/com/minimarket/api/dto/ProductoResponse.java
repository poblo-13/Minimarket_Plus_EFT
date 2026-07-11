package com.minimarket.api.dto;

import java.math.BigDecimal;

public record ProductoResponse(Long id, String nombre, BigDecimal precio, Integer stock, Long categoriaId) { }
