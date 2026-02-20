package com.vaas.templateengine.infrastructure.persistence;

import com.vaas.templateengine.domain.model.Channel;
import com.vaas.templateengine.domain.model.NotificationTemplate;
import com.vaas.templateengine.domain.model.TemplateStatus;
import com.vaas.templateengine.domain.port.NotificationTemplateRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Teste de Integração (IT): Valida a comunicação entre o Repository e o MongoDB.
 * Usa o suporte nativo do Spring Boot 3.5 para Docker Compose.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Persistência de Templates")
class NotificationTemplatePersistenceIT {

    @Autowired
    private NotificationTemplateRepository repository;

    @Test
    @DisplayName("Deve salvar e recuperar um template com sucesso")
    void shouldSaveAndRetrieveTemplate() {
        // Given (Dado)
        NotificationTemplate template = NotificationTemplate.builder()
                .name("Welcome Email")
                .description("Email enviado após o cadastro")
                .channel(Channel.EMAIL)
                .orgId("org-123")
                .workspaceId("wp-456")
                .status(TemplateStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        // When (Quando)
        NotificationTemplate savedTemplate = repository.save(template);

        // Then (Então)
        assertNotNull(savedTemplate.getId());
        Optional<NotificationTemplate> found = repository.findById(savedTemplate.getId());

        assertTrue(found.isPresent());
        assertEquals("Welcome Email", found.get().getName());
        assertEquals(Channel.EMAIL, found.get().getChannel());
    }
}