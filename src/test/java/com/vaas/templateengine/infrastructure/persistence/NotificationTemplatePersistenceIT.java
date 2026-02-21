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
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integração para a camada de persistência de templates.
 * Valida a integração com o MongoDB e os mecanismos de proteção de integridade.
 * Utiliza o Testcontainers para garantir um ambiente isolado e idêntico ao de produção.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@DisplayName("Persistência e Concorrência de Templates")
class NotificationTemplatePersistenceIT {

    @Autowired
    private NotificationTemplateRepository repository;

    /**
     * Valida se o sistema é capaz de persistir e recuperar um template básico com fuso horário absoluto.
     */
    @Test
    @DisplayName("Deve salvar e recuperar um template com sucesso")
    void shouldSaveAndRetrieveTemplate() {
        // Cenário
        NotificationTemplate template = NotificationTemplate.builder()
                .name("Welcome Email")
                .channel(Channel.EMAIL)
                .orgId("org-123")
                .workspaceId("wp-456")
                .status(TemplateStatus.ACTIVE)
                .createdAt(OffsetDateTime.now())
                .build();

        // Ação
        NotificationTemplate saved = repository.save(template);

        // Validação
        assertNotNull(saved.getId());
        assertEquals(0L, saved.getInternalVersion());
        assertEquals("Welcome Email", saved.getName());
    }

    /**
     * Valida se o mecanismo de Optimistic Locking impede que duas edições simultâneas
     * resultem em perda de dados (Race Condition).
     */
    @Test
    @DisplayName("Deve lançar OptimisticLockingFailureException em caso de edições simultâneas")
    void shouldHandleRaceConditionWithOptimisticLocking() {
        // Cenário: Persiste um template original
        NotificationTemplate original = repository.save(NotificationTemplate.builder()
                .name("Race Test")
                .orgId("org-1")
                .status(TemplateStatus.ACTIVE)
                .createdAt(OffsetDateTime.now())
                .build());

        // Simulação: Duas instâncias carregam o mesmo documento do banco
        NotificationTemplate inst1 = repository.findById(original.getId()).orElseThrow();
        NotificationTemplate inst2 = repository.findById(original.getId()).orElseThrow();

        // Ação 1: Primeira instância atualiza com sucesso (incrementa a versão interna)
        inst1.setName("Update 1");
        repository.save(inst1);

        // Ação 2: Segunda instância tenta atualizar baseada na versão antiga
        inst2.setName("Update 2");

        // Validação: O Spring Data deve rejeitar a segunda atualização
        assertThrows(OptimisticLockingFailureException.class, () -> {
            repository.save(inst2);
        });
    }
}