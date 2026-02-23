package com.vaas.templateengine.infrastructure.messaging;

import com.vaas.templateengine.domain.event.NotificationDispatchedEvent;
import com.vaas.templateengine.domain.event.TemplateCreatedEvent;
import com.vaas.templateengine.domain.model.TemplateStatsView;
import com.vaas.templateengine.infrastructure.persistence.TemplateStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor de eventos de domínio para manutenção do Read Model de estatísticas.
 * Implementa a estratégia de projeção de dados assíncrona do padrão CQRS, garantindo a
 * sincronização entre o ciclo de vida do template e a visão analítica.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final TemplateStatsRepository statsRepository;

    /**
     * Inicializa a projeção de estatísticas ao detectar a criação de um novo template.
     * Garante que o nome do recurso esteja disponível para consultas analíticas
     * desde o provisionamento do agregado.
     */
    @KafkaListener(topics = "template-created", groupId = "template-engine-stats")
    public void consumeTemplateCreated(TemplateCreatedEvent event) {
        try {
            TemplateStatsView stats = TemplateStatsView.builder()
                    .templateId(event.aggregateId())
                    .templateName(event.name())
                    .totalSent(0)
                    .successCount(0)
                    .errorCount(0)
                    .build();

            statsRepository.save(stats);
            log.debug("Projeção inicializada para o template: {}", event.name());
        } catch (Exception e) {
            log.error("Falha ao inicializar projeção para o template {}: {}",
                    event.aggregateId(), e.getMessage());
        }
    }

    /**
     * Processa eventos de despacho para atualização incremental dos contadores.
     * Utiliza lógica de recuperação resiliente para garantir a integridade dos dados
     * mesmo em cenários de processamento fora de ordem.
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

            log.debug("Estatísticas atualizadas para o template ID: {}", event.aggregateId());
        } catch (Exception e) {
            log.error("Erro no incremento da projeção analítica para o template {}: {}",
                    event.aggregateId(), e.getMessage());
        }
    }
}