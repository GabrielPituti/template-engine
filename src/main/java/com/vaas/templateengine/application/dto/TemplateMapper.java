package com.vaas.templateengine.application.dto;

import com.vaas.templateengine.domain.model.Channel;
import com.vaas.templateengine.domain.model.InputVariable;
import com.vaas.templateengine.domain.model.NotificationTemplate;
import com.vaas.templateengine.domain.model.SemanticVersion;
import com.vaas.templateengine.domain.model.TemplateStatsView;
import com.vaas.templateengine.domain.model.TemplateVersion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Interface de mapeamento responsável por converter objetos de domínio em DTOs de API.
 * Utiliza MapStruct para garantir alta performance e isolamento total do modelo de domínio.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TemplateMapper {

    /**
     * Converte o agregado de domínio para uma resposta detalhada da API.
     */
    @Mapping(target = "id", source = "id")
    TemplateResponse toResponse(NotificationTemplate template);

    /**
     * Converte a projeção de leitura de estatísticas para o DTO de resposta.
     */
    StatsResponse toStatsResponse(TemplateStatsView stats);

    /**
     * Método de suporte para conversão de SemanticVersion em String.
     * @param value O objeto de versão semântica.
     * @return Representação textual da versão.
     */
    default String map(SemanticVersion value) {
        return value != null ? value.toString() : null;
    }

    /** DTO para criação de template. */
    record CreateTemplateRequest(
            String name,
            String description,
            Channel channel,
            String orgId,
            String workspaceId
    ) {}

    /** DTO de resposta detalhada. */
    record TemplateResponse(
            String id,
            String name,
            String description,
            Channel channel,
            String status,
            OffsetDateTime createdAt,
            List<VersionResponse> versions
    ) {}

    /** DTO para representação de versões. */
    record VersionResponse(
            String id,
            String version,
            String estado,
            String body,
            List<InputVariable> inputSchema
    ) {}

    /** DTO para requisição de execução. */
    record ExecutionRequest(
            String templateVersionId,
            List<String> recipients,
            Map<String, Object> variables
    ) {}

    /** DTO para resposta de execução. */
    record ExecutionResponse(
            String executionId,
            String renderedContent,
            String status,
            OffsetDateTime executedOn
    ) {}

    /**
     * DTO de resposta para estatísticas consolidadas (CQRS).
     */
    record StatsResponse(
            String templateId,
            String templateName,
            long totalSent,
            long successCount,
            long errorCount,
            OffsetDateTime lastExecutedAt
    ) {}
}