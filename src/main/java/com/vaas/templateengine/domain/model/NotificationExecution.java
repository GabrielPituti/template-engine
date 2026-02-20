package com.vaas.templateengine.domain.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Entidade de Auditoria: Log de cada execução de template.
 * Conforme RF02: Persiste detalhes da renderização e status do despacho.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notification_executions")
public class NotificationExecution {
    @Id
    private String id;
    private String templateId;
    private String versionId;
    private List<String> recipients;
    private Map<String, Object> variables;
    private String renderedContent;
    private ExecutionStatus status;
    private LocalDateTime executedOn;
}