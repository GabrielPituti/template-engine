package com.vaas.templateengine.domain.event;

import java.time.OffsetDateTime;

/**
 * Definição selada dos eventos de domínio do sistema.
 * Utiliza o recurso de 'sealed interfaces' do Java 21 para garantir que apenas eventos
 * conhecidos e autorizados sejam processados pelo sistema de mensageria.
 */
public sealed interface DomainEvent
        permits TemplateCreatedEvent, TemplateVersionPublishedEvent, NotificationDispatchedEvent, TemplateArchivedEvent {

    /**
     * Retorna o identificador único do agregado que originou o evento.
     * @return ID do agregado.
     */
    String aggregateId();

    /**
     * Retorna o timestamp absoluto da ocorrência do evento em conformidade com ISO-8601.
     * @return Data e hora da ocorrência.
     */
    OffsetDateTime occurredAt();
}

/**
 * Evento gerado na criação de um novo template base.
 * @param aggregateId ID do template criado.
 * @param occurredAt Timestamp da criação.
 * @param name Nome do template.
 */
record TemplateCreatedEvent(String aggregateId, OffsetDateTime occurredAt, String name) implements DomainEvent {}

/**
 * Evento gerado quando uma versão de template é publicada com sucesso.
 * @param aggregateId ID do template.
 * @param occurredAt Timestamp da publicação.
 * @param versionId ID da versão publicada.
 */
record TemplateVersionPublishedEvent(String aggregateId, OffsetDateTime occurredAt, String versionId) implements DomainEvent {}

/**
 * Evento gerado após a execução e renderização de uma notificação.
 * @param aggregateId ID do template executado.
 * @param occurredAt Timestamp da execução.
 * @param status Status final do despacho.
 */
record NotificationDispatchedEvent(String aggregateId, OffsetDateTime occurredAt, String status) implements DomainEvent {
    /**
     * Construtor de conveniência que define automaticamente o timestamp de ocorrência como o instante atual.
     * @param aggregateId ID do template.
     * @param status Status do despacho.
     */
    public NotificationDispatchedEvent(String aggregateId, String status) {
        this(aggregateId, OffsetDateTime.now(), status);
    }
}

/**
 * Evento gerado quando um template é arquivado (Soft Delete).
 * @param aggregateId ID do template arquivado.
 * @param occurredAt Timestamp do arquivamento.
 */
record TemplateArchivedEvent(String aggregateId, OffsetDateTime occurredAt) implements DomainEvent {}