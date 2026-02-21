package com.vaas.templateengine.infrastructure.persistence;

import com.vaas.templateengine.domain.model.Channel;
import com.vaas.templateengine.domain.model.NotificationTemplate;
import com.vaas.templateengine.domain.model.TemplateStatus;
import com.vaas.templateengine.domain.port.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adapter que conecta o Port de domínio à implementação específica do Spring Data MongoDB.
 * Encapsula a lógica de tradução entre o domínio e a infraestrutura.
 */
@Component
@RequiredArgsConstructor
public class TemplateRepositoryAdapter implements NotificationTemplateRepository {

    private final SpringDataMongoTemplateRepository repository;

    @Override
    public NotificationTemplate save(NotificationTemplate template) {
        return repository.save(template);
    }

    @Override
    public Optional<NotificationTemplate> findById(String id) {
        return repository.findById(id);
    }

    /**
     * Implementação da busca filtrada utilizando a query dinâmica do repositório Mongo.
     */
    @Override
    public Page<NotificationTemplate> findAll(
            String orgId,
            String workspaceId,
            Channel channel,
            TemplateStatus status,
            Pageable pageable) {
        return repository.findByFilters(orgId, workspaceId, channel, status, pageable);
    }

    @Override
    public void deleteById(String id) {
        // Implementação de Soft Delete conforme RF01
        repository.findById(id).ifPresent(template -> {
            template.setStatus(TemplateStatus.ARCHIVED);
            repository.save(template);
        });
    }
}