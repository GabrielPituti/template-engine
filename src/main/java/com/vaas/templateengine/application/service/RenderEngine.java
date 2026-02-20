package com.vaas.templateengine.application.service;

import com.vaas.templateengine.shared.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Motor de Renderização: Responsável por substituir placeholders {{var}} por valores reais.
 * Utiliza Regex para alta performance e segurança.
 */
@Component
public class RenderEngine {

    // Regex para encontrar padrões do tipo {{variavel}}
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{(.+?)\\}\\}");

    /**
     * Renderiza o conteúdo substituindo as variáveis.
     * * @param content O corpo do template com placeholders
     * @param variables Mapa de chave/valor com os dados para preenchimento
     * @return Conteúdo processado
     */
    public String render(String content, Map<String, Object> variables) {
        if (content == null || content.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(content);

        while (matcher.find()) {
            String key = matcher.group(1).trim();
            Object value = variables.get(key);

            if (value == null) {
                // Decisão técnica: Se a variável faltar, lançamos erro clínico
                throw new BusinessException(
                        "Variável obrigatória ausente no preenchimento: " + key,
                        "MISSING_REQUIRED_VARIABLE"
                );
            }

            matcher.appendReplacement(sb, Matcher.quoteReplacement(value.toString()));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }
}