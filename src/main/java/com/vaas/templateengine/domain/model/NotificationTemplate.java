package com.vaas.templateengine.domain.model;

import com.vaas.templateengine.shared.exception.BusinessException;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Agregado Raiz (Aggregate Root) que centraliza as regras de negócio de templates.
 * Garante a integridade do ciclo de vida, versionamento e isolamento multi-tenant,
 * protegendo o estado interno contra mutações inconsistentes.
 */
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notification_templates")
public class NotificationTemplate {

    @Id
    private String id;

    @Setter
    private String name;

    @Setter
    private String description;

    private Channel channel;
    private String orgId;
    private String workspaceId;

    @Setter(AccessLevel.PRIVATE)
    private TemplateStatus status;

    private OffsetDateTime createdAt;

    @Setter
    private OffsetDateTime updatedAt;

    private OffsetDateTime deletedAt;

    /** * Controle de concorrência otimista (Optimistic Locking).
     * Previne perda de dados em atualizações simultâneas em ambientes distribuídos.
     */
    @Version
    private Long internalVersion;

    @Builder.Default
    private List<TemplateVersion> versions = new ArrayList<>();

    /**
     * Registra uma nova iteração de conteúdo no histórico do template.
     * Bloqueia a operação caso o template esteja em estado terminal (Archived).
     */
    public void addVersion(TemplateVersion version) {
        if (this.status == TemplateStatus.ARCHIVED) {
            throw new BusinessException("Templates arquivados não permitem novas versões.", "TEMPLATE_ARCHIVED");
        }
        if (this.versions == null) {
            this.versions = new ArrayList<>();
        }
        this.versions.add(version);
        this.updatedAt = OffsetDateTime.now();
    }

    /**
     * Aplica o arquivamento lógico (Soft Delete), preservando a rastreabilidade histórica.
     */
    public void archive() {
        this.status = TemplateStatus.ARCHIVED;
        this.deletedAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    public TemplateVersion getVersion(String versionId) {
        return this.versions.stream()
                .filter(v -> v.getId().equals(versionId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Versão inexistente: " + versionId, "VERSION_NOT_FOUND"));
    }

    /**
     * Seleciona a versão mais recente apta para produção.
     */
    public TemplateVersion getLatestPublishedVersion() {
        return this.versions.stream()
                .filter(TemplateVersion::isPublished)
                .max(TemplateVersion::compareTo)
                .orElseThrow(() -> new BusinessException("Nenhuma versão publicada encontrada.", "NO_PUBLISHED_VERSION"));
    }
}