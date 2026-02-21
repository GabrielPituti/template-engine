package com.vaas.templateengine.domain.event;

import java.time.OffsetDateTime;

/**
 * Evento disparado imediatamente após a criação de um novo template no sistema.
 */
public record TemplateCreatedEvent(
        String aggregateId,
        OffsetDateTime occurredAt,
        String name
) implements DomainEvent {}