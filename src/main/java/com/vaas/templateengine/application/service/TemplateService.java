package com.vaas.templateengine.application.service;

import com.vaas.templateengine.domain.model.*;
import com.vaas.templateengine.domain.port.NotificationTemplateRepository;
import com.vaas.templateengine.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service: Implementa os casos de uso de gerenciamento de templates.
 * Responsável por garantir as regras de imutabilidade de versões publicadas.
 */
@Service
@RequiredArgsConstructor
public class TemplateService {

    private final NotificationTemplateRepository repository;

    /**
     * Cria um novo template com uma versão inicial em DRAFT.
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
        return repository.save(template);
    }

    /**
     * Busca um template por ID ou lança exceção de negócio.
     */
    public NotificationTemplate getById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("Template não encontrado", "TEMPLATE_NOT_FOUND"));
    }

    /**
     * Publica uma versão específica.
     * Conforme RF01: Versões publicadas tornam-se imutáveis.
     */
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

        return repository.save(template);
    }
}