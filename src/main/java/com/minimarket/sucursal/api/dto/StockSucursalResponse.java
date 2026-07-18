package com.minimarket.sucursal.api.dto;
public record StockSucursalResponse(Long sucursalId, Long productoId, Integer disponible, Integer minimo) { }
