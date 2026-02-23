Notification Template Engine

Microsserviço multi-tenant para gestão e execução de templates de
notificação em múltiplos canais (E-mail, SMS e Webhook). Construído com
foco em segurança operacional, integridade de dados e observabilidade.

---

Arquitetura

Utilizei Arquitetura Hexagonal (Ports & Adapters) para manter o domínio
isolado de tecnologias externas. A modelagem segue Domain-Driven Design,
com NotificationTemplate como Aggregate Root controlando o ciclo de vida
e a imutabilidade das versões.

---

O que foi implementado

CQRS com projeções: eventos Kafka alimentam uma TemplateStatsView separada,
garantindo consultas analíticas em O(1) sem onerar a coleção principal.

Versionamento semântico: Value Object SemanticVersion com incremento de
Patch e Minor controlado pelo autor da versão.

Segurança no motor de renderização: proteção contra ReDoS via Regex
não-gananciosa, limite de 50KB e sanitização de HTML para o canal EMAIL.

Observabilidade multidimensional: métricas via Micrometer com tags por
channel, status e orgId prontas para Grafana.

Consistência distribuída: Optimistic Locking via @Version para prevenir
lost updates em edições concorrentes.

---

Stack

    Java 21 (Records, Sealed Interfaces, Pattern Matching)
    Spring Boot 3.5.11
    MongoDB + Spring Data
    Kafka (KRaft mode)
    OpenAPI 3.1 / Swagger UI
    JUnit 5 + Testcontainers + MapStruct + Caffeine

---

Como executar

    1. Requisitos: Java 21 e Docker instalados
    2. docker-compose up -d
    3. ./gradlew bootRun
    4. http://localhost:8080/swagger-ui.html

---

Documentação técnica complementar

    docs/ADR.md              → 24 decisões arquiteturais com contexto e trade-offs
    docs/ERROR_DICTIONARY.md → Códigos de erro com causas e resoluções
    docs/SCALABILITY.md      → Evolução técnica para alta disponibilidade
    docs/MONITORING.md       → Estratégias de monitoramento e métricas em produção

![Diagrama de Arquitetura](docs/architecture_diagram.png)

---

Histórico de desenvolvimento

    feat/infrastructure-setup     → Docker, Kafka KRaft, CI/CD
    feat/domain-persistence       → DDD, MongoDB, Testcontainers
    feat/business-logic           → RenderEngine, SchemaValidator, SemanticVersion
    feat/api-messaging-plus       → REST, Kafka, CQRS, Cache, MapStruct
    feat/observability-and-review → Micrometer, encapsulamento DDD, resiliência

---

O que faria diferente com mais tempo

Transactional Outbox Pattern: a publicação no Kafka hoje é fire-and-forget.
Em produção implementaria o Outbox com CDC via Debezium para garantir que
nenhum evento seja perdido em falha parcial.

Cache distribuído com Redis: o Caffeine atual é local por instância. Em
ambiente com múltiplos pods precisaria de Redis para consistência entre
réplicas.

Dead Letter Queue: adicionaria DLT no consumer para isolar poison pills
sem interromper o processamento da fila principal.

Testes de contrato com Pact: para garantir que evoluções na API não quebrem
consumidores em ambiente multi-tenant.

MongoDB Replica Set: para habilitar transações ACID reais multi-documento.

---

Trade-offs conscientes

Fire-and-forget no Kafka: escolha deliberada para maximizar throughput da
API de execução. Em cenário financeiro crítico, usaria confirmação síncrona.

Caffeine local vs Redis: adequado para instância única do desafio; em
produção com auto-scaling o Redis seria mandatório.