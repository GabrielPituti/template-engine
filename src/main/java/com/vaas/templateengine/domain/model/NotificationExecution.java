package com.vaas.templateengine.domain.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Registro de Auditoria e Conformidade (Audit Log).
 * Esta entidade representa um snapshot imutável de uma execução de renderização.
 * Uma vez criada, ela serve como evidência histórica do conteúdo disparado,
 * destinatários e estado resultante, sendo fundamental para processos de compliance.
 */
@Getter
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

    /**
     * Snapshot das variáveis fornecidas pelo cliente no momento exato da requisição.
     * Este estado é preservado para garantir a rastreabilidade em auditorias futuras.
     */
    private Map<String, Object> variables;

    private String renderedContent;

    private ExecutionStatus status;

    private OffsetDateTime executedOn;
}