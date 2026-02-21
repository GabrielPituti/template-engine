package com.vaas.templateengine.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Value Object: SemanticVersion")
class SemanticVersionTest {

    @Test
    @DisplayName("Deve iniciar corretamente como 1.0.0")
    void shouldStartAtInitialVersion() {
        SemanticVersion version = SemanticVersion.initial();
        assertEquals("1.0.0", version.toString());
    }

    @Test
    @DisplayName("Deve incrementar corretamente o Patch")
    void shouldIncrementPatch() {
        SemanticVersion v1 = new SemanticVersion(1, 2, 3);
        SemanticVersion v2 = v1.nextPatch();
        assertEquals("1.2.4", v2.toString());
    }

    @Test
    @DisplayName("Deve incrementar Minor e resetar Patch")
    void shouldIncrementMinorAndResetPatch() {
        SemanticVersion v1 = new SemanticVersion(1, 2, 3);
        SemanticVersion v2 = v1.nextMinor();
        assertEquals("1.3.0", v2.toString());
    }

    @Test
    @DisplayName("Deve comparar versões corretamente para ordenação")
    void shouldCompareVersions() {
        SemanticVersion v1 = new SemanticVersion(1, 0, 0);
        SemanticVersion v2 = new SemanticVersion(1, 1, 0);
        SemanticVersion v3 = new SemanticVersion(2, 0, 0);

        assertTrue(v1.compareTo(v2) < 0);
        assertTrue(v3.compareTo(v2) > 0);
        assertEquals(0, v1.compareTo(new SemanticVersion(1, 0, 0)));
    }
}