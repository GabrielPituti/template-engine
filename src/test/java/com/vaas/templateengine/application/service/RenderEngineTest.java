package com.vaas.templateengine.application.service;

import com.vaas.templateengine.shared.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Motor de Renderização (RenderEngine)")
class RenderEngineTest {

    private final RenderEngine renderEngine = new RenderEngine();

    @Test
    @DisplayName("Deve substituir variáveis simples com sucesso")
    void shouldRenderSimpleVariables() {
        String content = "Olá {{nome}}, bem-vindo ao {{sistema}}!";
        Map<String, Object> variables = Map.of(
                "nome", "Gabriel",
                "sistema", "VaaS"
        );

        String result = renderEngine.render(content, variables);

        assertEquals("Olá Gabriel, bem-vindo ao VaaS!", result);
    }

    @Test
    @DisplayName("Deve lançar exceção quando uma variável do template não é fornecida")
    void shouldThrowExceptionWhenVariableIsMissing() {
        String content = "Seu código é {{code}}";
        Map<String, Object> variables = Map.of("outro", "valor");

        BusinessException exception = assertThrows(BusinessException.class, () ->
                renderEngine.render(content, variables)
        );

        assertEquals("MISSING_REQUIRED_VARIABLE", exception.getCode());
    }

    @Test
    @DisplayName("Deve lidar com conteúdo vazio ou nulo")
    void shouldHandleEmptyContent() {
        assertEquals("", renderEngine.render("", Map.of()));
        assertEquals("", renderEngine.render(null, Map.of()));
    }
}