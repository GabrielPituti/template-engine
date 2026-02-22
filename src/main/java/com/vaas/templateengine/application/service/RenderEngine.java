package com.vaas.templateengine.application.service;

import com.vaas.templateengine.shared.exception.BusinessException;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Motor de interpolação de strings responsável pela resolução de placeholders.
 * A implementação prioriza a segurança contra ataques de negação de serviço (ReDoS)
 * através de expressões regulares não-gananciosas e limites rígidos de carga útil.
 */
@Component
public class RenderEngine {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{(.+?)\\}\\}");
    private static final int MAX_CONTENT_LENGTH = 50_000;

    /**
     * Realiza a substituição dinâmica de placeholders por valores do contexto.
     * Utiliza StringBuilder para otimização de memória e Matcher.quoteReplacement
     * para garantir a integridade de caracteres especiais durante a substituição.
     * * @param content Template bruto com sintaxe {{variavel}}.
     * @param variables Mapa de contexto fornecido para a execução.
     * @param shouldEscapeHtml Ativa a sanitização para proteção contra Cross-Site Scripting (XSS).
     * @return Conteúdo final processado e seguro.
     */
    public String render(String content, Map<String, Object> variables, boolean shouldEscapeHtml) {
        if (content == null || content.isEmpty()) {
            return "";
        }

        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new BusinessException(
                    "O conteúdo excede o limite de segurança operacional.",
                    "TEMPLATE_TOO_LARGE"
            );
        }

        StringBuilder sb = new StringBuilder();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(content);

        while (matcher.find()) {
            String key = matcher.group(1).trim();
            Object value = variables.get(key);

            if (value == null) {
                throw new BusinessException(
                        "Variável obrigatória ausente no contexto: " + key,
                        "MISSING_REQUIRED_VARIABLE"
                );
            }

            String stringValue = value.toString();
            String processedValue = shouldEscapeHtml ? HtmlUtils.htmlEscape(stringValue) : stringValue;

            matcher.appendReplacement(sb, Matcher.quoteReplacement(processedValue));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }
}