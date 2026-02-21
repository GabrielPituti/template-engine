package com.vaas.templateengine.application.dto;

import com.vaas.templateengine.domain.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Mapper centralizado para DTOs de API.
 * Agora inclui suporte para respostas paginadas, evitando que o modelo do Spring Data vaze para o contrato.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TemplateMapper {

    @Mapping(target = "id", source = "id")
    TemplateResponse toResponse(NotificationTemplate template);

    StatsResponse toStatsResponse(TemplateStatsView stats);

    default String map(SemanticVersion value) {
        return value != null ? value.toString() : null;
    }

    /**
     * Converte uma página de agregados em uma lista de DTOs de resposta simplificados.
     */
    List<TemplateResponse> toResponseList(List<NotificationTemplate> templates);

    /**
     * Wrapper para respostas paginadas.
     */
    record PagedResponse<T>(
            List<T> content,
            long totalElements,
            int totalPages,
            int size,
            int number
    ) {}

    default PagedResponse<TemplateResponse> toPagedResponse(Page<NotificationTemplate> page) {
        return new PagedResponse<>(
                toResponseList(page.getContent()),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getSize(),
                page.getNumber()
        );
    }

    record CreateTemplateRequest(String name, String description, Channel channel, String orgId, String workspaceId) {}
    record TemplateResponse(String id, String name, String description, Channel channel, String status, OffsetDateTime createdAt, List<VersionResponse> versions) {}
    record VersionResponse(String id, String version, String estado, String body, List<InputVariable> inputSchema) {}
    record ExecutionRequest(String templateVersionId, List<String> recipients, Map<String, Object> variables) {}
    record ExecutionResponse(String executionId, String renderedContent, String status, OffsetDateTime executedOn) {}
    record StatsResponse(String templateId, String templateName, long totalSent, long successCount, long errorCount, OffsetDateTime lastExecutedAt) {}
}