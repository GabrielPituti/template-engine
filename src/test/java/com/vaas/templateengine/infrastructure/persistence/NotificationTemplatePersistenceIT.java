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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Teste de Integração: Valida Persistência e Proteção contra Race Conditions.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Persistência e Concorrência de Templates")
class NotificationTemplatePersistenceIT {

    @Autowired
    private NotificationTemplateRepository repository;

    @Test
    @DisplayName("Deve salvar e recuperar um template com sucesso")
    void shouldSaveAndRetrieveTemplate() {
        NotificationTemplate template = NotificationTemplate.builder()
                .name("Welcome Email")
                .channel(Channel.EMAIL)
                .orgId("org-1")
                .workspaceId("wp-1")
                .status(TemplateStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        NotificationTemplate saved = repository.save(template);
        assertNotNull(saved.getId());
        assertEquals(0L, saved.getInternalVersion()); // Primeira versão é 0
    }

    @Test
    @DisplayName("Deve lançar OptimisticLockingFailureException em caso de edições simultâneas")
    void shouldHandleRaceConditionWithOptimisticLocking() {
        // 1. Salva um template original
        NotificationTemplate original = repository.save(NotificationTemplate.builder()
                .name("Race Test")
                .orgId("org-1")
                .status(TemplateStatus.ACTIVE)
                .build());

        // 2. Simula duas instâncias carregando o mesmo documento
        NotificationTemplate inst1 = repository.findById(original.getId()).get();
        NotificationTemplate inst2 = repository.findById(original.getId()).get();

        // 3. Primeira instância atualiza com sucesso
        inst1.setName("Update 1");
        repository.save(inst1);

        // 4. Segunda instância tenta atualizar com a versão antiga -> Deve falhar
        inst2.setName("Update 2");
        assertThrows(OptimisticLockingFailureException.class, () -> {
            repository.save(inst2);
        });
    }
}