package com.vaas.templateengine.domain.event;

import java.time.OffsetDateTime;

/**
 * Evento disparado quando um template é arquivado (soft delete).
 */
public record TemplateArchivedEvent(
        String aggregateId,
        OffsetDateTime occurredAt
) implements DomainEvent {}