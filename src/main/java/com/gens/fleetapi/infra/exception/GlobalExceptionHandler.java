package com.gens.fleetapi.infra.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import lombok.extern.slf4j.Slf4j;


import java.time.Instant;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<StandardError> resourceNotFound(ResourceNotFoundException e, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new StandardError(
                        Instant.now(),
                        HttpStatus.NOT_FOUND.value(),
                        "Resource not found",
                        e.getMessage(),
                        request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardError> validation(MethodArgumentNotValidException e, HttpServletRequest request) {

        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .reduce((m1, m2) -> m1 + ", " + m2)
                .orElse("Validation error in the fields.");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new StandardError(
                        Instant.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        "Validation error",
                        errorMessage,
                        request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<StandardError> authentication(AuthenticationException e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new StandardError(
                        Instant.now(),
                        HttpStatus.UNAUTHORIZED.value(),
                        "Authentication failed",
                        "Invalid email or password.",
                        request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardError> genericException(Exception e, HttpServletRequest request) {

        log.error("Unexpected error while handling {}", request.getRequestURI(), e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new StandardError(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal server error",
                "An unexpected system error occurred. Please try again later.",
                request.getRequestURI()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<StandardError> illegalArgument(IllegalArgumentException e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new StandardError(
                        Instant.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        "Invalid request (Invariant violation)",
                        e.getMessage(),
                        request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<StandardError> illegalState(IllegalStateException e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new StandardError(
                        Instant.now(),
                        HttpStatus.CONFLICT.value(),
                        "Business State Conflict",
                        e.getMessage(),
                        request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<StandardError> duplicateResource(DuplicateResourceException e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new StandardError(
                        Instant.now(),
                        HttpStatus.CONFLICT.value(),
                        "Duplicate Feature",
                        e.getMessage(),
                        request.getRequestURI()
                )
        );
    }
}