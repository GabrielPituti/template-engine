package com.vaas.templateengine.application.service;

import com.vaas.templateengine.shared.exception.BusinessException;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Componente responsável pela interpolação de variáveis em templates.
 * Utiliza expressões regulares compiladas para garantir alta performance sob carga.
 */
@Component
public class RenderEngine {

    /** Regex para identificar padrões {{variavel}} */
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{(.+?)\\}\\}");

    /**
     * Renderiza o conteúdo substituindo placeholders por valores do contexto.
     * @param content O texto bruto do template.
     * @param variables Mapa de dados para preenchimento.
     * @param shouldEscapeHtml Se verdadeiro, aplica escape de caracteres HTML para prevenir XSS.
     * @return Conteúdo final processado.
     * @throws BusinessException Caso uma variável obrigatória no template não seja fornecida.
     */
    public String render(String content, Map<String, Object> variables, boolean shouldEscapeHtml) {
        if (content == null || content.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(content);

        while (matcher.find()) {
            String key = matcher.group(1).trim();
            Object value = variables.get(key);

            if (value == null) {
                throw new BusinessException(
                        "Variável obrigatória ausente no contexto de renderização: " + key,
                        "MISSING_REQUIRED_VARIABLE"
                );
            }

            String stringValue = value.toString();

            // Mitigação de ataques XSS para canais que interpretam HTML (ex: E-mail)
            if (shouldEscapeHtml) {
                stringValue = HtmlUtils.htmlEscape(stringValue);
            }

            // QuoteReplacement garante que caracteres especiais no valor não quebrem a regex
            matcher.appendReplacement(sb, Matcher.quoteReplacement(stringValue));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }
}