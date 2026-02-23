package com.vaas.templateengine.application.service;

import com.vaas.templateengine.domain.model.InputVariable;
import com.vaas.templateengine.domain.model.VariableType;
import com.vaas.templateengine.shared.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/**
 * Componente de validação clínica de payloads de entrada.
 * Garante a conformidade entre os dados fornecidos em tempo de execução e o contrato
 * definido no schema do template, prevenindo falhas de renderização.
 */
@Component
public class SchemaValidator {

    /**
     * Valida recursivamente a obrigatoriedade e os tipos de dados das variáveis.
     * @param inputVariables Definição do contrato esperado pelo template.
     * @param providedVariables Valores reais fornecidos para processamento.
     */
    public void validate(List<InputVariable> inputVariables, Map<String, Object> providedVariables) {
        if (inputVariables == null || inputVariables.isEmpty()) {
            return;
        }

        for (InputVariable schemaVar : inputVariables) {
            Object value = providedVariables.get(schemaVar.name());

            if (schemaVar.required() && value == null) {
                throw new BusinessException(
                        "Atributo obrigatório não informado: " + schemaVar.name(),
                        "MISSING_REQUIRED_VARIABLE"
                );
            }

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
                    String.format("Incompatibilidade de tipo para '%s'. Esperado: %s", name, expectedType),
                    "INVALID_VARIABLE_TYPE"
            );
        }
    }

    private boolean isValidDate(Object value) {
        if (value instanceof String str) {
            try {
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