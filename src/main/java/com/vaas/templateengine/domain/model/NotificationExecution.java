package com.vaas.templateengine.domain.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Entidade de Auditoria: Log de cada execução de renderização.
 * Registra o estado exato da notificação no momento do disparo para fins de conformidade.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notification_executions")
public class NotificationExecution {

    /** Identificador único da execução (UUID) */
    @Id
    private String id;

    /** Referência ao template utilizado */
    private String templateId;

    /** Referência à versão específica do template no momento da execução */
    private String versionId;

    /** Lista de destinatários (e-mails, números de telefone ou URLs de webhook) */
    private List<String> recipients;

    /** Snapshot das variáveis fornecidas pelo cliente no momento da requisição */
    private Map<String, Object> variables;

    /** Conteúdo final renderizado após o processamento das variáveis */
    private String renderedContent;

    /** Resultado final da operação (Sucesso ou erro de validação) */
    private ExecutionStatus status;

    /** Timestamp absoluto da execução com informação de fuso horário */
    private OffsetDateTime executedOn;
}