package com.minimarket.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.dao.DataIntegrityViolationException;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleBodyValidation(MethodArgumentNotValidException exception,
                                                              HttpServletRequest request) {
        List<Map<String, Object>> errors = exception.getBindingResult().getAllErrors().stream()
                .map(this::validationError)
                .toList();
        return problem(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed.", request, errors);
    }

    @ExceptionHandler({HandlerMethodValidationException.class, ConstraintViolationException.class})
    public ResponseEntity<ProblemDetail> handleMethodValidation(Exception exception, HttpServletRequest request) {
        List<Map<String, Object>> errors = switch (exception) {
            case HandlerMethodValidationException methodException -> methodException.getAllErrors().stream()
                    .map(this::methodValidationError).toList();
            case ConstraintViolationException constraintException -> constraintException.getConstraintViolations().stream()
                    .map(violation -> Map.<String, Object>of("field", violation.getPropertyPath().toString(),
                            "message", violation.getMessage()))
                    .toList();
            default -> List.of();
        };
        return problem(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed.", request, errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleMalformedJson(HttpMessageNotReadableException exception,
                                                             HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "MALFORMED_JSON", "Malformed JSON request body.", request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleTypeMismatch(MethodArgumentTypeMismatchException exception,
                                                            HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "TYPE_MISMATCH",
                "Parameter '%s' has an invalid value.".formatted(exception.getName()), request);
    }

    @ExceptionHandler({NoResourceFoundException.class, EntityNotFoundException.class, NoSuchElementException.class})
    public ResponseEntity<ProblemDetail> handleNotFound(Exception exception, HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND, "NOT_FOUND", "The requested resource was not found.", request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleConflict(DataIntegrityViolationException exception,
                                                        HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, "CONFLICT", "The request conflicts with the current resource state.", request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleInvalidArgument(IllegalArgumentException exception,
                                                               HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", exception.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception exception, HttpServletRequest request) {
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "An unexpected error occurred.", request);
    }

    private Map<String, Object> validationError(ObjectError error) {
        if (error instanceof FieldError fieldError) {
            return Map.of("field", fieldError.getField(), "message", fieldError.getDefaultMessage());
        }
        return Map.of("field", error.getObjectName(), "message", error.getDefaultMessage());
    }

    private Map<String, Object> methodValidationError(MessageSourceResolvable error) {
        return Map.of("field", "request", "message", error.getDefaultMessage());
    }

    private ResponseEntity<ProblemDetail> problem(HttpStatus status, String code, String detail,
                                                  HttpServletRequest request) {
        return problem(status, code, detail, request, null);
    }

    private ResponseEntity<ProblemDetail> problem(HttpStatus status, String code, String detail,
                                                  HttpServletRequest request, List<Map<String, Object>> errors) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create("urn:minimarket:error:" + code.toLowerCase()));
        problem.setTitle(status.getReasonPhrase());
        problem.setProperty("code", code);
        problem.setProperty("path", request.getRequestURI());
        if (errors != null && !errors.isEmpty()) {
            problem.setProperty("errors", errors);
        }
        return ResponseEntity.status(status).body(problem);
    }
}
