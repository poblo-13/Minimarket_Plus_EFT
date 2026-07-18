package com.minimarket.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/** Entrada del cliente para el comando agregado de venta. Los precios y las marcas de tiempo pertenecen al servidor. */
public record VentaRequest(
        @NotNull @Positive Long usuarioId,
        @NotNull @Positive Long sucursalId,
        @NotEmpty List<@Valid LineaVentaRequest> lineas) {
    /** Compatibility constructor; new direct-sale flows must provide a sucursalId. */
    public VentaRequest(Long usuarioId, List<LineaVentaRequest> lineas) {
        this(usuarioId, null, lineas);
    }
}
