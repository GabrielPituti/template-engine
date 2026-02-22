package com.vaas.templateengine.application.service;

import com.vaas.templateengine.domain.model.*;
import com.vaas.templateengine.domain.port.NotificationExecutionRepository;
import com.vaas.templateengine.domain.port.NotificationTemplateRepository;
import com.vaas.templateengine.infrastructure.messaging.NotificationProducer;
import com.vaas.templateengine.infrastructure.persistence.TemplateStatsRepository;
import com.vaas.templateengine.shared.exception.BusinessException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
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
 * Suíte de testes unitários para validação da camada de orquestração.
 * Garante a correta aplicação das regras de versionamento, imutabilidade
 * e o isolamento de métricas operacionais por inquilino.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Regras de Negócio: Template Service")
class TemplateServiceTest {

    @Mock
    private NotificationTemplateRepository templateRepository;
    @Mock
    private NotificationExecutionRepository executionRepository;
    @Mock
    private TemplateStatsRepository statsRepository;
    @Mock
    private SchemaValidator schemaValidator;
    @Mock
    private RenderEngine renderEngine;
    @Mock
    private NotificationProducer eventProducer;
    @Mock
    private MeterRegistry meterRegistry;
    @Mock
    private Counter counter;

    @InjectMocks
    private TemplateService templateService;

    @BeforeEach
    void setUp() {
        lenient().when(meterRegistry.counter(anyString(), any(String[].class))).thenReturn(counter);
    }

    @Test
    @DisplayName("Deve executar um template utilizando a última versão publicada por padrão")
    void shouldExecuteTemplateWithLatestPublishedVersion() {
        TemplateVersion publishedVersion = TemplateVersion.builder()
                .id("v1").version(new SemanticVersion(1, 0, 0))
                .estado(VersionState.PUBLISHED).body("Olá {{nome}}").build();

        NotificationTemplate template = NotificationTemplate.builder()
                .id("t1").status(TemplateStatus.ACTIVE).channel(Channel.EMAIL)
                .orgId("org-test").versions(List.of(publishedVersion)).build();

        when(templateRepository.findById("t1")).thenReturn(Optional.of(template));
        when(renderEngine.render(anyString(), anyMap(), anyBoolean())).thenReturn("Olá Gabriel");
        when(executionRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        NotificationExecution result = templateService.executeTemplate("t1", null, List.of("test@test.com"), Map.of("nome", "Gabriel"));

        assertNotNull(result);
        assertEquals("Olá Gabriel", result.getRenderedContent());
        verify(eventProducer, times(1)).publish(any());
        verify(meterRegistry, atLeastOnce()).counter(eq("notifications.execution.total"), any(String[].class));
    }

    @Test
    @DisplayName("Deve impedir o despacho caso a versão solicitada ainda resida em estado DRAFT")
    void shouldThrowErrorWhenExecutingDraftVersion() {
        TemplateVersion draftVersion = TemplateVersion.builder()
                .id("v1").estado(VersionState.DRAFT).build();

        NotificationTemplate template = NotificationTemplate.builder()
                .id("t1").status(TemplateStatus.ACTIVE).channel(Channel.EMAIL)
                .orgId("org-test").versions(List.of(draftVersion)).build();

        when(templateRepository.findById("t1")).thenReturn(Optional.of(template));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                templateService.executeTemplate("t1", "v1", List.of("test@test.com"), Map.of())
        );

        assertEquals("VERSION_NOT_PUBLISHED", ex.getCode());
        verify(meterRegistry, atLeastOnce()).counter(eq("notifications.execution.total"), any(String[].class));
    }

    @Test
    @DisplayName("Deve emitir evento de arquivamento ao desativar um template")
    void shouldEmitEventWhenArchivingTemplate() {
        NotificationTemplate template = NotificationTemplate.builder()
                .id("t1").status(TemplateStatus.ACTIVE).build();

        when(templateRepository.findById("t1")).thenReturn(Optional.of(template));

        templateService.archiveTemplate("t1");

        assertEquals(TemplateStatus.ARCHIVED, template.getStatus());
        verify(eventProducer).publish(any(com.vaas.templateengine.domain.event.TemplateArchivedEvent.class));
    }
}