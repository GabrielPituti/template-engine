package com.vaas.templateengine.application.service;

import com.vaas.templateengine.domain.model.InputVariable;
import com.vaas.templateengine.domain.model.VariableType;
import com.vaas.templateengine.shared.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/**
 * SchemaValidator: Garante que o payload de variáveis recebido condiz
 * com o inputSchema definido no template.
 */
@Component
public class SchemaValidator {

    /**
     * Valida um mapa de variáveis contra uma lista de definições de variáveis.
     * @param inputVariables Definições do schema do template
     * @param providedVariables Valores fornecidos na requisição
     */
    public void validate(List<InputVariable> inputVariables, Map<String, Object> providedVariables) {
        if (inputVariables == null || inputVariables.isEmpty()) {
            return;
        }

        for (InputVariable schemaVar : inputVariables) {
            Object value = providedVariables.get(schemaVar.name());

            // 1. Validar Obrigatoriedade
            if (schemaVar.required() && value == null) {
                throw new BusinessException(
                        "Variável obrigatória ausente: " + schemaVar.name(),
                        "MISSING_REQUIRED_VARIABLE"
                );
            }

            // Se o valor estiver presente, validar o tipo
            if (value != null) {
                validateType(schemaVar.name(), schemaVar.type(), value);
            }
        }
    }

    private void validateType(String name, VariableType expectedType, Object value) {
        boolean isValid = switch (expectedType) {
            case STRING -> value instanceof String;
            case NUMBER -> value instanceof Number;
            case BOOLEAN -> value instanceof Boolean;
            case DATE -> isValidDate(value);
        };

        if (!isValid) {
            throw new BusinessException(
                    String.format("Tipo inválido para variável '%s'. Esperado: %s", name, expectedType),
                    "INVALID_VARIABLE_TYPE"
            );
        }
    }

    private boolean isValidDate(Object value) {
        if (value instanceof String str) {
            try {
                // Validação básica de ISO-8601
                java.time.OffsetDateTime.parse(str);
                return true;
            } catch (DateTimeParseException e) {
                try {
                    java.time.LocalDate.parse(str);
                    return true;
                } catch (DateTimeParseException e2) {
                    return false;
                }
            }
        }
        return false;
    }
}