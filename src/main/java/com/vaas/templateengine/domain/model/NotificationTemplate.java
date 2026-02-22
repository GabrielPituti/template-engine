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
 * Agregado Raiz (Aggregate Root) que representa um Template de Notificação.
 * Gerencia o ciclo de vida, versionamento e garante a integridade das regras de negócio.
 */
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notification_templates")
public class NotificationTemplate {

    @Id
    private String id;

    /** Nome do template - Mutação permitida via setter controlado */
    @Setter
    private String name;

    /** Descrição do template - Mutação permitida via setter controlado */
    @Setter
    private String description;

    private Channel channel;

    private String orgId;

    private String workspaceId;

    /** Status controlado: apenas métodos de domínio como archive() podem alterar */
    @Setter(AccessLevel.PRIVATE)
    private TemplateStatus status;

    private OffsetDateTime createdAt;

    @Setter
    private OffsetDateTime updatedAt;

    private OffsetDateTime deletedAt;

    /** Controle de concorrência otimista para evitar Race Conditions */
    @Version
    private Long internalVersion;

    @Builder.Default
    private List<TemplateVersion> versions = new ArrayList<>();

    /**
     * Adiciona uma nova versão ao agregado, validando o estado do template.
     * @param version A versão a ser adicionada.
     * @throws BusinessException se o template estiver arquivado.
     */
    public void addVersion(TemplateVersion version) {
        if (this.status == TemplateStatus.ARCHIVED) {
            throw new BusinessException("Não é possível adicionar versões a um template arquivado.", "TEMPLATE_ARCHIVED");
        }
        if (this.versions == null) {
            this.versions = new ArrayList<>();
        }
        this.versions.add(version);
        this.updatedAt = OffsetDateTime.now();
    }

    /**
     * Realiza o arquivamento (Soft Delete) do template conforme RF01.
     */
    public void archive() {
        this.status = TemplateStatus.ARCHIVED;
        this.deletedAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    /**
     * Localiza uma versão por ID dentro da lista de versões do agregado.
     */
    public TemplateVersion getVersion(String versionId) {
        return this.versions.stream()
                .filter(v -> v.getId().equals(versionId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Versão não encontrada: " + versionId, "VERSION_NOT_FOUND"));
    }

    /**
     * Recupera a última versão que foi marcada como PUBLISHED.
     */
    public TemplateVersion getLatestPublishedVersion() {
        return this.versions.stream()
                .filter(TemplateVersion::isPublished)
                .max(TemplateVersion::compareTo)
                .orElseThrow(() -> new BusinessException("Nenhuma versão publicada disponível.", "NO_PUBLISHED_VERSION"));
    }
}