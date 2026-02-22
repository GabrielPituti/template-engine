package com.vaas.templateengine.domain.model;

import com.vaas.templateengine.shared.exception.BusinessException;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Entidade que representa uma iteração do template.
 * Implementa regras rigorosas de imutabilidade para garantir a integridade histórica.
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
     * Atualiza o conteúdo da versão apenas se ela ainda for um rascunho (Bug Claude #1).
     * @param body Novo corpo do template.
     * @param subject Novo assunto.
     * @param inputSchema Novo schema de variáveis.
     * @param changelog Descrição da alteração.
     */
    public void updateContent(String body, String subject, List<InputVariable> inputSchema, String changelog) {
        if (isPublished()) {
            throw new BusinessException("Versão publicada é imutável e não pode ser editada.", "VERSION_IMMUTABLE");
        }
        this.body = body;
        this.subject = subject;
        this.inputSchema = inputSchema;
        this.changelog = changelog;
    }

    public void publish() {
        if (isPublished()) {
            throw new BusinessException("Esta versão já está publicada.", "VERSION_ALREADY_PUBLISHED");
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