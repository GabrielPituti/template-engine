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
 * Adapter: Implementação da interface de domínio usando o Spring Data Mongo.
 * Aqui fazemos a ponte entre o 'Port' e o 'Spring Data'.
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

    @Override
    public Page<NotificationTemplate> findAll(String orgId, String workspaceId, Channel channel, TemplateStatus status, Pageable pageable) {
        // No futuro, se channel/status forem nulos, a query trata ou filtramos em memória
        return repository.findByFilters(orgId, workspaceId, channel, status, pageable);
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }
}