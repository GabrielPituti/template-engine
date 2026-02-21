package com.vaas.templateengine.application.service;

import com.vaas.templateengine.shared.exception.BusinessException;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Motor de renderização otimizado para alta performance e baixa alocação de memória.
 * Implementa salvaguardas contra ataques de ReDoS e negação de serviço por estouro de conteúdo.
 */
@Component
public class RenderEngine {

    /** * Expressão regular não-gananciosa para identificação de placeholders.
     * O uso de '.+?' aliado à validação de tamanho previne o backtracking catastrófico (ReDoS).
     */
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{(.+?)\\}\\}");

    /** * Limite de segurança para o tamanho do conteúdo (50KB).
     * Impede o processamento de strings excessivamente longas que poderiam causar exaustão de CPU ou Memória.
     */
    private static final int MAX_CONTENT_LENGTH = 50_000;

    /**
     * Processa a substituição de placeholders por valores reais do contexto de execução.
     * * @param content Texto bruto do template contendo os placeholders.
     * @param variables Mapa contendo as chaves e valores para preenchimento.
     * @param shouldEscapeHtml Define se deve aplicar sanitização contra injeção de script (XSS).
     * @return Conteúdo processado, com as variáveis interpoladas e seguro para o canal de saída.
     * @throws BusinessException Caso o conteúdo exceda limites de segurança ou faltem variáveis obrigatórias.
     */
    public String render(String content, Map<String, Object> variables, boolean shouldEscapeHtml) {
        if (content == null || content.isEmpty()) {
            return "";
        }

        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new BusinessException(
                    "O conteúdo do template excede o limite de segurança permitido.",
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
                        "Variável obrigatória ausente no contexto de renderização: " + key,
                        "MISSING_REQUIRED_VARIABLE"
                );
            }

            String stringValue = value.toString();

            if (shouldEscapeHtml) {
                stringValue = HtmlUtils.htmlEscape(stringValue);
            }

            matcher.appendReplacement(sb, Matcher.quoteReplacement(stringValue));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }
}