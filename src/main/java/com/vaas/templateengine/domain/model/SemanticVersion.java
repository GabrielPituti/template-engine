package com.vaas.templateengine.domain.model;

/**
 * Value Object: Encapsula a lógica de versão major.minor.patch.
 * Implementa Comparable para permitir ordenação natural de versões.
 */
public record SemanticVersion(int major, int minor, int patch) implements Comparable<SemanticVersion> {

    public static SemanticVersion initial() {
        return new SemanticVersion(1, 0, 0);
    }

    public SemanticVersion nextPatch() {
        return new SemanticVersion(major, minor, patch + 1);
    }

    public SemanticVersion nextMinor() {
        return new SemanticVersion(major, minor + 1, 0);
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