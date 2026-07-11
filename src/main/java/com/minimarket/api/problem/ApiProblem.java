package com.minimarket.api.problem;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Documentation-only representation of an RFC 9457 Problem Details response.
 * The application error handler may expose the standard members plus the
 * extension members {@code timestamp}, {@code traceId}, and {@code errors}.
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
    /** Documentation-only schema for an individual validation error extension. */
    public record FieldViolation(String field, String message, Object rejectedValue) { }
}
