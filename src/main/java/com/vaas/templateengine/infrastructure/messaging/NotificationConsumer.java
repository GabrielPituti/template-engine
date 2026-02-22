package com.vaas.templateengine.infrastructure.messaging;

import com.vaas.templateengine.domain.event.NotificationDispatchedEvent;
import com.vaas.templateengine.domain.model.TemplateStatsView;
import com.vaas.templateengine.infrastructure.persistence.TemplateStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor de eventos de domínio para manutenção de projeções analíticas.
 * Implementa o padrão CQRS (Command Query Responsibility Segregation),
 * desacoplando a atualização de estatísticas do fluxo principal de execução.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final TemplateStatsRepository statsRepository;

    /**
     * Processa notificações disparadas para atualizar a visão consolidada de métricas.
     * Inclui mecanismos de tolerância a falhas para evitar que inconsistências na
     * projeção interrompam o consumo de mensagens do broker.
     */
    @KafkaListener(topics = "notification-dispatched", groupId = "template-engine-stats")
    public void consumeNotificationDispatched(NotificationDispatchedEvent event) {
        try {
            TemplateStatsView stats = statsRepository.findById(event.aggregateId())
                    .orElse(TemplateStatsView.builder()
                            .templateId(event.aggregateId())
                            .totalSent(0)
                            .successCount(0)
                            .errorCount(0)
                            .build());

            stats.increment("SUCCESS".equalsIgnoreCase(event.status()));
            statsRepository.save(stats);

            log.debug("Estatísticas atualizadas para o template: {}", event.aggregateId());
        } catch (Exception e) {
            log.error("Falha na atualização da projeção de leitura para o template {}: {}",
                    event.aggregateId(), e.getMessage());
        }
    }
}