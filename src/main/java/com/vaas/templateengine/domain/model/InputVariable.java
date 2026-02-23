package com.vaas.templateengine.domain.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Value Object: Variável esperada no inputSchema.
 */
public record InputVariable(@NotBlank String name, @NotNull VariableType type, boolean required) {
}
