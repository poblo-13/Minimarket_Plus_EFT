package com.minimarket.api.dto;

public record CarritoResponse(Long id, Long usuarioId, Long productoId, Integer cantidad) { }
