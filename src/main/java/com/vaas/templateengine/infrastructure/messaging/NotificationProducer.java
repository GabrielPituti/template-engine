package com.vaas.templateengine.infrastructure.messaging;

import com.vaas.templateengine.domain.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Adapter de saída responsável pela publicação de eventos de domínio no Kafka.
 * Implementa a integração assíncrona necessária para o padrão CQRS e auditoria externa.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publica um evento de domínio no tópico correspondente.
     * Utiliza a chave do agregado como chave da mensagem para garantir a ordenação por partição.
     * * @param event O evento de domínio a ser disparado.
     */
    public void publish(DomainEvent event) {
        String topic = resolveTopic(event);
        String key = event.aggregateId();

        kafkaTemplate.send(topic, key, event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Evento [{} : {}] publicado com sucesso na partição {}",
                                event.getClass().getSimpleName(), key, result.getRecordMetadata().partition());
                    } else {
                        log.error("Falha ao publicar evento [{} : {}]: {}",
                                event.getClass().getSimpleName(), key, ex.getMessage());
                    }
                });
    }

    private String resolveTopic(DomainEvent event) {
        return switch (event) {
            case com.vaas.templateengine.domain.event.TemplateCreatedEvent e -> "template-created";
            case com.vaas.templateengine.domain.event.TemplateVersionPublishedEvent e -> "template-published";
            case com.vaas.templateengine.domain.event.NotificationDispatchedEvent e -> "notification-dispatched";
            case com.vaas.templateengine.domain.event.TemplateArchivedEvent e -> "template-archived";
        };
    }
}