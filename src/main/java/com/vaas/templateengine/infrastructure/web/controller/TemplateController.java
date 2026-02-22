package com.vaas.templateengine.infrastructure.web.controller;

import com.vaas.templateengine.application.dto.TemplateMapper;
import com.vaas.templateengine.application.dto.TemplateMapper.*;
import com.vaas.templateengine.application.service.TemplateService;
import com.vaas.templateengine.domain.model.*;
import com.vaas.templateengine.domain.port.NotificationTemplateRepository;
import com.vaas.templateengine.infrastructure.persistence.TemplateStatsRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Ponto de entrada da API para gestão e execução de templates.
 * Atua como o adaptador primário na Arquitetura Hexagonal, sendo responsável
 * pela validação de contratos de entrada e pela orquestração de respostas
 * em conformidade com a especificação OpenAPI.
 */
@RestController
@RequestMapping("/v1/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;
    private final TemplateMapper mapper;
    private final TemplateStatsRepository statsRepository;
    private final NotificationTemplateRepository templateRepository;

    /**
     * Endpoint para criação de novos agregados de template.
     * Delega a orquestração para o serviço de aplicação para garantir que o
     * ciclo de vida inicial e a emissão de eventos ocorram de forma atômica.
     */
    @PostMapping
    public ResponseEntity<TemplateResponse> create(@RequestBody @Valid CreateTemplateRequest request) {
        NotificationTemplate template = templateService.createTemplate(
                request.name(), request.description(), request.channel(),
                request.orgId(), request.workspaceId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(template));
    }

    /**
     * Lista templates utilizando paginação e filtros dinâmicos.
     * Esta implementação cumpre o requisito de busca multi-tenant, exigindo
     * obrigatoriamente os identificadores de organização e workspace.
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

    /**
     * Expõe a visão de leitura (Read Model) das estatísticas do template.
     * Cumpre o papel de Query no padrão CQRS, acessando uma projeção otimizada
     * e desacoplada da escrita principal.
     */
    @GetMapping("/{id}/stats")
    public ResponseEntity<StatsResponse> getStats(@PathVariable String id) {
        TemplateStatsView stats = statsRepository.findById(id)
                .orElse(TemplateStatsView.builder().templateId(id).build());
        return ResponseEntity.ok(mapper.toStatsResponse(stats));
    }

    /**
     * Aciona o motor de renderização e registra o log de execução.
     * A segurança da operação (como travas de arquivamento e publicação)
     * é garantida pela camada de serviço de aplicação.
     */
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

    /**
     * Promove um rascunho para versão publicada.
     * Essencial para o fluxo de imutabilidade, selando o conteúdo para execuções futuras.
     */
    @PostMapping("/{id}/versions/{versionId}/publish")
    public ResponseEntity<TemplateResponse> publish(
            @PathVariable String id,
            @PathVariable String versionId) {
        NotificationTemplate template = templateService.publishVersion(id, versionId);
        return ResponseEntity.ok(mapper.toResponse(template));
    }

    /**
     * Realiza o arquivamento do template.
     * Implementado como Soft Delete para preservação da integridade histórica dos logs de envio.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> archive(@PathVariable String id) {
        templateService.archiveTemplate(id);
        return ResponseEntity.noContent().build();
    }
}