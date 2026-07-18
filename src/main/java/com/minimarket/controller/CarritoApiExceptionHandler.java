package com.minimarket.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

/** Conflictos de checkout: carrito vacío y stock no disponible o insuficiente. */
@RestControllerAdvice(basePackageClasses = CarritoController.class)
class CarritoApiExceptionHandler {
    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<ProblemDetail> conflicto(IllegalStateException exception, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problem.setType(URI.create("urn:minimarket:error:conflict"));
        problem.setProperty("code", "CONFLICT");
        problem.setProperty("path", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }
}
