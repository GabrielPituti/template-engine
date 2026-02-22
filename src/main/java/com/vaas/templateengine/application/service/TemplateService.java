package com.vaas.templateengine.application.service;

import com.vaas.templateengine.domain.event.*;
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
 * Esta camada coordena as interações entre o domínio e os componentes de infraestrutura,
 * garantindo a atomicidade das operações e a integridade do ciclo de vida das notificações.
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
    private final MeterRegistry meterRegistry;

    /**
     * Cria um novo agregado de template com sua versão inicial.
     * A emissão do evento de criação permite que o sistema de analytics ou outros
     * microsserviços reajam à disponibilidade de um novo template no catálogo.
     */
    @Transactional
    public NotificationTemplate createTemplate(String name, String description, Channel channel, String orgId, String workspaceId) {
        log.info("Iniciando criação do template: {} para a organização: {}", name, orgId);

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
                .body("Olá {{nome}}, seu cadastro foi realizado.")
                .inputSchema(List.of(new InputVariable("nome", VariableType.STRING, true)))
                .changelog("Criação inicial do template.")
                .createdAt(OffsetDateTime.now())
                .build();

        template.addVersion(initial);
        NotificationTemplate saved = templateRepository.save(template);

        eventProducer.publish(new TemplateCreatedEvent(saved.getId(), OffsetDateTime.now(), saved.getName()));

        return saved;
    }

    /**
     * Atualiza o conteúdo de uma versão em estado de rascunho.
     * O método delega a validação de imutabilidade para a entidade TemplateVersion,
     * impedindo que versões já publicadas sejam alteradas indevidamente.
     */
    @Transactional
    @CacheEvict(value = "templates", key = "#templateId")
    public NotificationTemplate updateVersion(String templateId, String versionId, String body, String subject, List<InputVariable> schema, String changelog) {
        NotificationTemplate template = getById(templateId);
        TemplateVersion version = template.getVersion(versionId);

        version.updateContent(body, subject, schema, changelog);
        template.setUpdatedAt(OffsetDateTime.now());

        return templateRepository.save(template);
    }

    /**
     * Localiza um template pelo identificador único.
     * Implementa cache local via Caffeine para reduzir a latência de I/O em disparos de alta volumetria.
     */
    @Cacheable(value = "templates", key = "#id")
    public NotificationTemplate getById(String id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Template não encontrado: " + id, "TEMPLATE_NOT_FOUND"));
    }

    /**
     * Promove uma versão para o estado publicado.
     * A invalidação do cache é necessária para garantir que instâncias de execução
     * passem a utilizar o novo conteúdo imediatamente após a publicação.
     */
    @Transactional
    @CacheEvict(value = "templates", key = "#templateId")
    public NotificationTemplate publishVersion(String templateId, String versionId) {
        NotificationTemplate template = getById(templateId);
        TemplateVersion version = template.getVersion(versionId);

        version.publish();
        NotificationTemplate saved = templateRepository.save(template);

        eventProducer.publish(new TemplateVersionPublishedEvent(templateId, OffsetDateTime.now(), versionId));
        log.info("Versão do template {} publicada com sucesso.", templateId);

        return saved;
    }

    /**
     * Executa o arquivamento lógico do template.
     * Esta decisão de design preserva a rastreabilidade histórica dos envios
     * vinculados ao template, atendendo requisitos de auditoria.
     */
    @Transactional
    @CacheEvict(value = "templates", key = "#templateId")
    public void archiveTemplate(String templateId) {
        NotificationTemplate template = getById(templateId);
        template.archive();
        templateRepository.save(template);

        eventProducer.publish(new TemplateArchivedEvent(templateId, OffsetDateTime.now()));
        log.info("Template {} arquivado com sucesso.", templateId);
    }

    /**
     * Orquestra a execução da renderização e validação de schema.
     * Registra métricas multidimensionais para permitir monitoramento granular
     * por organização e canal de comunicação via Spring Actuator.
     */
    @Transactional
    public NotificationExecution executeTemplate(String templateId, String versionId, List<String> recipients, Map<String, Object> variables) {
        NotificationTemplate template = getById(templateId);

        if (template.getStatus() == TemplateStatus.ARCHIVED) {
            recordMetric(template, "ARCHIVED_ERROR");
            throw new BusinessException("Não é possível executar um template arquivado.", "TEMPLATE_ARCHIVED");
        }

        TemplateVersion version = (versionId != null) ? template.getVersion(versionId) : template.getLatestPublishedVersion();

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

        eventProducer.publish(new NotificationDispatchedEvent(templateId, status.name()));
        recordMetric(template, status.name());

        return saved;
    }

    private void recordMetric(NotificationTemplate template, String resultStatus) {
        meterRegistry.counter("notifications.execution.total",
                "channel", template.getChannel().name(),
                "status", resultStatus,
                "orgId", template.getOrgId()
        ).increment();
    }
}