package com.vaas.templateengine.infrastructure.persistence;

import com.vaas.templateengine.domain.model.Channel;
import com.vaas.templateengine.domain.model.NotificationTemplate;
import com.vaas.templateengine.domain.model.TemplateStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Adapter técnico para o MongoDB.
 * Utiliza JSON queries dinâmicas para suportar a filtragem opcional do RF01.
 */
@Repository
public interface SpringDataMongoTemplateRepository extends MongoRepository<NotificationTemplate, String> {

    /**
     * Query que ignora filtros nulos de forma sênior.
     * O uso de lógica condicional ($or com $expr) no MongoDB garante que, se o parâmetro for nulo (null),
     * a restrição seja ignorada para aquele campo específico.
     */
    @Query("{ 'orgId': ?0, 'workspaceId': ?1, " +
            "  $and: [ " +
            "    { $or: [ { $expr: { $eq: [?2, null] } }, { 'channel': ?2 } ] }, " +
            "    { $or: [ { $expr: { $eq: [?3, null] } }, { 'status': ?3 } ] } " +
            "  ]" +
            "}")
    Page<NotificationTemplate> findByFilters(
            String orgId,
            String workspaceId,
            Channel channel,
            TemplateStatus status,
            Pageable pageable
    );
}