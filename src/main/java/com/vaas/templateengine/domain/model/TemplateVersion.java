package com.vaas.templateengine.domain.model;

import lombok.*;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Entidade que representa uma versão específica de um template.
 * Versões em estado PUBLISHED são consideradas imutáveis para garantir integridade histórica.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateVersion implements Comparable<TemplateVersion> {

    /** Identificador único da versão */
    private String id;

    /** Versão semântica (Major.Minor.Patch) */
    private SemanticVersion version;

    /** Assunto da notificação (utilizado primordialmente em e-mails) */
    private String subject;

    /** Corpo do template contendo placeholders no formato {{variavel}} */
    private String body;

    /** Definição das variáveis esperadas para a correta renderização desta versão */
    private List<InputVariable> inputSchema;

    /** Estado atual da versão (DRAFT ou PUBLISHED) */
    private VersionState estado;

    /** Registro descritivo das alterações realizadas nesta versão */
    private String changelog;

    /** Data de criação da versão com precisão de fuso horário */
    private OffsetDateTime createdAt;

    /**
     * Verifica se a versão já foi publicada.
     * @return true se o estado for PUBLISHED.
     */
    public boolean isPublished() {
        return VersionState.PUBLISHED.equals(this.estado);
    }

    /**
     * Permite a ordenação de versões dentro do agregado de template.
     */
    @Override
    public int compareTo(TemplateVersion other) {
        return this.version.compareTo(other.version);
    }
}