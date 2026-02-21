Notification Template Engine - Fase 5: API, Mensageria e Diferenciais

Nesta fase final, transformamos o motor de templates em um serviço distribuído e acessível, integrando a camada de exposição REST com eventos de alta performance e otimizações de nível Big Tech.

🛠️ O que está sendo entregue nesta fase:

API RESTful Contract-First: Implementação dos controllers baseada na especificação OpenAPI 3.1, garantindo contratos rigorosos.

Mensageria com Kafka (KRaft): Disparo de eventos de domínio (TemplateCreated, NotificationDispatched, etc.) para integração assíncrona.

Estratégia de CQRS: Separação entre o fluxo de escrita (Comandos) e o fluxo de leitura (Projeções/Views), otimizando consultas de auditoria.

Cache de Alta Performance (Caffeine): Redução drástica de latência em execuções de templates publicados, minimizando acessos ao MongoDB.

Mapeamento Profissional (MapStruct): Desacoplamento total entre as entidades de domínio e os DTOs de entrada/saída.

Validação Clínica (Bean Validation): Garantia de integridade dos dados na borda da aplicação (Fail-Fast).

🧱 Decisões Técnicas & Trade-offs:

Event-Driven Architecture: O uso de eventos permite que sistemas externos reajam a notificações sem onerar o fluxo principal de renderização.

Idempotência de Consumo: Os consumers do Kafka foram projetados para serem idempotentes, evitando duplicidade de registros de auditoria em casos de reentrega de mensagens.

Invalidação de Cache: Implementada a estratégia de CacheEvict no momento da publicação de novas versões, garantindo que o motor nunca utilize templates obsoletos.

🚀 Como validar:

Testcontainers: Os testes de integração validam o fluxo completo, desde a API até a persistência e o disparo de mensagens no Kafka.

Swagger UI: Disponível em http://localhost:8080/swagger-ui.html para testes manuais dos endpoints.

Kafdrop: Monitore os tópicos e mensagens em tempo real via http://localhost:9000.

🛠️ Troubleshooting (Resolução de Problemas)

Erro de Conexão com Docker (Testcontainers)

Caso ocorra um IllegalStateException ou MongoTimeoutException durante os testes:

Certifique-se de que o Docker Desktop está em execução.

Verifique se o comando docker ps funciona no seu terminal.

No Windows, garanta que a opção "Expose daemon on tcp://localhost:2375 without TLS" no Docker Desktop esteja desmarcada (o Testcontainers prefere o npipe padrão) ou, se necessário, configurada corretamente no seu shell.

Importante: Verifique se a sua classe de teste de integração possui a anotação @Import(TestcontainersConfiguration.class). Sem isso, o Spring não saberá como se conectar aos containers dinâmicos criados para o teste.

Utilize o terminal integrado do IntelliJ para garantir que as variáveis de ambiente do SDK sejam carregadas corretamente.