package com.vaas.templateengine.application.service;

import com.vaas.templateengine.domain.model.*;
import com.vaas.templateengine.domain.port.NotificationExecutionRepository;
import com.vaas.templateengine.domain.port.NotificationTemplateRepository;
import com.vaas.templateengine.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service: Implementa os casos de uso de gerenciamento e execução de templates.
 * Integra o SchemaValidator, RenderEngine e lógica de Versionamento Semântico.
 */
@Service
@RequiredArgsConstructor
public class TemplateService {

    private final NotificationTemplateRepository templateRepository;
    private final NotificationExecutionRepository executionRepository;
    private final SchemaValidator schemaValidator;
    private final RenderEngine renderEngine;

    @Transactional
    public NotificationTemplate createTemplate(String name, String description, Channel channel, String orgId, String workspaceId) {
        NotificationTemplate template = NotificationTemplate.builder()
                .name(name)
                .description(description)
                .channel(channel)
                .orgId(orgId)
                .workspaceId(workspaceId)
                .status(TemplateStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        TemplateVersion initialVersion = TemplateVersion.builder()
                .id(UUID.randomUUID().toString())
                .version(SemanticVersion.initial())
                .estado(VersionState.DRAFT)
                .createdAt(LocalDateTime.now())
                .build();

        template.addVersion(initialVersion);
        return templateRepository.save(template);
    }

    /**
     * Atualiza um template.
     * Se a última versão for DRAFT, sobrescreve.
     * Se for PUBLISHED, cria uma nova versão (Patch ou Minor) baseada no schema.
     */
    @Transactional
    public NotificationTemplate updateTemplate(String id, String subject, String body, List<InputVariable> schema, String changelog) {
        NotificationTemplate template = getById(id);

        TemplateVersion latest = template.getVersions().stream()
                .max((v1, v2) -> v1.getVersion().compareTo(v2.getVersion()))
                .orElseThrow(() -> new BusinessException("Versão não encontrada", "VERSION_NOT_FOUND"));

        if (latest.getEstado() == VersionState.DRAFT) {
            // Regra: Rascunhos são mutáveis
            latest.setSubject(subject);
            latest.setBody(body);
            latest.setInputSchema(schema);
            latest.setChangelog(changelog);
            latest.setCreatedAt(LocalDateTime.now());
        } else {
            // Regra: Versões Publicadas são IMUTÁVEIS. Criamos a próxima.
            SemanticVersion nextVersion = calculateNextVersion(latest, schema);

            TemplateVersion newVersion = TemplateVersion.builder()
                    .id(UUID.randomUUID().toString())
                    .version(nextVersion)
                    .subject(subject)
                    .body(body)
                    .inputSchema(schema)
                    .estado(VersionState.DRAFT)
                    .changelog(changelog)
                    .createdAt(LocalDateTime.now())
                    .build();

            template.addVersion(newVersion);
        }

        template.setUpdatedAt(LocalDateTime.now());
        return templateRepository.save(template);
    }

    private SemanticVersion calculateNextVersion(TemplateVersion latest, List<InputVariable> newSchema) {
        // Se o schema mudou (variáveis novas, tipos ou obrigatoriedade) -> Incremento Minor (X.Y.0)
        if (!latest.getInputSchema().equals(newSchema)) {
            return latest.getVersion().nextMinor();
        }
        // Se apenas o corpo/assunto mudou -> Incremento Patch (X.Y.Z)
        return latest.getVersion().nextPatch();
    }

    public NotificationTemplate getById(String id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Template não encontrado", "TEMPLATE_NOT_FOUND"));
    }

    @Transactional
    public NotificationExecution executeTemplate(String templateId, String versionId, List<String> recipients, Map<String, Object> variables) {
        NotificationTemplate template = getById(templateId);
        TemplateVersion version = findVersion(template, versionId);

        schemaValidator.validate(version.getInputSchema(), variables);
        String renderedBody = renderEngine.render(version.getBody(), variables);

        NotificationExecution execution = NotificationExecution.builder()
                .id(UUID.randomUUID().toString())
                .templateId(templateId)
                .versionId(version.getId())
                .recipients(recipients)
                .variables(variables)
                .renderedContent(renderedBody)
                .status(ExecutionStatus.SUCCESS)
                .executedOn(LocalDateTime.now())
                .build();

        return executionRepository.save(execution);
    }

    private TemplateVersion findVersion(NotificationTemplate template, String versionId) {
        if (versionId != null) {
            return template.getVersions().stream()
                    .filter(v -> v.getId().equals(versionId))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException("Versão não encontrada", "VERSION_NOT_FOUND"));
        }

        return template.getVersions().stream()
                .filter(TemplateVersion::isPublished)
                .max((v1, v2) -> v1.getVersion().compareTo(v2.getVersion()))
                .orElseThrow(() -> new BusinessException("Nenhuma versão publicada encontrada", "NO_PUBLISHED_VERSION"));
    }

    @Transactional
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
        template.setUpdatedAt(LocalDateTime.now());
        return templateRepository.save(template);
    }
}