package com.vaas.templateengine.infrastructure.persistence;

import com.vaas.templateengine.domain.model.TemplateStatsView;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório específico para a projeção de leitura de estatísticas.
 * Utilizado exclusivamente pelo fluxo de consultas (Queries) e pelo Consumer de eventos.
 */
@Repository
public interface TemplateStatsRepository extends MongoRepository<TemplateStatsView, String> {
}