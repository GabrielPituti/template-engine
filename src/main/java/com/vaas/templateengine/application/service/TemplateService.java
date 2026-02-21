package com.vaas.templateengine.application.service;

import com.vaas.templateengine.domain.model.*;
import com.vaas.templateengine.domain.port.NotificationExecutionRepository;
import com.vaas.templateengine.domain.port.NotificationTemplateRepository;
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
 * Implementa regras de versionamento, imutabilidade, auditoria e cache de performance.
 */
@Service
@RequiredArgsConstructor
public class TemplateService {

    private final NotificationTemplateRepository templateRepository;
    private final NotificationExecutionRepository executionRepository;
    private final SchemaValidator schemaValidator;
    private final RenderEngine renderEngine;

    /**
     * Cria um novo template com sua versão inicial em estado de rascunho (1.0.0).
     */
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
                .body("Bem-vindo {{nome}}") // Exemplo inicial
                .inputSchema(List.of(new InputVariable("nome", VariableType.STRING, true)))
                .createdAt(OffsetDateTime.now())
                .build();

        template.addVersion(initialVersion);
        return templateRepository.save(template);
    }

    /**
     * Recupera um template por identificador único.
     * Utiliza cache para otimizar leituras em execuções de alta volumetria.
     */
    @Cacheable(value = "templates", key = "#id")
    public NotificationTemplate getById(String id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Template não encontrado", "TEMPLATE_NOT_FOUND"));
    }

    /**
     * Publica uma versão específica, tornando-a imutável e pronta para execução.
     */
    @Transactional
    @CacheEvict(value = "templates", key = "#templateId")
    public NotificationTemplate publishVersion(String templateId, String versionId) {
        NotificationTemplate template = getById(templateId);

        TemplateVersion version = template.getVersions().stream()
                .filter(v -> v.getId().equals(versionId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Versão não encontrada", "VERSION_NOT_FOUND"));

        if (version.isPublished()) {
            throw new BusinessException("Esta versão já está publicada", "VERSION_ALREADY_PUBLISHED");
        }

        version.setEstado(VersionState.PUBLISHED);
        template.setUpdatedAt(OffsetDateTime.now());
        return templateRepository.save(template);
    }

    /**
     * Arquiva um template (Soft Delete) conforme exigido no RF01.
     */
    @Transactional
    @CacheEvict(value = "templates", key = "#templateId")
    public void archiveTemplate(String templateId) {
        templateRepository.deleteById(templateId);
    }

    /**
     * Executa a renderização do template e registra o log de auditoria.
     */
    @Transactional
    public NotificationExecution executeTemplate(String templateId, String versionId, List<String> recipients, Map<String, Object> variables) {
        NotificationTemplate template = getById(templateId);

        if (template.getStatus() == TemplateStatus.ARCHIVED) {
            throw new BusinessException("Não é possível executar um template arquivado", "TEMPLATE_ARCHIVED");
        }

        TemplateVersion version = findVersionForExecution(template, versionId);

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

        return executionRepository.save(execution);
    }

    private TemplateVersion findVersionForExecution(NotificationTemplate template, String versionId) {
        if (versionId != null) {
            return template.getVersions().stream()
                    .filter(v -> v.getId().equals(versionId))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException("Versão específica não encontrada", "VERSION_NOT_FOUND"));
        }

        return template.getVersions().stream()
                .filter(TemplateVersion::isPublished)
                .max(TemplateVersion::compareTo)
                .orElseThrow(() -> new BusinessException("Nenhuma versão publicada disponível", "NO_PUBLISHED_VERSION"));
    }
}