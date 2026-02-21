package com.vaas.templateengine.infrastructure.web;

import com.vaas.templateengine.shared.exception.BusinessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller Advice: Transforma exceções em respostas JSON padronizadas.
 * Implementa o "Tratamento Clínico": o cliente da API recebe um erro semântico e detalhado.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Captura erros de regras de negócio (ex: Versão Publicada não pode ser editada).
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(BusinessException ex) {
        return ResponseEntity.status(400).body(Map.of(
                "timestamp", LocalDateTime.now(),
                "code", ex.getCode(),
                "message", ex.getMessage()
        ));
    }

    /**
     * Captura erros de validação de campos (ex: @NotBlank, @NotNull).
     * Essencial para o "Fail-Fast" solicitado em arquiteturas sêniores.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity.status(400).body(Map.of(
                "timestamp", LocalDateTime.now(),
                "code", "VALIDATION_ERROR",
                "message", "Campos inválidos na requisição",
                "details", details
        ));
    }

    /**
     * Fallback para erros inesperados. Evita vazamento de stacktrace para o cliente.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return ResponseEntity.status(500).body(Map.of(
                "timestamp", LocalDateTime.now(),
                "code", "INTERNAL_SERVER_ERROR",
                "message", "Ocorreu um erro inesperado no servidor."
        ));
    }
}