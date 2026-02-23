package com.vaas.templateengine.application.service;

import com.vaas.templateengine.application.dto.TemplateMapper;
import com.vaas.templateengine.domain.event.*;
import com.vaas.templateengine.domain.model.*;
import com.vaas.templateengine.domain.port.NotificationExecutionRepository;
import com.vaas.templateengine.domain.port.NotificationTemplateRepository;
import com.vaas.templateengine.infrastructure.messaging.NotificationProducer;
import com.vaas.templateengine.infrastructure.persistence.TemplateStatsRepository;
import com.vaas.templateengine.shared.exception.BusinessException;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Maestro da lógica de aplicação.
 * Orquestra as transações ACID entre o domínio rico e os adaptadores de infraestrutura,
 * garantindo a emissão de eventos e métricas operacionais.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateService {

    private final NotificationTemplateRepository templateRepository;
    private final NotificationExecutionRepository executionRepository;
    private final TemplateStatsRepository statsRepository;
    private final SchemaValidator schemaValidator;
    private final RenderEngine renderEngine;
    private final NotificationProducer eventProducer;
    private final MeterRegistry meterRegistry;

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

        TemplateVersion initial = TemplateVersion.builder()
                .id(UUID.randomUUID().toString())
                .version(SemanticVersion.initial())
                .estado(VersionState.DRAFT)
                .subject("Novo Template: " + name)
                .body("Olá {{nome}}")
                .inputSchema(List.of(new InputVariable("nome", VariableType.STRING, true)))
                .changelog("Criação inicial.")
                .createdAt(OffsetDateTime.now())
                .build();

        template.addVersion(initial);
        NotificationTemplate saved = templateRepository.save(template);
        eventProducer.publish(new TemplateCreatedEvent(saved.getId(), OffsetDateTime.now(), saved.getName()));
        return saved;
    }

    public Page<NotificationTemplate> listTemplates(
            String orgId, String workspaceId, Channel channel, TemplateStatus status, Pageable pageable) {
        return templateRepository.findAll(orgId, workspaceId, channel, status, pageable);
    }

    @Transactional
    @CacheEvict(value = "templates", key = "#id")
    public NotificationTemplate createVersion(String id, TemplateMapper.CreateVersionRequest request, List<InputVariable> schema) {
        NotificationTemplate template = getById(id);
        TemplateVersion lastVersion = template.getVersions().stream()
                .max(TemplateVersion::compareTo)
                .orElseThrow();

        SemanticVersion next = request.isMinor() ? lastVersion.getVersion().nextMinor() : lastVersion.getVersion().nextPatch();

        TemplateVersion newVersion = TemplateVersion.builder()
                .id(UUID.randomUUID().toString())
                .version(next)
                .estado(VersionState.DRAFT)
                .subject(request.subject())
                .body(request.body())
                .inputSchema(schema)
                .changelog(request.changelog())
                .createdAt(OffsetDateTime.now())
                .build();

        template.addVersion(newVersion);
        return templateRepository.save(template);
    }

    @Transactional
    @CacheEvict(value = "templates", key = "#templateId")
    public NotificationTemplate updateVersion(String templateId, String versionId, String body, String subject, List<InputVariable> schema, String changelog) {
        NotificationTemplate template = getById(templateId);
        TemplateVersion version = template.getVersion(versionId);

        version.updateContent(body, subject, schema, changelog);
        template.setUpdatedAt(OffsetDateTime.now());

        return templateRepository.save(template);
    }

    @Cacheable(value = "templates", key = "#id")
    public NotificationTemplate getById(String id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Template não encontrado: " + id, "TEMPLATE_NOT_FOUND"));
    }

    public TemplateStatsView getStats(String templateId) {
        return statsRepository.findById(templateId)
                .orElse(TemplateStatsView.builder().templateId(templateId).build());
    }

    @Transactional
    @CacheEvict(value = "templates", key = "#templateId")
    public NotificationTemplate publishVersion(String templateId, String versionId) {
        NotificationTemplate template = getById(templateId);
        TemplateVersion version = template.getVersion(versionId);

        version.publish();
        NotificationTemplate saved = templateRepository.save(template);

        eventProducer.publish(new TemplateVersionPublishedEvent(templateId, OffsetDateTime.now(), versionId));
        return saved;
    }

    @Transactional
    @CacheEvict(value = "templates", key = "#templateId")
    public void archiveTemplate(String templateId) {
        NotificationTemplate template = getById(templateId);
        template.archive();
        templateRepository.save(template);
        eventProducer.publish(new TemplateArchivedEvent(templateId, OffsetDateTime.now()));
    }

    @Transactional
    public NotificationExecution executeTemplate(String templateId, String versionId, List<String> recipients, Map<String, Object> variables) {
        NotificationTemplate template = getById(templateId);

        if (template.getStatus() == TemplateStatus.ARCHIVED) {
            recordMetric(template, "ARCHIVED_ERROR");
            throw new BusinessException("Operação negada: template arquivado.", "TEMPLATE_ARCHIVED");
        }

        TemplateVersion version = (versionId != null) ? template.getVersion(versionId) : template.getLatestPublishedVersion();

        if (!version.isPublished()) {
            recordMetric(template, "DRAFT_ERROR");
            throw new BusinessException("Versão em rascunho não pode ser executada.", "VERSION_NOT_PUBLISHED");
        }

        ExecutionStatus status = ExecutionStatus.SUCCESS;
        String renderedContent;

        try {
            schemaValidator.validate(version.getInputSchema(), variables);
            renderedContent = renderEngine.render(version.getBody(), variables, template.getChannel() == Channel.EMAIL);
        } catch (BusinessException e) {
            status = ExecutionStatus.VALIDATION_ERROR;
            renderedContent = "Falha de validação técnica.";
        }

        NotificationExecution execution = NotificationExecution.builder()
                .id(UUID.randomUUID().toString())
                .templateId(templateId)
                .versionId(version.getId())
                .recipients(recipients)
                .variables(variables)
                .renderedContent(renderedContent)
                .status(status)
                .executedOn(OffsetDateTime.now())
                .build();

        NotificationExecution saved = executionRepository.save(execution);
        eventProducer.publish(new NotificationDispatchedEvent(templateId, status.name()));
        recordMetric(template, status.name());

        return saved;
    }

    private void recordMetric(NotificationTemplate template, String resultStatus) {
        String channel = template.getChannel() != null ? template.getChannel().name() : "UNKNOWN";
        String orgId = template.getOrgId() != null ? template.getOrgId() : "UNKNOWN";

        meterRegistry.counter("notifications.execution.total",
                "channel", channel,
                "status", resultStatus,
                "orgId", orgId
        ).increment();
    }
}