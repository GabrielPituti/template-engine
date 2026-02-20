package com.vaas.templateengine.domain.model;

/**
 * Value Object que representa o versionamento semântico (major.minor.patch).
 * Implementa Comparable para facilitar a busca pela "última versão publicada".
 */
public record SemanticVersion(int major, int minor, int patch) implements Comparable<SemanticVersion> {

    public static SemanticVersion initial() {
        return new SemanticVersion(1, 0, 0);
    }

    @Override
    public String toString() {
        return String.format("%d.%d.%d", major, minor, patch);
    }

    @Override
    public int compareTo(SemanticVersion other) {
        if (this.major != other.major) return Integer.compare(this.major, other.major);
        if (this.minor != other.minor) return Integer.compare(this.minor, other.minor);
        return Integer.compare(this.patch, other.patch);
    }
}