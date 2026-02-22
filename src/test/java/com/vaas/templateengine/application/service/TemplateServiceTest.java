package com.vaas.templateengine.application.service;

import com.vaas.templateengine.domain.model.*;
import com.vaas.templateengine.domain.port.NotificationExecutionRepository;
import com.vaas.templateengine.domain.port.NotificationTemplateRepository;
import com.vaas.templateengine.infrastructure.messaging.NotificationProducer;
import com.vaas.templateengine.shared.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para o TemplateService.
 * Valida as regras de negócio, versionamento e segurança da engine.
 * Implementa mocks para garantir o isolamento do domínio.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Regras de Negócio: Template Service")
class TemplateServiceTest {

    @Mock
    private NotificationTemplateRepository templateRepository;

    @Mock
    private NotificationExecutionRepository executionRepository;

    @Mock
    private SchemaValidator schemaValidator;

    @Mock
    private RenderEngine renderEngine;

    /** * Mock para o produtor de eventos Kafka.
     * Necessário para validar o disparo de eventos de domínio após operações de sucesso.
     */
    @Mock
    private NotificationProducer eventProducer;

    @InjectMocks
    private TemplateService templateService;

    @Test
    @DisplayName("Deve executar um template com sucesso procurando a última versão publicada")
    void shouldExecuteTemplateWithLatestPublishedVersion() {
        // Cenário: Template ativo com uma versão já publicada
        TemplateVersion publishedVersion = TemplateVersion.builder()
                .id("v1")
                .version(new SemanticVersion(1, 0, 0))
                .estado(VersionState.PUBLISHED)
                .body("Hello {{name}}")
                .build();

        NotificationTemplate template = NotificationTemplate.builder()
                .id("t1")
                .status(TemplateStatus.ACTIVE)
                .channel(Channel.EMAIL)
                .versions(List.of(publishedVersion))
                .build();

        when(templateRepository.findById("t1")).thenReturn(Optional.of(template));
        when(renderEngine.render(anyString(), anyMap(), anyBoolean())).thenReturn("Hello Gabriel");
        when(executionRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // Ação: Execução sem informar versão específica (deve buscar a última publicada)
        NotificationExecution result = templateService.executeTemplate("t1", null, List.of("test@test.com"), Map.of("name", "Gabriel"));

        // Validação: Verifica se o motor processou corretamente e se o evento foi emitido
        assertNotNull(result);
        assertEquals("Hello Gabriel", result.getRenderedContent());
        assertEquals(ExecutionStatus.SUCCESS, result.getStatus());

        verify(eventProducer, times(1)).publish(any());
    }

    @Test
    @DisplayName("Deve lançar erro ao tentar executar versão que ainda é DRAFT")
    void shouldThrowErrorWhenExecutingDraftVersion() {
        // Cenário: Template com versão em rascunho
        TemplateVersion draftVersion = TemplateVersion.builder()
                .id("v1")
                .version(new SemanticVersion(1, 0, 0))
                .estado(VersionState.DRAFT)
                .build();

        NotificationTemplate template = NotificationTemplate.builder()
                .id("t1")
                .status(TemplateStatus.ACTIVE)
                .versions(List.of(draftVersion))
                .build();

        when(templateRepository.findById("t1")).thenReturn(Optional.of(template));

        // Ação & Validação: Bloqueio de segurança conforme RF02 e auditoria sênior
        BusinessException ex = assertThrows(BusinessException.class, () ->
                templateService.executeTemplate("t1", "v1", List.of("test@test.com"), Map.of())
        );

        assertEquals("VERSION_NOT_PUBLISHED", ex.getCode());
    }
}