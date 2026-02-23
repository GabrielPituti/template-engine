package com.vaas.templateengine.domain.model;

/**
 * Objeto de Valor (Value Object) que encapsula a lógica de versionamento semântico.
 * Segue o padrão Major.Minor.Patch para garantir rastreabilidade e compatibilidade.
 */
public record SemanticVersion(int major, int minor, int patch) implements Comparable<SemanticVersion> {

    /**
     * Define a versão inicial padrão do sistema.
     * @return Instância representando 1.0.0.
     */
    public static SemanticVersion initial() {
        return new SemanticVersion(1, 0, 0);
    }

    /**
     * Incrementa a versão de correção (Patch), mantendo compatibilidade de contrato e conteúdo.
     * @return Nova instância com patch incrementado.
     */
    public SemanticVersion nextPatch() {
        return new SemanticVersion(major, minor, patch + 1);
    }

    /**
     * Incrementa a versão menor (Minor), indicando adição de funcionalidades ou alteração de schema.
     * @return Nova instância com minor incrementado e patch zerado.
     */
    public SemanticVersion nextMinor() {
        return new SemanticVersion(major, minor + 1, 0);
    }

    @Override
    public String toString() {
        return String.format("%d.%d.%d", major, minor, patch);
    }

    /**
     * Compara versões para fins de ordenação cronológica e lógica.
     */
    @Override
    public int compareTo(SemanticVersion other) {
        if (this.major != other.major) return Integer.compare(this.major, other.major);
        if (this.minor != other.minor) return Integer.compare(this.minor, other.minor);
        return Integer.compare(this.patch, other.patch);
    }
}