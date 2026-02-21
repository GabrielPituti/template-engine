package com.vaas.templateengine.infrastructure.web;

import com.vaas.templateengine.shared.exception.BusinessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Centralizador de exceções da infraestrutura web.
 * Implementa o padrão de respostas semânticas exigido em APIs de missão crítica.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Trata conflitos de concorrência otimista (Race Conditions).
     * Mapeia erros de versão do banco para HTTP 409 Conflict.
     */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(OptimisticLockingFailureException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "timestamp", OffsetDateTime.now(),
                "code", "CONCURRENCY_CONFLICT",
                "message", "O recurso foi atualizado por outro usuário. Por favor, recarregue os dados e tente novamente."
        ));
    }

    /**
     * Trata erros de regras de negócio definidos pelo domínio.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(BusinessException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "timestamp", OffsetDateTime.now(),
                "code", ex.getCode(),
                "message", ex.getMessage()
        ));
    }

    /**
     * Trata falhas de validação de contrato (Bean Validation).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "timestamp", OffsetDateTime.now(),
                "code", "VALIDATION_ERROR",
                "message", "Campos inválidos na requisição",
                "details", details
        ));
    }

    /**
     * Tratamento genérico para falhas inesperadas, garantindo que stacktraces técnicos não vazem na API.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "timestamp", OffsetDateTime.now(),
                "code", "INTERNAL_SERVER_ERROR",
                "message", "Ocorreu um erro inesperado no servidor."
        ));
    }
}