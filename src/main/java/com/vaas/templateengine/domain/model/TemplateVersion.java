package com.vaas.templateengine.domain.model;

import com.vaas.templateengine.shared.exception.BusinessException;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Representa uma iteração específica do conteúdo e contrato de dados de um template.
 * Implementa garantias de imutabilidade para assegurar que versões já utilizadas
 * em produção permaneçam como registros históricos fidedignos.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateVersion implements Comparable<TemplateVersion> {

    private String id;
    private SemanticVersion version;
    private String subject;
    private String body;
    private List<InputVariable> inputSchema;

    @Setter(AccessLevel.PRIVATE)
    private VersionState estado;

    private String changelog;
    private OffsetDateTime createdAt;

    /**
     * Realiza a atualização controlada do conteúdo da versão.
     * Esta mutação é permitida apenas enquanto a versão reside em estado de rascunho (DRAFT).
     */
    public void updateContent(String body, String subject, List<InputVariable> inputSchema, String changelog) {
        if (isPublished()) {
            throw new BusinessException("Versões publicadas são imutáveis.", "VERSION_IMMUTABLE");
        }
        this.body = body;
        this.subject = subject;
        this.inputSchema = inputSchema;
        this.changelog = changelog;
    }

    /**
     * Transforma a versão em um artefato imutável pronto para execução.
     */
    public void publish() {
        if (isPublished()) {
            throw new BusinessException("A versão já se encontra publicada.", "VERSION_ALREADY_PUBLISHED");
        }
        this.estado = VersionState.PUBLISHED;
    }

    public boolean isPublished() {
        return VersionState.PUBLISHED.equals(this.estado);
    }

    @Override
    public int compareTo(TemplateVersion other) {
        return this.version.compareTo(other.version);
    }
}