package com.vaas.templateengine.application.service;

import com.vaas.templateengine.domain.model.*;
import com.vaas.templateengine.domain.port.NotificationTemplateRepository;
import com.vaas.templateengine.shared.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Teste Unitário: Valida a lógica de negócio do TemplateService.
 * Implementa validações clínicas para garantir imutabilidade e consistência de estados.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Regras de Negócio do Template Service")
class TemplateServiceTest {

    @Mock
    private NotificationTemplateRepository repository;

    @InjectMocks
    private TemplateService service;

    @Test
    @DisplayName("Deve criar um template com versão 1.0.0 em estado DRAFT")
    void shouldCreateTemplateWithInitialVersion() {
        // Given
        when(repository.save(any(NotificationTemplate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        NotificationTemplate result = service.createTemplate(
                "Welcome", "Desc", Channel.EMAIL, "org-1", "wp-1"
        );

        // Then
        assertNotNull(result);
        assertEquals(1, result.getVersions().size());
        assertEquals("1.0.0", result.getVersions().getFirst().getVersion().toString());
        assertEquals(VersionState.DRAFT, result.getVersions().getFirst().getEstado());
        verify(repository, times(1)).save(any());
    }

    @Test
    @DisplayName("Deve publicar uma versão com sucesso")
    void shouldPublishVersionSuccessfully() {
        // Given
        String templateId = "template-1";
        String versionId = "v1";

        TemplateVersion draftVersion = TemplateVersion.builder()
                .id(versionId)
                .estado(VersionState.DRAFT)
                .build();

        NotificationTemplate template = NotificationTemplate.builder()
                .id(templateId)
                .versions(new ArrayList<>(java.util.List.of(draftVersion)))
                .build();

        when(repository.findById(templateId)).thenReturn(Optional.of(template));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        NotificationTemplate result = service.publishVersion(templateId, versionId);

        // Then
        assertEquals(VersionState.PUBLISHED, result.getVersions().getFirst().getEstado());
        verify(repository, times(1)).save(template);
    }

    @Test
    @DisplayName("Deve lançar erro ao tentar publicar uma versão que já está publicada (Imutabilidade)")
    void shouldThrowErrorWhenPublishingAlreadyPublishedVersion() {
        // Given
        String templateId = "template-123";
        String versionId = "v1";

        TemplateVersion publishedVersion = TemplateVersion.builder()
                .id(versionId)
                .estado(VersionState.PUBLISHED)
                .build();

        NotificationTemplate template = NotificationTemplate.builder()
                .id(templateId)
                .versions(new ArrayList<>(java.util.List.of(publishedVersion)))
                .build();

        when(repository.findById(templateId)).thenReturn(Optional.of(template));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () ->
                service.publishVersion(templateId, versionId)
        );

        assertEquals("VERSION_ALREADY_PUBLISHED", exception.getCode());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar erro quando a versão não existe no template")
    void shouldThrowErrorWhenVersionDoesNotExist() {
        // Given
        String templateId = "template-1";
        NotificationTemplate template = NotificationTemplate.builder()
                .id(templateId)
                .versions(new ArrayList<>())
                .build();

        when(repository.findById(templateId)).thenReturn(Optional.of(template));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () ->
                service.publishVersion(templateId, "non-existent")
        );

        assertEquals("VERSION_NOT_FOUND", exception.getCode());
    }

    @Test
    @DisplayName("Deve lançar erro quando o template não existe")
    void shouldThrowErrorWhenTemplateNotFound() {
        // Given
        when(repository.findById("invalid")).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () ->
                service.getById("invalid")
        );
        assertEquals("TEMPLATE_NOT_FOUND", exception.getCode());
    }
}