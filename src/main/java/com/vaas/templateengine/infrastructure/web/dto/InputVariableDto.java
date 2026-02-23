package com.vaas.templateengine.infrastructure.web.dto;

import com.vaas.templateengine.domain.model.VariableType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object que define o contrato de entrada/saída para variáveis de template.
 * Sua existência garante que mudanças internas no Value Object de domínio não
 * impactem diretamente os consumidores da API, mantendo a estabilidade do contrato.
 */
public record InputVariableDto(
        @NotBlank(message = "O nome da variável é obrigatório")
        String name,

        @NotNull(message = "O tipo da variável é obrigatório")
        VariableType type,

        boolean required
) {}