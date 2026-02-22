package com.vaas.templateengine.infrastructure.web.controller;

import com.vaas.templateengine.application.dto.TemplateMapper;
import com.vaas.templateengine.application.dto.TemplateMapper.*;
import com.vaas.templateengine.application.service.TemplateService;
import com.vaas.templateengine.domain.model.*;
import com.vaas.templateengine.domain.port.NotificationTemplateRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Adaptador de entrada (Primary Adapter) que expõe os recursos de templates via REST.
 * Responsável pela conformidade com o contrato OpenAPI e orquestração de chamadas ao serviço,
 * garantindo o isolamento da lógica de negócio.
 */
@RestController
@RequestMapping("/v1/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;
    private final TemplateMapper mapper;
    private final NotificationTemplateRepository templateRepository;

    @PostMapping
    public ResponseEntity<TemplateResponse> create(@RequestBody @Valid CreateTemplateRequest request) {
        NotificationTemplate template = templateService.createTemplate(
                request.name(), request.description(), request.channel(),
                request.orgId(), request.workspaceId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(template));
    }

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
        return ResponseEntity.ok(mapper.toStatsResponse(templateService.getStats(id)));
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<ExecutionResponse> execute(
            @PathVariable String id,
            @RequestBody @Valid ExecutionRequest request) {
        NotificationExecution execution = templateService.executeTemplate(
                id, request.templateVersionId(), request.recipients(), request.variables()
        );
        return ResponseEntity.ok(new ExecutionResponse(
                execution.getId(), execution.getRenderedContent(),
                execution.getStatus().name(), execution.getExecutedOn()
        ));
    }

    @PostMapping("/{id}/versions")
    public ResponseEntity<TemplateResponse> createVersion(
            @PathVariable String id,
            @RequestBody @Valid CreateVersionRequest request) {
        NotificationTemplate template = templateService.createVersion(id, request,
                mapper.toInputVariableDomainList(request.inputSchema()));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(template));
    }

    @PatchMapping("/{id}/versions/{versionId}")
    public ResponseEntity<TemplateResponse> updateVersion(
            @PathVariable String id,
            @PathVariable String versionId,
            @RequestBody @Valid UpdateVersionRequest request) {
        NotificationTemplate template = templateService.updateVersion(
                id, versionId, request.body(), request.subject(),
                mapper.toInputVariableDomainList(request.inputSchema()), request.changelog());
        return ResponseEntity.ok(mapper.toResponse(template));
    }

    @GetMapping("/{id}/versions")
    public ResponseEntity<List<VersionResponse>> listVersions(@PathVariable String id) {
        NotificationTemplate template = templateService.getById(id);
        return ResponseEntity.ok(mapper.toVersionResponseList(template.getVersions()));
    }

    @PostMapping("/{id}/versions/{versionId}/publish")
    public ResponseEntity<TemplateResponse> publish(
            @PathVariable String id,
            @PathVariable String versionId) {
        NotificationTemplate template = templateService.publishVersion(id, versionId);
        return ResponseEntity.ok(mapper.toResponse(template));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> archive(@PathVariable String id) {
        templateService.archiveTemplate(id);
        return ResponseEntity.noContent().build();
    }
}