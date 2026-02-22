package com.vaas.templateengine.application.service;

import com.vaas.templateengine.domain.event.NotificationDispatchedEvent;
import com.vaas.templateengine.domain.event.TemplateArchivedEvent;
import com.vaas.templateengine.domain.model.*;
import com.vaas.templateengine.domain.port.NotificationExecutionRepository;
import com.vaas.templateengine.domain.port.NotificationTemplateRepository;
import com.vaas.templateengine.infrastructure.messaging.NotificationProducer;
import com.vaas.templateengine.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Serviço de aplicação responsável pela orquestração do ciclo de vida de templates.
 */
@Service
@RequiredArgsConstructor
public class TemplateService {

    private final NotificationTemplateRepository templateRepository;
    private final NotificationExecutionRepository executionRepository;
    private final SchemaValidator schemaValidator;
    private final RenderEngine renderEngine;
    private final NotificationProducer eventProducer;

    @Transactional
    public NotificationTemplate createTemplate(String name, String description, Channel channel, String orgId, String workspaceId) {
        NotificationTemplate template = NotificationTemplate.builder()
                .name(name)
                .description(description)
                .channel(channel)
                .orgId(orgId)
                .workspaceId(workspaceId)
                .status(TemplateStatus.ACTIVE)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        TemplateVersion initialVersion = TemplateVersion.builder()
                .id(UUID.randomUUID().toString())
                .version(SemanticVersion.initial())
                .estado(VersionState.DRAFT)
                .subject("Novo Template: " + name)
                .body("Bem-vindo {{nome}}")
                .inputSchema(List.of(new InputVariable("nome", VariableType.STRING, true)))
                .changelog("Versão inicial gerada automaticamente.")
                .createdAt(OffsetDateTime.now())
                .build();

        template.addVersion(initialVersion);
        return templateRepository.save(template);
    }

    @Cacheable(value = "templates", key = "#id")
    public NotificationTemplate getById(String id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Template não encontrado", "TEMPLATE_NOT_FOUND"));
    }

    @Transactional
    @CacheEvict(value = "templates", key = "#templateId")
    public NotificationTemplate publishVersion(String templateId, String versionId) {
        NotificationTemplate template = getById(templateId);
        TemplateVersion version = template.getVersion(versionId);

        version.publish();
        templateRepository.save(template);

        // Emissão de evento sênior para o Kafka
        eventProducer.publish(new com.vaas.templateengine.domain.event.TemplateVersionPublishedEvent(templateId, OffsetDateTime.now(), versionId));

        return template;
    }

    @Transactional
    @CacheEvict(value = "templates", key = "#templateId")
    public void archiveTemplate(String templateId) {
        // Correção Bug #1: Soft Delete em vez de hard delete.
        NotificationTemplate template = getById(templateId);
        template.archive();
        templateRepository.save(template);

        // Emissão de evento de arquivamento
        eventProducer.publish(new TemplateArchivedEvent(templateId, OffsetDateTime.now()));
    }

    @Transactional
    public NotificationExecution executeTemplate(String templateId, String versionId, List<String> recipients, Map<String, Object> variables) {
        NotificationTemplate template = getById(templateId);

        if (template.getStatus() == TemplateStatus.ARCHIVED) {
            throw new BusinessException("Não é possível executar um template arquivado", "TEMPLATE_ARCHIVED");
        }

        // Correção Bug #4: Validar se a versão específica está PUBLISHED
        TemplateVersion version = findVersionForExecution(template, versionId);

        if (!version.isPublished()) {
            throw new BusinessException("A versão solicitada ainda está em rascunho (DRAFT).", "VERSION_NOT_PUBLISHED");
        }

        ExecutionStatus status = ExecutionStatus.SUCCESS;
        String renderedBody;

        try {
            schemaValidator.validate(version.getInputSchema(), variables);
            boolean shouldEscapeHtml = template.getChannel() == Channel.EMAIL;
            renderedBody = renderEngine.render(version.getBody(), variables, shouldEscapeHtml);
        } catch (BusinessException e) {
            status = ExecutionStatus.VALIDATION_ERROR;
            renderedBody = "Erro de Validação: " + e.getMessage();
        }

        NotificationExecution execution = NotificationExecution.builder()
                .id(UUID.randomUUID().toString())
                .templateId(templateId)
                .versionId(version.getId())
                .recipients(recipients)
                .variables(variables)
                .renderedContent(renderedBody)
                .status(status)
                .executedOn(OffsetDateTime.now())
                .build();

        NotificationExecution saved = executionRepository.save(execution);

        // Dispara evento para atualização das estatísticas (CQRS)
        eventProducer.publish(new NotificationDispatchedEvent(templateId, status.name()));

        return saved;
    }

    private TemplateVersion findVersionForExecution(NotificationTemplate template, String versionId) {
        if (versionId != null) {
            return template.getVersion(versionId);
        }

        return template.getLatestPublishedVersion();
    }
}