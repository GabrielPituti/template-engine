package com.vaas.templateengine.application.service;

import com.vaas.templateengine.domain.model.*;
import com.vaas.templateengine.domain.port.NotificationExecutionRepository;
import com.vaas.templateengine.domain.port.NotificationTemplateRepository;
import com.vaas.templateengine.shared.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Teste Unitário: Valida a lógica de negócio do TemplateService de forma integrada.
 * Cobre criação, execução, publicação e versionamento semântico automático.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Regras de Negócio do Template Service - Integrado")
class TemplateServiceTest {

    @Mock
    private NotificationTemplateRepository templateRepository;

    @Mock
    private NotificationExecutionRepository executionRepository;

    @Mock
    private SchemaValidator schemaValidator;

    @Mock
    private RenderEngine renderEngine;

    @InjectMocks
    private TemplateService service;

    @Test
    @DisplayName("Deve criar um template com versão 1.0.0 em estado DRAFT")
    void shouldCreateTemplateWithInitialVersion() {
        // Given
        when(templateRepository.save(any(NotificationTemplate.class))).thenAnswer(i -> i.getArgument(0));

        // When
        NotificationTemplate result = service.createTemplate(
                "Welcome", "Desc", Channel.EMAIL, "org-1", "wp-1"
        );

        // Then
        assertNotNull(result);
        assertEquals(1, result.getVersions().size());
        assertEquals("1.0.0", result.getVersions().getFirst().getVersion().toString());
        assertEquals(VersionState.DRAFT, result.getVersions().getFirst().getEstado());
        verify(templateRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Deve atualizar o rascunho existente se a última versão ainda for DRAFT")
    void shouldUpdateDraftVersionWithoutCreatingNewOne() {
        // Given
        String templateId = "t-1";
        TemplateVersion v1 = TemplateVersion.builder()
                .id("v1")
                .version(new SemanticVersion(1, 0, 0))
                .estado(VersionState.DRAFT)
                .body("Corpo Antigo")
                .inputSchema(new ArrayList<>())
                .build();

        NotificationTemplate template = NotificationTemplate.builder()
                .id(templateId)
                .versions(new ArrayList<>(List.of(v1)))
                .build();

        when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));
        when(templateRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        NotificationTemplate result = service.updateTemplate(templateId, "Assunto", "Novo Corpo", new ArrayList<>(), "Update draft");

        // Then
        assertEquals(1, result.getVersions().size());
        assertEquals("Novo Corpo", result.getVersions().getFirst().getBody());
        assertEquals("1.0.0", result.getVersions().getFirst().getVersion().toString());
    }

    @Test
    @DisplayName("Deve criar nova versão PATCH (1.0.1) quando apenas o corpo muda e a última versão é PUBLISHED")
    void shouldCreateNewPatchVersionWhenBodyChanges() {
        // Given
        String templateId = "t-1";
        List<InputVariable> schema = List.of(new InputVariable("var", VariableType.STRING, true));
        TemplateVersion v1 = TemplateVersion.builder()
                .id("v1")
                .version(new SemanticVersion(1, 0, 0))
                .estado(VersionState.PUBLISHED)
                .body("Corpo Antigo")
                .inputSchema(schema)
                .build();

        NotificationTemplate template = NotificationTemplate.builder()
                .id(templateId)
                .versions(new ArrayList<>(List.of(v1)))
                .build();

        when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));
        when(templateRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        NotificationTemplate result = service.updateTemplate(templateId, "Assunto", "Corpo Novo", schema, "Novo patch");

        // Then
        assertEquals(2, result.getVersions().size());
        TemplateVersion latest = result.getVersions().stream()
                .max((vA, vB) -> vA.getVersion().compareTo(vB.getVersion())).get();

        assertEquals("1.0.1", latest.getVersion().toString());
        assertEquals(VersionState.DRAFT, latest.getEstado());
    }

    @Test
    @DisplayName("Deve criar nova versão MINOR (1.1.0) quando o schema de variáveis muda")
    void shouldCreateNewMinorVersionWhenSchemaChanges() {
        // Given
        String templateId = "t-1";
        TemplateVersion v1 = TemplateVersion.builder()
                .id("v1")
                .version(new SemanticVersion(1, 0, 0))
                .estado(VersionState.PUBLISHED)
                .inputSchema(new ArrayList<>())
                .build();

        NotificationTemplate template = NotificationTemplate.builder()
                .id(templateId)
                .versions(new ArrayList<>(List.of(v1)))
                .build();

        List<InputVariable> newSchema = List.of(new InputVariable("nome", VariableType.STRING, true));

        when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));
        when(templateRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        NotificationTemplate result = service.updateTemplate(templateId, "Assunto", "Corpo", newSchema, "Novo schema");

        // Then
        TemplateVersion latest = result.getVersions().stream()
                .max((vA, vB) -> vA.getVersion().compareTo(vB.getVersion())).get();

        assertEquals("1.1.0", latest.getVersion().toString());
    }

    @Test
    @DisplayName("Deve executar um template com sucesso procurando a última versão publicada")
    void shouldExecuteTemplateUsingLatestPublishedVersion() {
        // Given
        String templateId = "t-123";
        TemplateVersion v1 = TemplateVersion.builder()
                .id("v1")
                .version(new SemanticVersion(1, 0, 0))
                .estado(VersionState.PUBLISHED)
                .body("Olá {{nome}}")
                .build();

        NotificationTemplate template = NotificationTemplate.builder()
                .id(templateId)
                .versions(new ArrayList<>(List.of(v1)))
                .build();

        Map<String, Object> vars = Map.of("nome", "Gabriel");

        when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));
        when(renderEngine.render(anyString(), anyMap())).thenReturn("Olá Gabriel");
        when(executionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        NotificationExecution result = service.executeTemplate(templateId, null, List.of("user@test.com"), vars);

        // Then
        assertNotNull(result);
        assertEquals("Olá Gabriel", result.getRenderedContent());
        assertEquals(ExecutionStatus.SUCCESS, result.getStatus());

        verify(schemaValidator).validate(any(), eq(vars));
        verify(renderEngine).render(eq("Olá {{nome}}"), eq(vars));
        verify(executionRepository).save(any());
    }

    @Test
    @DisplayName("Deve lançar erro ao tentar executar se não houver nenhuma versão publicada")
    void shouldThrowErrorWhenNoPublishedVersionFound() {
        // Given
        String templateId = "t-123";
        NotificationTemplate template = NotificationTemplate.builder()
                .id(templateId)
                .versions(new ArrayList<>())
                .build();

        when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));

        // When & Then
        BusinessException ex = assertThrows(BusinessException.class, () ->
                service.executeTemplate(templateId, null, List.of(), Map.of())
        );

        assertEquals("NO_PUBLISHED_VERSION", ex.getCode());
    }

    @Test
    @DisplayName("Deve lançar erro ao tentar publicar uma versão que já está publicada")
    void shouldThrowErrorWhenPublishingAlreadyPublishedVersion() {
        // Given
        String templateId = "t-1";
        String versionId = "v1";
        TemplateVersion published = TemplateVersion.builder().id(versionId).estado(VersionState.PUBLISHED).build();
        NotificationTemplate template = NotificationTemplate.builder().id(templateId).versions(new ArrayList<>(List.of(published))).build();

        when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));

        // When & Then
        BusinessException ex = assertThrows(BusinessException.class, () ->
                service.publishVersion(templateId, versionId)
        );

        assertEquals("VERSION_ALREADY_PUBLISHED", ex.getCode());
    }
}