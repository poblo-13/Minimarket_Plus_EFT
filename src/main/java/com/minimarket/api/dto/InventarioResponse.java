package com.minimarket.api.dto;

import java.time.LocalDateTime;

public record InventarioResponse(Long id, Long productoId, Integer cantidad, String tipoMovimiento,
                                 LocalDateTime fechaMovimiento) { }
