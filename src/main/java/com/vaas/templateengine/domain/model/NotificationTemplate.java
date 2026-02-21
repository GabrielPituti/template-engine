package com.vaas.templateengine.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private String orgId;
    private String workspaceId;
    private TemplateStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Optimistic Locking
    // Evita Race Conditions. Se dois processos tentarem atualizar o mesmo documento,
    // o Spring Data lança uma OptimisticLockingFailureException.
    @Version
    private Long internalVersion;

    @Builder.Default
    private List<TemplateVersion> versions = new ArrayList<>();

    public void addVersion(TemplateVersion version) {
        if (this.versions == null) {
            this.versions = new ArrayList<>();
        }
        this.versions.add(version);
    }
}