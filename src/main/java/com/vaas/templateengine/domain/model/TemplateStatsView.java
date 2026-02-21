package com.vaas.templateengine.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.OffsetDateTime;

/**
 * Projeção de leitura (View) otimizada para consultas de estatísticas.
 * Representa o lado "Query" do CQRS, consolidando dados de eventos para resposta rápida.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "template_stats_view")
public class TemplateStatsView {

    @Id
    private String templateId;

    private String templateName;

    private long totalSent;

    private long successCount;

    private long errorCount;

    private OffsetDateTime lastExecutedAt;

    /**
     * Incrementa os contadores com base no status do despacho.
     * @param isSuccess Se o despacho foi bem-sucedido.
     */
    public void increment(boolean isSuccess) {
        this.totalSent++;
        if (isSuccess) {
            this.successCount++;
        } else {
            this.errorCount++;
        }
        this.lastExecutedAt = OffsetDateTime.now();
    }
}