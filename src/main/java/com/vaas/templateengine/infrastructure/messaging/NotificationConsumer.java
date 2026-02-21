package com.vaas.templateengine.infrastructure.messaging;

import com.vaas.templateengine.domain.event.NotificationDispatchedEvent;
import com.vaas.templateengine.domain.model.TemplateStatsView;
import com.vaas.templateengine.infrastructure.persistence.TemplateStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumer responsável por processar eventos de despacho e atualizar as projeções de leitura.
 * Implementa a reatividade necessária para manter a consistência eventual do modelo CQRS.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final TemplateStatsRepository statsRepository;

    /**
     * Escuta o tópico de notificações disparadas para atualizar os contadores de performance.
     * @param event Evento contendo o ID do template e o status do despacho.
     */
    @KafkaListener(topics = "notification-dispatched", groupId = "template-engine-stats")
    public void consumeNotificationDispatched(NotificationDispatchedEvent event) {
        log.info("Processando estatísticas para o template: {}", event.aggregateId());

        TemplateStatsView stats = statsRepository.findById(event.aggregateId())
                .orElse(TemplateStatsView.builder()
                        .templateId(event.aggregateId())
                        .totalSent(0)
                        .successCount(0)
                        .errorCount(0)
                        .build());

        boolean isSuccess = "SUCCESS".equalsIgnoreCase(event.status());
        stats.increment(isSuccess);

        statsRepository.save(stats);
    }
}