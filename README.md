Notification Template Engine

Este projeto é um microsserviço multi-tenant de alto desempenho que desenvolvi para gerenciar e executar templates de notificação através de múltiplos canais como E-mail, SMS e Webhook. A solução foi construída com foco em escalabilidade, segurança e integridade total dos dados.

Visão Geral e Arquitetura

Utilizei a Arquitetura Hexagonal (Ports & Adapters) para garantir que as regras de negócio fossem independentes de tecnologias externas. A modelagem seguiu rigorosamente os padrões de Domain-Driven Design (DDD), tratando o template como um Aggregate Root que protege suas versões e estados internos.

Diferenciais Técnicos que Implementei

Versionamento Semântico: Controle rígido de versões (Major.Minor.Patch) com bloqueio de edição para conteúdos já publicados.

Motor de Renderização Seguro: Proteção nativa contra ataques de ReDoS e injeção de scripts (XSS).

Mensageria e CQRS: Comunicação assíncrona via Kafka para atualização de projeções de leitura, garantindo performance em consultas analíticas.

Observabilidade: Registro de métricas customizadas via Micrometer para monitoramento granular por organização e canal.

Performance: Implementação de cache agressivo de leitura para templates estáveis utilizando Caffeine.

Stack Tecnológica

Linguagem: Java 21 (Records, Sealed Interfaces, Pattern Matching)

Framework: Spring Boot 3.5

Persistência: MongoDB

Mensageria: Kafka (KRaft mode)

Documentação: OpenAPI 3.1 / Swagger UI

Infraestrutura de Testes: Testcontainers e JUnit 5

Como Executar o Projeto

Certifique-se de possuir o Java 21 e o Docker instalados.

Inicie a infraestrutura: docker-compose up -d

Execute a aplicação: ./gradlew bootRun

Acesse a documentação da API em: http://localhost:8080/swagger-ui.html

Estrutura de Desenvolvimento

Organizei a entrega de forma incremental através das seguintes branches:

main: Versão consolidada e pronta para produção.

feat/infrastructure-setup: Base de containers e CI/CD.

feat/domain-persistence: Modelagem e camada de dados.

feat/business-logic: Motor de renderização e versionamento.

feat/api-messaging-plus: REST, Kafka, CQRS e Cache.

feat/observability-and-review: Métricas, refinamento de DDD e ajustes finais de segurança.
