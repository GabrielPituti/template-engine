package com.vaas.templateengine.application.dto;

import com.vaas.templateengine.domain.model.*;
import com.vaas.templateengine.infrastructure.web.dto.InputVariableDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Interface de mapeamento centralizada utilizando MapStruct.
 * Realiza a tradução entre agregados de domínio e DTOs de exposição.
 * A configuração unmappedTargetPolicy garante que campos não mapeados não causem
 * falhas silenciosas, enquanto as expressões customizadas tratam conversões de Enums.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TemplateMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "status", expression = "java(template.getStatus().name())")
    @Mapping(target = "channel", expression = "java(template.getChannel().name())")
    TemplateResponse toResponse(NotificationTemplate template);

    StatsResponse toStatsResponse(TemplateStatsView stats);

    /**
     * Converte o Value Object SemanticVersion para sua representação textual.
     */
    default String map(SemanticVersion value) {
        return value != null ? value.toString() : null;
    }

    List<TemplateResponse> toResponseList(List<NotificationTemplate> templates);

    /**
     * Mapeamentos explícitos para blindagem do domínio.
     */
    InputVariableDto toInputVariableDto(InputVariable domain);
    InputVariable toInputVariableDomain(InputVariableDto dto);
    List<InputVariableDto> toInputVariableDtoList(List<InputVariable> domainList);
    List<InputVariable> toInputVariableDomainList(List<InputVariableDto> dtoList);

    /**
     * Estrutura de transporte para respostas paginadas.
     * Mantém a consistência do contrato REST independentemente da tecnologia de persistência.
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

    record TemplateResponse(
            String id,
            String name,
            String description,
            String channel,
            String status,
            OffsetDateTime createdAt,
            List<VersionResponse> versions
    ) {}

    record VersionResponse(
            String id,
            String version,
            String estado,
            String body,
            List<InputVariableDto> inputSchema
    ) {}

    record ExecutionRequest(String templateVersionId, List<String> recipients, Map<String, Object> variables) {}

    record ExecutionResponse(String executionId, String renderedContent, String status, OffsetDateTime executedOn) {}

    record StatsResponse(String templateId, String templateName, long totalSent, long successCount, long errorCount, OffsetDateTime lastExecutedAt) {}
}