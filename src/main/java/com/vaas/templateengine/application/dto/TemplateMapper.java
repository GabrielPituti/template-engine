package com.vaas.templateengine.application.dto;

import com.vaas.templateengine.domain.model.*;
import com.vaas.templateengine.infrastructure.web.dto.InputVariableDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Interface de mapeamento centralizada utilizando MapStruct.
 * Implementa o isolamento entre camadas, garantindo que o contrato da API (DTOs)
 * permaneça estável mesmo diante de evoluções no modelo de domínio.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TemplateMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "status", expression = "java(template.getStatus().name())")
    @Mapping(target = "channel", expression = "java(template.getChannel().name())")
    TemplateResponse toResponse(NotificationTemplate template);

    StatsResponse toStatsResponse(TemplateStatsView stats);

    default String map(SemanticVersion value) {
        return value != null ? value.toString() : null;
    }

    List<TemplateResponse> toResponseList(List<NotificationTemplate> templates);

    List<VersionResponse> toVersionResponseList(List<TemplateVersion> versions);

    InputVariableDto toInputVariableDto(InputVariable domain);
    InputVariable toInputVariableDomain(InputVariableDto dto);
    List<InputVariableDto> toInputVariableDtoList(List<InputVariable> domainList);
    List<InputVariable> toInputVariableDomainList(List<InputVariableDto> dtoList);

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

    record CreateTemplateRequest(
            @NotBlank(message = "O nome é obrigatório") String name,
            String description,
            @NotNull(message = "O canal é obrigatório") Channel channel,
            @NotBlank(message = "O orgId é obrigatório") String orgId,
            @NotBlank(message = "O workspaceId é obrigatório") String workspaceId
    ) {}

    record CreateVersionRequest(
            String subject,
            @NotBlank(message = "O corpo do template é obrigatório") String body,
            String changelog,
            List<InputVariableDto> inputSchema,
            boolean isMinor
    ) {}

    record UpdateVersionRequest(
            String subject,
            String body,
            String changelog,
            List<InputVariableDto> inputSchema
    ) {}

    record TemplateResponse(String id, String name, String description, String channel, String status, OffsetDateTime createdAt, List<VersionResponse> versions) {}
    record VersionResponse(String id, String version, String estado, String body, List<InputVariableDto> inputSchema) {}
    record ExecutionRequest(String templateVersionId, List<String> recipients, Map<String, Object> variables) {}
    record ExecutionResponse(String executionId, String renderedContent, String status, OffsetDateTime executedOn) {}
    record StatsResponse(String templateId, String templateName, long totalSent, long successCount, long errorCount, OffsetDateTime lastExecutedAt) {}
}