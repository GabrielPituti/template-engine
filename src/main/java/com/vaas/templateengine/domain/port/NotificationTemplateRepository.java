package com.vaas.templateengine.domain.port;

import com.vaas.templateengine.domain.model.Channel;
import com.vaas.templateengine.domain.model.NotificationTemplate;
import com.vaas.templateengine.domain.model.TemplateStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Port (Interface de Saída): Define o contrato de persistência para templates.
 */
public interface NotificationTemplateRepository {

    NotificationTemplate save(NotificationTemplate template);

    Optional<NotificationTemplate> findById(String id);

    /**
     * Busca templates de forma paginada aplicando filtros de organização, workspace, canal e status.
     */
    Page<NotificationTemplate> findAll(
            String orgId,
            String workspaceId,
            Channel channel,
            TemplateStatus status,
            Pageable pageable
    );

    void deleteById(String id);
}