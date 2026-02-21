package com.vaas.templateengine.infrastructure.web;

import com.vaas.templateengine.shared.exception.BusinessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller Advice: Transforma exceções em respostas JSON padronizadas.
 * Implementa o "Tratamento Clínico": o cliente da API recebe um erro semântico e detalhado.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(BusinessException ex) {
        return ResponseEntity.status(400).body(Map.of(
                "timestamp", OffsetDateTime.now(),
                "code", ex.getCode(),
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity.status(400).body(Map.of(
                "timestamp", OffsetDateTime.now(),
                "code", "VALIDATION_ERROR",
                "message", "Campos inválidos na requisição",
                "details", details
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return ResponseEntity.status(500).body(Map.of(
                "timestamp", OffsetDateTime.now(),
                "code", "INTERNAL_SERVER_ERROR",
                "message", "Ocorreu um erro inesperado no servidor."
        ));
    }
}