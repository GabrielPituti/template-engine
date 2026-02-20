package com.vaas.templateengine.domain.model;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidade Filha: Representa uma versão específica do template.
 * Conforme RF01: Inclui o 'estado' (DRAFT/PUBLISHED) e o 'inputSchema'.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateVersion {
    private String id;
    private SemanticVersion version;
    private String subject; // Utilizado apenas para canal EMAIL
    private String body; // Conteúdo com placeholders {{variavel}}
    private List<InputVariable> inputSchema;
    private VersionState estado; // Nome exato solicitado: DRAFT ou PUBLISHED
    private String changelog;
    private LocalDateTime createdAt;

    public boolean isPublished() {
        return VersionState.PUBLISHED.equals(this.estado);
    }
}