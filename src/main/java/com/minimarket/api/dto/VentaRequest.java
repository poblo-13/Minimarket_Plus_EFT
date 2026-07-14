package com.minimarket.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/** Entrada del cliente para el comando agregado de venta. Los precios y las marcas de tiempo pertenecen al servidor. */
public record VentaRequest(
        @NotNull @Positive Long usuarioId,
        @NotEmpty List<@Valid LineaVentaRequest> lineas) { }
