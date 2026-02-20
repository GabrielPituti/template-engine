package com.vaas.templateengine.application.service;

import com.vaas.templateengine.domain.model.InputVariable;
import com.vaas.templateengine.domain.model.VariableType;
import com.vaas.templateengine.shared.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Validação de Schema (SchemaValidator)")
class SchemaValidatorTest {

    private final SchemaValidator validator = new SchemaValidator();

    @Test
    @DisplayName("Deve validar com sucesso tipos corretos")
    void shouldValidateCorrectTypes() {
        List<InputVariable> schema = List.of(
                new InputVariable("idade", VariableType.NUMBER, true),
                new InputVariable("ativo", VariableType.BOOLEAN, true),
                new InputVariable("nome", VariableType.STRING, false)
        );

        Map<String, Object> input = Map.of(
                "idade", 25,
                "ativo", true,
                "nome", "Gabriel"
        );

        assertDoesNotThrow(() -> validator.validate(schema, input));
    }

    @Test
    @DisplayName("Deve lançar erro para variável obrigatória ausente")
    void shouldThrowExceptionForMissingRequiredVariable() {
        List<InputVariable> schema = List.of(
                new InputVariable("token", VariableType.STRING, true)
        );

        Map<String, Object> input = Map.of("outra", "coisa");

        BusinessException ex = assertThrows(BusinessException.class, () ->
                validator.validate(schema, input)
        );

        assertEquals("MISSING_REQUIRED_VARIABLE", ex.getCode());
    }

    @Test
    @DisplayName("Deve lançar erro para tipo NUMBER inválido")
    void shouldThrowExceptionForInvalidNumber() {
        List<InputVariable> schema = List.of(
                new InputVariable("valor", VariableType.NUMBER, true)
        );

        Map<String, Object> input = Map.of("valor", "cem");

        BusinessException ex = assertThrows(BusinessException.class, () ->
                validator.validate(schema, input)
        );

        assertEquals("INVALID_VARIABLE_TYPE", ex.getCode());
    }

    @Test
    @DisplayName("Deve validar datas no formato ISO-8601")
    void shouldValidateIsoDates() {
        List<InputVariable> schema = List.of(
                new InputVariable("data", VariableType.DATE, true)
        );

        assertTrue(assertDoesNotThrow(() -> {
            validator.validate(schema, Map.of("data", "2026-02-20"));
            validator.validate(schema, Map.of("data", "2026-02-20T19:40:00Z"));
            return true;
        }));
    }
}