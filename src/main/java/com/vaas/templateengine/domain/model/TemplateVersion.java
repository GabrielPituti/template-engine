package com.vaas.templateengine.domain.model;

import com.vaas.templateengine.shared.exception.BusinessException;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Entidade que representa uma versão específica de um template.
 * * Correção Imutabilidade: Removido @Setter público.
 * Apenas métodos de domínio podem alterar o estado (Bug #3).
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
     * Altera o estado da versão para PUBLISHED. Operação única e irreversível.
     */
    public void publish() {
        if (isPublished()) {
            throw new BusinessException("Esta versão já está publicada e é imutável.", "VERSION_ALREADY_PUBLISHED");
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