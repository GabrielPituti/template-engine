package com.vaas.templateengine.infrastructure.web.controller;

import com.vaas.templateengine.application.dto.TemplateMapper;
import com.vaas.templateengine.application.dto.TemplateMapper.*;
import com.vaas.templateengine.application.service.TemplateService;
import com.vaas.templateengine.domain.event.NotificationDispatchedEvent;
import com.vaas.templateengine.domain.event.TemplateCreatedEvent;
import com.vaas.templateengine.domain.model.NotificationExecution;
import com.vaas.templateengine.domain.model.NotificationTemplate;
import com.vaas.templateengine.domain.model.TemplateStatsView;
import com.vaas.templateengine.infrastructure.messaging.NotificationProducer;
import com.vaas.templateengine.infrastructure.persistence.TemplateStatsRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

/**
 * Controller REST responsável pela exposição dos recursos de templates e execuções.
 * Implementa o contrato definido na especificação OpenAPI 3.1.
 */
@RestController
@RequestMapping("/v1/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;
    private final TemplateMapper mapper;
    private final NotificationProducer eventProducer;
    private final TemplateStatsRepository statsRepository;

    /**
     * Cria um novo template de notificação e dispara o evento de criação.
     */
    @PostMapping
    public ResponseEntity<TemplateResponse> create(@RequestBody @Valid CreateTemplateRequest request) {
        NotificationTemplate template = templateService.createTemplate(
                request.name(),
                request.description(),
                request.channel(),
                request.orgId(),
                request.workspaceId()
        );

        eventProducer.publish(new TemplateCreatedEvent(template.getId(), OffsetDateTime.now(), template.getName()));

        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(template));
    }

    /**
     * Recupera os detalhes de um template pelo ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TemplateResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(mapper.toResponse(templateService.getById(id)));
    }

    /**
     * Recupera as estatísticas consolidadas de um template (Lado de Leitura do CQRS).
     */
    @GetMapping("/{id}/stats")
    public ResponseEntity<StatsResponse> getStats(@PathVariable String id) {
        TemplateStatsView stats = statsRepository.findById(id)
                .orElse(TemplateStatsView.builder()
                        .templateId(id)
                        .totalSent(0)
                        .successCount(0)
                        .errorCount(0)
                        .build());

        return ResponseEntity.ok(mapper.toStatsResponse(stats));
    }

    /**
     * Executa a renderização de um template e registra o log de auditoria.
     */
    @PostMapping("/{id}/execute")
    public ResponseEntity<ExecutionResponse> execute(
            @PathVariable String id,
            @RequestBody @Valid ExecutionRequest request) {

        NotificationExecution execution = templateService.executeTemplate(
                id,
                request.templateVersionId(),
                request.recipients(),
                request.variables()
        );

        eventProducer.publish(new NotificationDispatchedEvent(id, execution.getStatus().name()));

        ExecutionResponse response = new ExecutionResponse(
                execution.getId(),
                execution.getRenderedContent(),
                execution.getStatus().name(),
                execution.getExecutedOn()
        );

        return ResponseEntity.ok(response);
    }
}