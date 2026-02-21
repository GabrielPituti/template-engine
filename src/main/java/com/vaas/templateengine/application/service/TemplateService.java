package com.vaas.templateengine.application.service;

import com.vaas.templateengine.domain.model.*;
import com.vaas.templateengine.domain.port.NotificationExecutionRepository;
import com.vaas.templateengine.domain.port.NotificationTemplateRepository;
import com.vaas.templateengine.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Serviço de Aplicação que orquestra as regras de negócio de templates.
 * Implementa as lógicas de imutabilidade, versionamento semântico e auditoria.
 */
@Service
@RequiredArgsConstructor
public class TemplateService {

    private final NotificationTemplateRepository templateRepository;
    private final NotificationExecutionRepository executionRepository;
    private final SchemaValidator schemaValidator;
    private final RenderEngine renderEngine;

    /**
     * Cria um novo template com uma versão inicial em rascunho (1.0.0).
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
                .createdAt(OffsetDateTime.now())
                .build();

        template.addVersion(initialVersion);
        return templateRepository.save(template);
    }

    /**
     * Atualiza o conteúdo de um template.
     * Se a última versão estiver em DRAFT, ela é modificada.
     * Se estiver PUBLISHED, uma nova versão semântica é criada automaticamente.
     */
    @Transactional
    public NotificationTemplate updateTemplate(String id, String subject, String body, List<InputVariable> schema, String changelog) {
        NotificationTemplate template = getById(id);

        TemplateVersion latest = template.getVersions().stream()
                .max(TemplateVersion::compareTo)
                .orElseThrow(() -> new BusinessException("Versão não encontrada", "VERSION_NOT_FOUND"));

        if (latest.getEstado() == VersionState.DRAFT) {
            atualizarRascunho(latest, subject, body, schema, changelog);
        } else {
            criarNovaVersao(template, latest, subject, body, schema, changelog);
        }

        template.setUpdatedAt(OffsetDateTime.now());
        return templateRepository.save(template);
    }

    /**
     * Executa a renderização de um template para um conjunto de destinatários.
     * Realiza validação de schema, substituição de variáveis e gera log de auditoria.
     */
    @Transactional
    public NotificationExecution executeTemplate(String templateId, String versionId, List<String> recipients, Map<String, Object> variables) {
        NotificationTemplate template = getById(templateId);
        TemplateVersion version = localizarVersaoParaExecucao(template, versionId);

        // Validação clínica de tipos e obrigatoriedade
        schemaValidator.validate(version.getInputSchema(), variables);

        // Renderização com proteção contra XSS se for canal de E-mail
        boolean deveEscaparHtml = template.getChannel() == Channel.EMAIL;
        String content = renderEngine.render(version.getBody(), variables, deveEscaparHtml);

        NotificationExecution execution = NotificationExecution.builder()
                .id(UUID.randomUUID().toString())
                .templateId(templateId)
                .versionId(version.getId())
                .recipients(recipients)
                .variables(variables)
                .renderedContent(content)
                .status(ExecutionStatus.SUCCESS)
                .executedOn(OffsetDateTime.now())
                .build();

        return executionRepository.save(execution);
    }

    public NotificationTemplate getById(String id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Template não encontrado", "TEMPLATE_NOT_FOUND"));
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
        template.setUpdatedAt(OffsetDateTime.now());
        return templateRepository.save(template);
    }

    private void atualizarRascunho(TemplateVersion draft, String subject, String body, List<InputVariable> schema, String changelog) {
        draft.setSubject(subject);
        draft.setBody(body);
        draft.setInputSchema(schema);
        draft.setChangelog(changelog);
        draft.setCreatedAt(OffsetDateTime.now());
    }

    private void criarNovaVersao(NotificationTemplate template, TemplateVersion latest, String subject, String body, List<InputVariable> schema, String changelog) {
        // Incremento Minor se o schema mudar (quebra de contrato), caso contrário Patch
        SemanticVersion nextVersion = !latest.getInputSchema().equals(schema)
                ? latest.getVersion().nextMinor()
                : latest.getVersion().nextPatch();

        template.addVersion(TemplateVersion.builder()
                .id(UUID.randomUUID().toString())
                .version(nextVersion)
                .subject(subject)
                .body(body)
                .inputSchema(schema)
                .estado(VersionState.DRAFT)
                .changelog(changelog)
                .createdAt(OffsetDateTime.now())
                .build());
    }

    private TemplateVersion localizarVersaoParaExecucao(NotificationTemplate template, String versionId) {
        if (versionId != null) {
            return template.getVersions().stream()
                    .filter(v -> v.getId().equals(versionId))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException("Versão não encontrada", "VERSION_NOT_FOUND"));
        }

        return template.getVersions().stream()
                .filter(TemplateVersion::isPublished)
                .max(TemplateVersion::compareTo)
                .orElseThrow(() -> new BusinessException("Nenhuma versão publicada disponível", "NO_PUBLISHED_VERSION"));
    }
}