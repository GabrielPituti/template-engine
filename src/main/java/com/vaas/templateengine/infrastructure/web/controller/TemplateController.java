package com.vaas.templateengine.infrastructure.web.controller;

import com.vaas.templateengine.application.dto.TemplateMapper;
import com.vaas.templateengine.application.dto.TemplateMapper.*;
import com.vaas.templateengine.application.service.TemplateService;
import com.vaas.templateengine.domain.event.NotificationDispatchedEvent;
import com.vaas.templateengine.domain.event.TemplateCreatedEvent;
import com.vaas.templateengine.domain.model.*;
import com.vaas.templateengine.infrastructure.messaging.NotificationProducer;
import com.vaas.templateengine.infrastructure.persistence.TemplateStatsRepository;
import com.vaas.templateengine.domain.port.NotificationTemplateRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

/**
 * Controller REST em conformidade com o RF01 e RF03 (CQRS).
 */
@RestController
@RequestMapping("/v1/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;
    private final TemplateMapper mapper;
    private final NotificationProducer eventProducer;
    private final TemplateStatsRepository statsRepository;
    private final NotificationTemplateRepository templateRepository;

    @PostMapping
    public ResponseEntity<TemplateResponse> create(@RequestBody @Valid CreateTemplateRequest request) {
        NotificationTemplate template = templateService.createTemplate(
                request.name(), request.description(), request.channel(),
                request.orgId(), request.workspaceId()
        );
        eventProducer.publish(new TemplateCreatedEvent(template.getId(), OffsetDateTime.now(), template.getName()));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(template));
    }

    /**
     * Lista templates com paginação e filtros (RF01).
     * Exemplo: GET /v1/templates?orgId=org-1&workspaceId=wp-1&channel=EMAIL&page=0&size=10
     */
    @GetMapping
    public ResponseEntity<PagedResponse<TemplateResponse>> list(
            @RequestParam String orgId,
            @RequestParam String workspaceId,
            @RequestParam(required = false) Channel channel,
            @RequestParam(required = false) TemplateStatus status,
            @PageableDefault(size = 20) Pageable pageable) {

        var page = templateRepository.findAll(orgId, workspaceId, channel, status, pageable);
        return ResponseEntity.ok(mapper.toPagedResponse(page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TemplateResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(mapper.toResponse(templateService.getById(id)));
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<StatsResponse> getStats(@PathVariable String id) {
        TemplateStatsView stats = statsRepository.findById(id)
                .orElse(TemplateStatsView.builder().templateId(id).build());
        return ResponseEntity.ok(mapper.toStatsResponse(stats));
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<ExecutionResponse> execute(
            @PathVariable String id,
            @RequestBody @Valid ExecutionRequest request) {
        NotificationExecution execution = templateService.executeTemplate(
                id, request.templateVersionId(), request.recipients(), request.variables()
        );
        eventProducer.publish(new NotificationDispatchedEvent(id, execution.getStatus().name()));
        return ResponseEntity.ok(new ExecutionResponse(
                execution.getId(), execution.getRenderedContent(),
                execution.getStatus().name(), execution.getExecutedOn()
        ));
    }
}