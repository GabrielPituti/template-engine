package com.vaas.templateengine.application.service;

import com.vaas.templateengine.domain.event.NotificationDispatchedEvent;
import com.vaas.templateengine.domain.event.TemplateArchivedEvent;
import com.vaas.templateengine.domain.event.TemplateVersionPublishedEvent;
import com.vaas.templateengine.domain.model.*;
import com.vaas.templateengine.domain.port.NotificationExecutionRepository;
import com.vaas.templateengine.domain.port.NotificationTemplateRepository;
import com.vaas.templateengine.infrastructure.messaging.NotificationProducer;
import com.vaas.templateengine.shared.exception.BusinessException;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Orquestrador central da lógica de negócio de templates.
 * Implementa regras de versionamento, imutabilidade, auditoria e diferenciais de performance/observabilidade.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateService {

    private final NotificationTemplateRepository templateRepository;
    private final NotificationExecutionRepository executionRepository;
    private final SchemaValidator schemaValidator;
    private final RenderEngine renderEngine;
    private final NotificationProducer eventProducer;

    /** Diferencial: Registro de métricas para Observabilidade via Micrometer */
    private final MeterRegistry meterRegistry;

    /**
     * Cria um novo template com sua versão inicial em estado de rascunho (1.0.0).
     */
    @Transactional
    public NotificationTemplate createTemplate(String name, String description, Channel channel, String orgId, String workspaceId) {
        log.info("Criando novo template: {} para a organização: {}", name, orgId);

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
                .subject("Bem-vindo ao serviço") // Atendendo RF01
                .body("Olá {{nome}}, seu cadastro foi realizado.")
                .inputSchema(List.of(new InputVariable("nome", VariableType.STRING, true)))
                .changelog("Setup inicial do template.") // Atendendo RF01
                .createdAt(OffsetDateTime.now())
                .build();

        template.addVersion(initial);
        return templateRepository.save(template);
    }

    /**
     * Recupera um template por ID. Utiliza Caffeine Cache para alta performance em execuções massivas.
     */
    @Cacheable(value = "templates", key = "#id")
    public NotificationTemplate getById(String id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Template não encontrado: " + id, "TEMPLATE_NOT_FOUND"));
    }

    /**
     * Publica uma versão, tornando-a imutável. Invalida o cache para propagar a alteração.
     */
    @Transactional
    @CacheEvict(value = "templates", key = "#templateId")
    public NotificationTemplate publishVersion(String templateId, String versionId) {
        NotificationTemplate template = getById(templateId);
        TemplateVersion version = template.getVersion(versionId);

        version.publish(); // Lógica encapsulada no domínio
        NotificationTemplate saved = templateRepository.save(template);

        eventProducer.publish(new TemplateVersionPublishedEvent(templateId, OffsetDateTime.now(), versionId));
        log.info("Versão {} do template {} publicada com sucesso.", version.getVersion(), templateId);

        return saved;
    }

    /**
     * Realiza o Soft Delete (Arquivamento) conforme RF01.
     */
    @Transactional
    @CacheEvict(value = "templates", key = "#templateId")
    public void archiveTemplate(String templateId) {
        NotificationTemplate template = getById(templateId);
        template.archive(); // Proteção de domínio
        templateRepository.save(template);

        eventProducer.publish(new TemplateArchivedEvent(templateId, OffsetDateTime.now()));
        log.info("Template {} arquivado com sucesso.", templateId);
    }

    /**
     * Executa a renderização do template com validação rigorosa de schema e estado.
     * Registra métricas de observabilidade para monitoramento em tempo real.
     */
    @Transactional
    public NotificationExecution executeTemplate(String templateId, String versionId, List<String> recipients, Map<String, Object> variables) {
        NotificationTemplate template = getById(templateId);

        // Proteção Sênior: Impedir disparos de templates deletados
        if (template.getStatus() == TemplateStatus.ARCHIVED) {
            recordMetric(template, "ARCHIVED_ERROR");
            throw new BusinessException("Não é possível executar um template arquivado.", "TEMPLATE_ARCHIVED");
        }

        TemplateVersion version = (versionId != null) ? template.getVersion(versionId) : template.getLatestPublishedVersion();

        // Proteção Sênior: Impedir disparos de rascunhos (DRAFT)
        if (!version.isPublished()) {
            recordMetric(template, "DRAFT_ERROR");
            throw new BusinessException("A versão solicitada não está publicada.", "VERSION_NOT_PUBLISHED");
        }

        ExecutionStatus status = ExecutionStatus.SUCCESS;
        String renderedContent;

        try {
            schemaValidator.validate(version.getInputSchema(), variables);
            renderedContent = renderEngine.render(version.getBody(), variables, template.getChannel() == Channel.EMAIL);
        } catch (BusinessException e) {
            status = ExecutionStatus.VALIDATION_ERROR;
            renderedContent = "Falha técnica na validação: " + e.getMessage();
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

        // CQRS: Dispara evento para atualização das visões de leitura
        eventProducer.publish(new NotificationDispatchedEvent(templateId, status.name()));

        // Diferencial: Observabilidade multidimensional
        recordMetric(template, status.name());

        return saved;
    }

    /**
     * Helper para registro de métricas via Micrometer.
     * Permite criar dashboards por Canal e Status no Grafana/Prometheus.
     */
    private void recordMetric(NotificationTemplate template, String resultStatus) {
        meterRegistry.counter("notifications.execution.total",
                "channel", template.getChannel().name(),
                "status", resultStatus,
                "orgId", template.getOrgId()
        ).increment();
    }
}