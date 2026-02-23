package com.vaas.templateengine.domain.event;

import java.time.OffsetDateTime;

/**
 * Interface selada base para todos os eventos de domínio do sistema.
 * Define o contrato mínimo de rastreabilidade para o sistema de mensageria.
 * Conforme as especificações do Java 21, as implementações permitidas devem estar no mesmo pacote.
 */
public sealed interface DomainEvent
        permits TemplateCreatedEvent, TemplateVersionPublishedEvent, NotificationDispatchedEvent, TemplateArchivedEvent {

    /**
     * Retorna o identificador único do agregado (template) que originou o evento.
     * @return ID do agregado.
     */
    String aggregateId();

    /**
     * Retorna o timestamp absoluto da ocorrência do evento em conformidade com ISO-8601.
     * @return Data e hora da ocorrência.
     */
    OffsetDateTime occurredAt();
}