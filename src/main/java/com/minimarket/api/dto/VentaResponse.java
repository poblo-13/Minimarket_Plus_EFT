package com.minimarket.api.dto;

import java.time.LocalDateTime;
import java.util.List;

public record VentaResponse(Long id, Long usuarioId, LocalDateTime fecha, List<Long> detalleIds, Double total) { }
