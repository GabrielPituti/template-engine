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
 * Correção Bug #5: Adicionado tratamento de erro para evitar loops de reprocessamento.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final TemplateStatsRepository statsRepository;

    @KafkaListener(topics = "notification-dispatched", groupId = "template-engine-stats")
    public void consumeNotificationDispatched(NotificationDispatchedEvent event) {
        try {
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
        } catch (Exception e) {
            // Log do erro para monitoramento (DLQ seria o ideal em produção)
            log.error("Erro ao processar evento do Kafka para o template {}: {}",
                    event.aggregateId(), e.getMessage());
        }
    }
}