package com.vaas.templateengine.infrastructure.web;

import com.vaas.templateengine.shared.exception.BusinessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Controller Advice: Transforma exceções em respostas JSON padronizadas.
 * Isso é "Tratamento Clínico": o cliente da API recebe um erro que ele entende.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(BusinessException ex) {
        return ResponseEntity.status(400).body(Map.of(
                "timestamp", LocalDateTime.now(),
                "code", ex.getCode(),
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return ResponseEntity.status(500).body(Map.of(
                "timestamp", LocalDateTime.now(),
                "code", "INTERNAL_SERVER_ERROR",
                "message", "Ocorreu um erro inesperado no servidor."
        ));
    }
}