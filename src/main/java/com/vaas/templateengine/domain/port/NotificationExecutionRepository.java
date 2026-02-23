package com.vaas.templateengine.domain.port;

import com.vaas.templateengine.domain.model.NotificationExecution;

/**
 * Port (Interface de Saída): Define a persistência do log de auditoria das execuções.
 */
public interface NotificationExecutionRepository {
    NotificationExecution save(NotificationExecution execution);
}