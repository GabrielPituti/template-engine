Notification Template Engine - Fase 5: API, Mensageria e CQRS

Esta etapa consolidou a engine como um microsserviço distribuído e resiliente. Trabalhei na integração da exposição REST com a comunicação assíncrona baseada em eventos de domínio.

Entregas Técnicas

API Contract-First: Desenhei a interface do sistema primeiro via OpenAPI 3.1, garantindo que o contrato da API fosse a fonte única da verdade durante o desenvolvimento.

Arquitetura CQRS: Implementei um fluxo onde comandos alteram o estado e eventos de domínio disparam a atualização de visões de leitura otimizadas.

Kafka Consumer: Desenvolvi o processamento assíncrono do evento NotificationDispatchedEvent para alimentar uma coleção dedicada de estatísticas de envio.

Camada de Cache: Utilizei o Caffeine Cache para otimizar a recuperação de templates publicados, reduzindo a latência em cenários de alta volumetria.

MapStruct: Implementei o mapeamento entre as entidades de domínio e os DTOs de resposta, garantindo que detalhes internos da implementação permaneçam isolados.

Decisões Estratégicas

Separação de Leitura e Escrita: Adotei o CQRS para as estatísticas para garantir que consultas analíticas pesadas não impactem a performance da base principal de templates.

Documentação Viva: Configurei o Swagger UI para ler o contrato OpenAPI estático, facilitando a integração com consumidores externos e mantendo a documentação sempre sincronizada com o código.

Como Monitorar

Swagger UI: http://localhost:8080/swagger-ui.html

Visualizador Kafka (Kafdrop): http://localhost:9000

Métricas de Saúde (Actuator): http://localhost:8080/actuator/health
