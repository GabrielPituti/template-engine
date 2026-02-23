package com.vaas.templateengine.infrastructure.messaging;

import com.vaas.templateengine.domain.event.NotificationDispatchedEvent;
import com.vaas.templateengine.domain.event.TemplateCreatedEvent;
import com.vaas.templateengine.domain.model.TemplateStatsView;
import com.vaas.templateengine.infrastructure.persistence.TemplateStatsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para validação do consumidor de eventos e projeção CQRS.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Messaging: Notification Consumer")
class NotificationConsumerTest {

    @Mock
    private TemplateStatsRepository statsRepository;

    @InjectMocks
    private NotificationConsumer consumer;

    @Test
    @DisplayName("Deve inicializar estatísticas com o nome do template ao receber evento de criação")
    void shouldInitializeStatsOnCreationEvent() {
        TemplateCreatedEvent event = new TemplateCreatedEvent("id-1", OffsetDateTime.now(), "Template Teste");

        consumer.consumeTemplateCreated(event);

        ArgumentCaptor<TemplateStatsView> captor = ArgumentCaptor.forClass(TemplateStatsView.class);
        verify(statsRepository).save(captor.capture());

        TemplateStatsView saved = captor.getValue();
        assertEquals("id-1", saved.getTemplateId());
        assertEquals("Template Teste", saved.getTemplateName());
        assertEquals(0, saved.getTotalSent());
    }

    @Test
    @DisplayName("Deve incrementar contadores corretamente ao receber evento de despacho")
    void shouldUpdateCountersOnDispatchEvent() {
        TemplateStatsView existing = TemplateStatsView.builder()
                .templateId("id-1").templateName("Teste").totalSent(5).build();

        when(statsRepository.findById("id-1")).thenReturn(Optional.of(existing));
        NotificationDispatchedEvent event = new NotificationDispatchedEvent("id-1", "SUCCESS");

        consumer.consumeNotificationDispatched(event);

        verify(statsRepository).save(argThat(stats ->
                stats.getTotalSent() == 6 && stats.getSuccessCount() == 1
        ));
    }
}