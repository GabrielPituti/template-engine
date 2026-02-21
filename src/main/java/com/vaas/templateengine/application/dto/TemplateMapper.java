package com.vaas.templateengine.application.dto;

import com.vaas.templateengine.domain.model.NotificationTemplate;
import com.vaas.templateengine.domain.model.TemplateVersion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Interface de mapeamento responsável por converter objetos de domínio em objetos de transferência (DTOs).
 * Utiliza o componente MapStruct para geração de código performático em tempo de compilação.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TemplateMapper {

    /**
     * Mapeia a entidade de domínio NotificationTemplate para o DTO de visualização.
     * * @param template Entidade de domínio.
     * @return DTO formatado para resposta da API.
     */
    // TemplateResponse toResponse(NotificationTemplate template);
}