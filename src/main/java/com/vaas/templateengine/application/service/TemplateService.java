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
 * Orquestrador central da lógica de negócio de templates de notificação.
 * Gerencia o ciclo de vida dos agregados, garantindo a aplicação de regras de
 * versionamento, imutabilidade, persistência e observabilidade.
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
     * Cria um novo template com sua versão inicial em estado de rascunho.
     * * @param name Nome do template.
     * @param description Descrição opcional das finalidades do template.
     * @param channel Canal de comunicação (EMAIL, SMS, WEBHOOK).
     * @param orgId Identificador da organização proprietária.
     * @param workspaceId Identificador do workspace.
     * @return O agregado NotificationTemplate persistido.
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
                .changelog("Setup inicial do template.")
                .createdAt(OffsetDateTime.now())
                .build();

        template.addVersion(initial);
        NotificationTemplate saved = templateRepository.save(template);

        eventProducer.publish(new TemplateCreatedEvent(saved.getId(), OffsetDateTime.now(), saved.getName()));

        return saved;
    }

    /**
     * Recupera um template por identificador único com suporte a cache.
     * * @param id Identificador único do template.
     * @return O agregado NotificationTemplate encontrado.
     */
    @Cacheable(value = "templates", key = "#id")
    public NotificationTemplate getById(String id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Template não encontrado: " + id, "TEMPLATE_NOT_FOUND"));
    }

    /**
     * Publica uma versão específica do template, tornando-a imutável.
     * * @param templateId Identificador do template.
     * @param versionId Identificador da versão a ser publicada.
     * @return O agregado atualizado e persistido.
     */
    @Transactional
    @CacheEvict(value = "templates", key = "#templateId")
    public NotificationTemplate publishVersion(String templateId, String versionId) {
        NotificationTemplate template = getById(templateId);
        TemplateVersion version = template.getVersion(versionId);

        version.publish();
        NotificationTemplate saved = templateRepository.save(template);

        eventProducer.publish(new TemplateVersionPublishedEvent(templateId, OffsetDateTime.now(), versionId));
        log.info("Versão {} do template {} publicada com sucesso.", version.getVersion(), templateId);

        return saved;
    }

    /**
     * Realiza o arquivamento lógico (Soft Delete) de um template.
     * * @param templateId Identificador do template a ser arquivado.
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
     * Executa a renderização de um template para um conjunto de variáveis.
     * * @param templateId Identificador do template.
     * @param versionId Identificador opcional da versão.
     * @param recipients Lista de destinatários.
     * @param variables Mapa de variáveis para renderização.
     * @return Registro da execução com o conteúdo renderizado e status.
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

    /**
     * Registra métricas multidimensionais de execução para observabilidade.
     * * @param template O agregado do template executado.
     * @param resultStatus O status final do processamento.
     */
    private void recordMetric(NotificationTemplate template, String resultStatus) {
        meterRegistry.counter("notifications.execution.total",
                "channel", template.getChannel().name(),
                "status", resultStatus,
                "orgId", template.getOrgId()
        ).increment();
    }
}