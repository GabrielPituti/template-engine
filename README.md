Notification Template Engine - Fase 5: API, Mensageria e CQRS

Esta fase conectou tudo: exposição REST, publicação de eventos de domínio
e separação entre leitura e escrita.

O que foi construído

Contrato OpenAPI 3.1: os schemas de CreateVersionRequest e InputVariableDto
foram definidos primeiro no YAML e depois implementados no código. Isso
garante que a implementação não derive da especificação de forma silenciosa.

Eventos de domínio via Kafka: quatro eventos selados (sealed interface) —
TemplateCreatedEvent, TemplateVersionPublishedEvent,
NotificationDispatchedEvent e TemplateArchivedEvent. O uso de sealed
interface garante em tempo de compilação que nenhum tipo de evento seja
ignorado no switch do NotificationProducer.

CQRS para estatísticas: o NotificationConsumer escuta o tópico
notification-dispatched e atualiza a TemplateStatsView de forma assíncrona.
Consultas analíticas não tocam a coleção principal de templates.

Caffeine Cache: recuperação de templates com TTL de 10 minutos e limite de
500 entradas. Invalidação explícita via @CacheEvict em todas as operações
de escrita.

MapStruct para isolamento: InputVariable do domínio nunca vaza para o
contrato REST — existe um InputVariableDto específico mapeado pelo
TemplateMapper.

Como acompanhar

  Swagger UI  → http://localhost:8080/swagger-ui.html
  Kafdrop     → http://localhost:9000
  Actuator    → http://localhost:8080/actuator/health
