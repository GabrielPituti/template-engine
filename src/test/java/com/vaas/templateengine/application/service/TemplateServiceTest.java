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
 * Testes unitários para orquestração da lógica de negócio de templates.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Regras de Negócio do Template Service")
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

    /**
     * Valida o fluxo de criação de template com a versão rascunho inicial.
     */
    @Test
    @DisplayName("Deve criar um template com versão 1.0.0 em estado DRAFT")
    void shouldCreateTemplateWithInitialVersion() {
        when(templateRepository.save(any(NotificationTemplate.class))).thenAnswer(i -> i.getArgument(0));

        NotificationTemplate result = service.createTemplate(
                "Welcome", "Desc", Channel.EMAIL, "org-1", "wp-1"
        );

        assertNotNull(result);
        assertEquals(1, result.getVersions().size());
        assertEquals("1.0.0", result.getVersions().getFirst().getVersion().toString());
        verify(templateRepository, times(1)).save(any());
    }

    /**
     * Valida a busca automática da última versão publicada para execução.
     */
    @Test
    @DisplayName("Deve executar um template com sucesso procurando a última versão publicada")
    void shouldExecuteTemplateUsingLatestPublishedVersion() {
        String templateId = "t-123";
        TemplateVersion v1 = TemplateVersion.builder()
                .id("v1")
                .version(new SemanticVersion(1, 0, 0))
                .estado(VersionState.PUBLISHED)
                .body("Olá {{nome}}")
                .build();

        NotificationTemplate template = NotificationTemplate.builder()
                .id(templateId)
                .channel(Channel.EMAIL)
                .versions(new ArrayList<>(List.of(v1)))
                .build();

        Map<String, Object> vars = Map.of("nome", "Gabriel");

        when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));
        when(renderEngine.render(anyString(), anyMap(), anyBoolean())).thenReturn("Olá Gabriel");
        when(executionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        NotificationExecution result = service.executeTemplate(templateId, null, List.of("user@test.com"), vars);

        assertNotNull(result);
        assertEquals("Olá Gabriel", result.getRenderedContent());

        verify(schemaValidator).validate(any(), eq(vars));
        verify(renderEngine).render(eq("Olá {{nome}}"), eq(vars), eq(true));
        verify(executionRepository).save(any());
    }

    /**
     * Garante que o sistema impede a execução caso não haja versões estáveis.
     */
    @Test
    @DisplayName("Deve lançar erro ao tentar executar se não houver nenhuma versão publicada")
    void shouldThrowErrorWhenNoPublishedVersionFound() {
        String templateId = "t-123";
        NotificationTemplate template = NotificationTemplate.builder()
                .id(templateId)
                .versions(new ArrayList<>())
                .build();

        when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));

        assertThrows(BusinessException.class, () ->
                service.executeTemplate(templateId, null, List.of(), Map.of())
        );
    }
}