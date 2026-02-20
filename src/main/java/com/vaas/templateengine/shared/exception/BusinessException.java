package com.vaas.templateengine.shared.exception;

import lombok.Getter;

/**
 * Exceção customizada para erros de regra de negócio.
 * Permite que o Controller retorne códigos de erro claros (Ex: TEMPLATE_NOT_FOUND).
 */
@Getter
public class BusinessException extends RuntimeException {
    private final String code;

    public BusinessException(String message, String code) {
        super(message);
        this.code = code;
    }
}