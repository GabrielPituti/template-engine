package com.vaas.templateengine.infrastructure.persistence;

import com.vaas.templateengine.domain.model.Channel;
import com.vaas.templateengine.domain.model.NotificationTemplate;
import com.vaas.templateengine.domain.model.TemplateStatus;
import com.vaas.templateengine.domain.port.NotificationTemplateRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integração para a camada de persistência.
 * Valida o comportamento do banco de dados e controle de concorrência.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Persistência e Concorrência de Templates")
class NotificationTemplatePersistenceIT {

    @Autowired
    private NotificationTemplateRepository repository;

    /**
     * Valida o ciclo básico de persistência e recuperação de um agregado.
     */
    @Test
    @DisplayName("Deve salvar e recuperar um template com sucesso")
    void shouldSaveAndRetrieveTemplate() {
        NotificationTemplate template = NotificationTemplate.builder()
                .name("Welcome Email")
                .channel(Channel.EMAIL)
                .orgId("org-1")
                .workspaceId("wp-1")
                .status(TemplateStatus.ACTIVE)
                .createdAt(OffsetDateTime.now())
                .build();

        NotificationTemplate saved = repository.save(template);
        assertNotNull(saved.getId());
        assertEquals(0L, saved.getInternalVersion());
    }

    /**
     * Valida se o controle de concorrência otimista (Optimistic Locking) impede
     * que edições simultâneas sobreescrevam dados incorretamente.
     */
    @Test
    @DisplayName("Deve lançar OptimisticLockingFailureException em caso de edições simultâneas")
    void shouldHandleRaceConditionWithOptimisticLocking() {
        NotificationTemplate original = repository.save(NotificationTemplate.builder()
                .name("Race Test")
                .orgId("org-1")
                .status(TemplateStatus.ACTIVE)
                .createdAt(OffsetDateTime.now())
                .build());

        NotificationTemplate inst1 = repository.findById(original.getId()).get();
        NotificationTemplate inst2 = repository.findById(original.getId()).get();

        inst1.setName("Update 1");
        repository.save(inst1);

        inst2.setName("Update 2");
        assertThrows(OptimisticLockingFailureException.class, () -> {
            repository.save(inst2);
        });
    }
}