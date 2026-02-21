package com.vaas.templateengine.infrastructure.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaas.templateengine.application.dto.TemplateMapper;
import com.vaas.templateengine.application.service.TemplateService;
import com.vaas.templateengine.domain.model.Channel;
import com.vaas.templateengine.domain.model.NotificationTemplate;
import com.vaas.templateengine.domain.model.TemplateStatus;
import com.vaas.templateengine.domain.port.NotificationTemplateRepository;
import com.vaas.templateengine.infrastructure.messaging.NotificationProducer;
import com.vaas.templateengine.infrastructure.persistence.TemplateStatsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * Testes de contrato da API REST para o TemplateController.
 * Utiliza MockitoBean (substituto do MockBean no Spring Boot 3.5+) para isolar a camada web.
 */
@WebMvcTest(TemplateController.class)
@DisplayName("API: Template Controller")
class TemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TemplateService templateService;

    @MockitoBean
    private TemplateMapper mapper;

    @MockitoBean
    private NotificationProducer eventProducer;

    @MockitoBean
    private TemplateStatsRepository statsRepository;

    @MockitoBean
    private NotificationTemplateRepository templateRepository;

    @Test
    @DisplayName("Deve criar um template e retornar 201 Created")
    void shouldCreateTemplate() throws Exception {
        // Cenário
        TemplateMapper.CreateTemplateRequest request = new TemplateMapper.CreateTemplateRequest(
                "Welcome", "Desc", Channel.EMAIL, "org-1", "wp-1"
        );

        NotificationTemplate template = NotificationTemplate.builder()
                .id("uuid-123")
                .name("Welcome")
                .status(TemplateStatus.ACTIVE)
                .build();

        TemplateMapper.TemplateResponse response = new TemplateMapper.TemplateResponse(
                "uuid-123", "Welcome", "Desc", Channel.EMAIL, "ACTIVE", OffsetDateTime.now(), null
        );

        // Mocking
        when(templateService.createTemplate(anyString(), anyString(), any(), anyString(), anyString()))
                .thenReturn(template);
        when(mapper.toResponse(any())).thenReturn(response);

        // Execução e Validação
        mockMvc.perform(post("/v1/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("uuid-123"))
                .andExpect(jsonPath("$.name").value("Welcome"));
    }
}