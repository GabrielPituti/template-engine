package com.vaas.templateengine.domain.event;

import java.time.OffsetDateTime;

/**
 * Evento disparado quando uma versão específica de um template é publicada.
 * Indica disponibilidade para o motor de renderização.
 */
public record TemplateVersionPublishedEvent(
        String aggregateId,
        OffsetDateTime occurredAt,
        String versionId
) implements DomainEvent {}