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
 * Gerencia o ciclo de vida, versionamento e isolamento multi-tenant, protegendo o estado
 * interno contra mutações inconsistentes via métodos de domínio explícitos.
 */
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notification_templates")
public class NotificationTemplate {

    @Id
    private String id;

    private String name;

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

    /**
     * Mecanismo de Optimistic Locking para garantir a integridade dos dados
     * em ambientes distribuídos com alta concorrência de edição.
     */
    @Version
    private Long internalVersion;

    @Builder.Default
    private List<TemplateVersion> versions = new ArrayList<>();

    /**
     * Atualiza os metadados informativos do template.
     * Implementa validações de estado para garantir que templates arquivados
     * permaneçam imutáveis para fins de histórico e conformidade.
     */
    public void updateInformation(String name, String description) {
        if (this.status == TemplateStatus.ARCHIVED) {
            throw new BusinessException("Templates arquivados não permitem alterações cadastrais.", "TEMPLATE_ARCHIVED");
        }
        if (name == null || name.isBlank()) {
            throw new BusinessException("O nome do template é um campo obrigatório.", "INVALID_ARGUMENT");
        }
        this.name = name;
        this.description = description;
        this.updatedAt = OffsetDateTime.now();
    }

    /**
     * Registra uma nova iteração de conteúdo no histórico do template.
     * Bloqueia a operação caso o agregado esteja em estado terminal (Archived).
     */
    public void addVersion(TemplateVersion version) {
        if (this.status == TemplateStatus.ARCHIVED) {
            throw new BusinessException("Templates arquivados não permitem a inclusão de novas versões.", "TEMPLATE_ARCHIVED");
        }
        if (this.versions == null) {
            this.versions = new ArrayList<>();
        }
        this.versions.add(version);
        this.updatedAt = OffsetDateTime.now();
    }

    /**
     * Realiza o arquivamento lógico (Soft Delete) do recurso.
     * Preserva a integridade referencial com os logs de execução passados.
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
                .orElseThrow(() -> new BusinessException("Identificador de versão não localizado: " + versionId, "VERSION_NOT_FOUND"));
    }

    /**
     * Localiza a versão mais atual apta para processamento em produção.
     */
    public TemplateVersion getLatestPublishedVersion() {
        return this.versions.stream()
                .filter(TemplateVersion::isPublished)
                .max(TemplateVersion::compareTo)
                .orElseThrow(() -> new BusinessException("Nenhuma versão publicada disponível para execução.", "NO_PUBLISHED_VERSION"));
    }
}