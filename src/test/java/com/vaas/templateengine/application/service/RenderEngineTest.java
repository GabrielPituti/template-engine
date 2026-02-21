package com.vaas.templateengine.application.service;

import com.vaas.templateengine.shared.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para validação do motor de interpolação de strings.
 */
@DisplayName("Motor de Renderização (RenderEngine)")
class RenderEngineTest {

    private final RenderEngine renderEngine = new RenderEngine();

    /**
     * Verifica a substituição correta de múltiplos placeholders simples.
     */
    @Test
    @DisplayName("Deve substituir variáveis simples com sucesso")
    void shouldRenderSimpleVariables() {
        String content = "Olá {{nome}}, bem-vindo ao {{sistema}}!";
        Map<String, Object> variables = Map.of(
                "nome", "Gabriel",
                "sistema", "VaaS"
        );

        String result = renderEngine.render(content, variables, false);

        assertEquals("Olá Gabriel, bem-vindo ao VaaS!", result);
    }

    /**
     * Valida a sanitização de entradas para evitar ataques de Cross-Site Scripting (XSS).
     */
    @Test
    @DisplayName("Deve escapar HTML quando solicitado (Proteção XSS)")
    void shouldEscapeHtmlWhenRequested() {
        String content = "Comentário: {{user_input}}";
        Map<String, Object> variables = Map.of("user_input", "<script>alert(1)</script>");

        String result = renderEngine.render(content, variables, true);

        assertTrue(result.contains("&lt;script&gt;"));
        assertFalse(result.contains("<script>"));
    }

    /**
     * Garante que a ausência de chaves obrigatórias interrompa o processo de renderização.
     */
    @Test
    @DisplayName("Deve lançar exceção quando uma variável do template não é fornecida")
    void shouldThrowExceptionWhenVariableIsMissing() {
        String content = "Seu código é {{code}}";
        Map<String, Object> variables = Map.of("outro", "valor");

        BusinessException exception = assertThrows(BusinessException.class, () ->
                renderEngine.render(content, variables, false)
        );

        assertEquals("MISSING_REQUIRED_VARIABLE", exception.getCode());
    }

    /**
     * Verifica a resiliência do motor perante entradas nulas ou vazias.
     */
    @Test
    @DisplayName("Deve lidar com conteúdo vazio ou nulo")
    void shouldHandleEmptyContent() {
        assertEquals("", renderEngine.render("", Map.of(), false));
        assertEquals("", renderEngine.render(null, Map.of(), false));
    }
}