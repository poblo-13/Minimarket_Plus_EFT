package com.minimarket.pedido.api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.minimarket.sucursal.exception.StockSucursalInsuficienteException;

import java.net.URI;

/** Mapea conflictos del flujo de pedidos sin alterar el manejador global existente. */
@RestControllerAdvice(basePackageClasses = PedidoController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PedidoApiExceptionHandler {
    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<ProblemDetail> conflicto(IllegalStateException exception, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problem.setType(URI.create("urn:minimarket:error:conflict"));
        problem.setTitle(HttpStatus.CONFLICT.getReasonPhrase());
        problem.setProperty("code", "CONFLICT");
        problem.setProperty("path", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(StockSucursalInsuficienteException.class)
    ResponseEntity<ProblemDetail> stockInsuficiente(StockSucursalInsuficienteException exception, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problem.setType(URI.create("urn:minimarket:error:insufficient-stock"));
        problem.setTitle(HttpStatus.CONFLICT.getReasonPhrase());
        problem.setProperty("code", "INSUFFICIENT_STOCK");
        problem.setProperty("path", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }
}
