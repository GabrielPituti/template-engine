package com.vaas.templateengine.domain.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregate Root que representa um template de notificação.
 * Conforme RF01: Suporta multi-tenancy e soft delete.
 */
@Getter
@Setter
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

    // Identificadores para isolamento de dados (Multi-tenancy)
    private String orgId;
    private String workspaceId;

    private TemplateStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt; // Utilizado para Soft Delete

    private List<TemplateVersion> versions;

    @Version
    private Long internalVersion; // Optimistic Locking para evitar race conditions

    public void addVersion(TemplateVersion templateVersion) {
        if (this.versions == null) {
            this.versions = new ArrayList<>();
        }
        this.versions.add(templateVersion);
    }
}