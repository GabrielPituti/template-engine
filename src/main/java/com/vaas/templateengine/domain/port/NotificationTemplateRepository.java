package com.vaas.templateengine.domain.port;

import com.vaas.templateengine.domain.model.Channel;
import com.vaas.templateengine.domain.model.NotificationTemplate;
import com.vaas.templateengine.domain.model.TemplateStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Port (Interface de Saída): Define como o domínio deseja salvar e buscar templates.
 * Conforme RF01: Suporta filtros por canal e status com paginação.
 */
public interface NotificationTemplateRepository {

    NotificationTemplate save(NotificationTemplate template);

    Optional<NotificationTemplate> findById(String id);

    Page<NotificationTemplate> findAll(
            String orgId,
            String workspaceId,
            Channel channel,
            TemplateStatus status,
            Pageable pageable
    );

    void deleteById(String id);
}