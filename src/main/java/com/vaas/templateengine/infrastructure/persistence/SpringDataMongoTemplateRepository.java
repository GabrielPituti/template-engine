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
 * Adapter (Detalhe Técnico): Interface do Spring Data que fala com o MongoDB.
 */
@Repository
public interface SpringDataMongoTemplateRepository extends MongoRepository<NotificationTemplate, String> {

    // Query dinâmica para suportar os filtros opcionais do RF01
    @Query("{ 'orgId': ?0, 'workspaceId': ?1, " +
            "'channel': { $skip: ?2 == null }, 'status': { $skip: ?3 == null } }")
    Page<NotificationTemplate> findByFilters(
            String orgId,
            String workspaceId,
            Channel channel,
            TemplateStatus status,
            Pageable pageable
    );
}