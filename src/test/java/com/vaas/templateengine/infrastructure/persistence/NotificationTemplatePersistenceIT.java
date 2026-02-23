package com.vaas.templateengine.infrastructure.persistence;

import com.vaas.templateengine.TestcontainersConfiguration;
import com.vaas.templateengine.domain.model.Channel;
import com.vaas.templateengine.domain.model.NotificationTemplate;
import com.vaas.templateengine.domain.model.TemplateStatus;
import com.vaas.templateengine.domain.port.NotificationTemplateRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integração para validação da camada de persistência e filtros dinâmicos.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@DisplayName("Persistência: Filtros e Concorrência")
class NotificationTemplatePersistenceIT {

    @Autowired
    private NotificationTemplateRepository repository;

    @Test
    @DisplayName("Deve realizar busca paginada aplicando filtros opcionais e ignorando nulos")
    void shouldFilterTemplatesCorrectly() {
        repository.save(NotificationTemplate.builder()
                .name("Email Tpl").channel(Channel.EMAIL).orgId("org-1").workspaceId("wp-1")
                .status(TemplateStatus.ACTIVE).createdAt(OffsetDateTime.now()).build());

        repository.save(NotificationTemplate.builder()
                .name("SMS Tpl").channel(Channel.SMS).orgId("org-1").workspaceId("wp-1")
                .status(TemplateStatus.ACTIVE).createdAt(OffsetDateTime.now()).build());

        var emailOnly = repository.findAll("org-1", "wp-1", Channel.EMAIL, null, PageRequest.of(0, 10));
        assertEquals(1, emailOnly.getTotalElements());

        var all = repository.findAll("org-1", "wp-1", null, null, PageRequest.of(0, 10));
        assertEquals(2, all.getTotalElements());
    }

    @Test
    @DisplayName("Deve lançar OptimisticLockingFailureException em caso de edições simultâneas")
    void shouldHandleRaceConditionWithOptimisticLocking() {
        NotificationTemplate original = repository.save(NotificationTemplate.builder()
                .name("Race Test").orgId("org-1").status(TemplateStatus.ACTIVE).createdAt(OffsetDateTime.now()).build());

        NotificationTemplate inst1 = repository.findById(original.getId()).orElseThrow();
        NotificationTemplate inst2 = repository.findById(original.getId()).orElseThrow();

        inst1.updateInformation("Update 1", original.getDescription());
        repository.save(inst1);

        inst2.updateInformation("Update 2", original.getDescription());
        assertThrows(OptimisticLockingFailureException.class, () -> repository.save(inst2));
    }
}