Notification Template Engine (VaaS Challenge)

Microsserviço multi-tenant de alto desempenho para gestão, versionamento e execução de templates de notificação (E-mail, SMS, Webhook).

🎯 Visão Geral

Este projeto foi desenvolvido com foco em missão crítica, utilizando as melhores práticas de engenharia para garantir integridade de dados, performance de renderização e rastreabilidade total de execuções. A solução resolve o desafio técnico de fornecer uma engine flexível para notificações em contextos de alta volumetria.

🏗️ Arquitetura

A solução utiliza Arquitetura Hexagonal (Ports & Adapters) e princípios de Domain-Driven Design (DDD) para isolar o domínio das tecnologias de infraestrutura:

Domínio: Agregados (NotificationTemplate), Entidades (TemplateVersion), Value Objects (SemanticVersion) e Eventos de Domínio selados.

Persistência: MongoDB com suporte a fuso horário absoluto (OffsetDateTime) e controlo de concorrência otimista (@Version).

Execução: Motor de renderização leve baseado em Regex e StringBuilder com proteções contra ReDoS e XSS.

Mensageria: Infraestrutura Kafka (modo KRaft) para comunicação assíncrona e padrões CQRS.

Cache: Caffeine para redução drástica de latência na recuperação de templates publicados.

🚀 Como Executar o Projeto

Pré-requisitos

Java 21 (Amazon Corretto ou Temurin).

Docker Desktop.

Inicialização

Sobe a infraestrutura necessária (MongoDB, Kafka, Kafdrop):

docker-compose up -d


Executa o build completo e os testes de integração (Testcontainers):

./gradlew build


Inicia a aplicação:

./gradlew bootRun


🛠️ Monitoramento e Ferramentas

Swagger UI (Documentação API): http://localhost:8080/swagger-ui.html

Kafdrop (Visualizador Kafka): http://localhost:9000

Actuator Health: http://localhost:8080/actuator/health

🌿 Estrutura de Branches (Roadmap Incremental)

O desenvolvimento seguiu uma evolução lógica e documentada em branches semânticas:

main: Versão estável, documentada e consolidada.

feat/infrastructure-setup: Setup de ambiente, Docker e pipeline CI/CD.

feat/domain-persistence: Modelagem DDD e camada de persistência.

feat/business-logic: Motor de renderização, regras de segurança e versionamento.

feat/api-messaging-plus: Camada de exposição REST, Kafka e diferenciais sênior.

Data de Entrega Final: 24/02/2026