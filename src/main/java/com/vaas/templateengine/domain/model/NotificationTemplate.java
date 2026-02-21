package com.vaas.templateengine.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Agregado Raiz (Aggregate Root) que representa um Template de Notificação.
 * Gerencia o ciclo de vida de múltiplas versões e garante o isolamento por inquilino (multi-tenancy).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notification_templates")
public class NotificationTemplate {

    @Id
    private String id;

    private String name;
    private String description;
    private Channel channel;

    /** Identificador da organização proprietária do template (Multi-tenancy) */
    private String orgId;

    /** Identificador do workspace dentro da organização (Multi-tenancy) */
    private String workspaceId;

    private TemplateStatus status;

    /** Data de criação com fuso horário para conformidade global ISO-8601 */
    private OffsetDateTime createdAt;

    /** Data da última modificação do agregado ou de suas versões */
    private OffsetDateTime updatedAt;

    /** Data de exclusão lógica (Soft Delete) */
    private OffsetDateTime deletedAt;

    /** * Controle de concorrência otimista.
     * Impede que duas edições simultâneas causem perda de integridade dos dados.
     */
    @Version
    private Long internalVersion;

    /** Lista histórica de versões do template. A ordem reflete a evolução do conteúdo. */
    @Builder.Default
    private List<TemplateVersion> versions = new ArrayList<>();

    /**
     * Adiciona uma nova versão ao template garantindo a inicialização da lista.
     * @param version A nova versão (DRAFT ou PUBLISHED) a ser anexada.
     */
    public void addVersion(TemplateVersion version) {
        if (this.versions == null) {
            this.versions = new ArrayList<>();
        }
        this.versions.add(version);
    }
}