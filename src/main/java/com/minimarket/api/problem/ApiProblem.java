package com.minimarket.api.problem;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Representación sólo documental de una respuesta Problem Details RFC 9457.
 * El manejador de errores de la aplicación puede exponer los miembros estándar
 * junto con las extensiones {@code timestamp}, {@code traceId} y {@code errors}.
 */
public record ApiProblem(
        URI type,
        String title,
        int status,
        String detail,
        URI instance,
        OffsetDateTime timestamp,
        String traceId,
        List<FieldViolation> errors) {
    /** Esquema sólo documental para una extensión individual de error de validación. */
    public record FieldViolation(String field, String message, Object rejectedValue) { }
}
