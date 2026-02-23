package com.vaas.templateengine.domain.event;

import java.time.OffsetDateTime;

/**
 * Evento disparado após a execução e renderização de uma notificação.
 * Utilizado para fluxos de auditoria e métricas de envio.
 */
public record NotificationDispatchedEvent(
        String aggregateId,
        OffsetDateTime occurredAt,
        String status
) implements DomainEvent {
    /**
     * Construtor de conveniência que define automaticamente o timestamp de ocorrência.
     * @param aggregateId ID do template executado.
     * @param status Status do despacho (SUCCESS ou VALIDATION_ERROR).
     */
    public NotificationDispatchedEvent(String aggregateId, String status) {
        this(aggregateId, OffsetDateTime.now(), status);
    }
}