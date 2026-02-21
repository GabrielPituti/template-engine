Notification Template Engine - Fase 5: API, Mensageria e Diferenciais Sênior

Esta branch consolida a transformação do motor de templates em um serviço distribuído, resiliente e de alta performance, integrando a camada de exposição REST com eventos assíncronos e a separação de responsabilidades via CQRS.

🛠️ Implementações Consolidadas (Fevereiro 2026)

API Contract-First com OpenAPI 3.1: Documentação técnica rigorosa disponível via Swagger UI, permitindo testes funcionais imediatos dos contratos.

Busca Paginada com Filtros Dinâmicos: Implementação do GET /v1/templates com suporte a paginação e filtros opcionais por channel e status, utilizando queries otimizadas no MongoDB.

Padrão CQRS (Read Model Projections): Separação entre o fluxo de escrita e leitura. Um Kafka Consumer processa eventos de despacho e atualiza uma View de Estatísticas (TemplateStatsView) de forma assíncrona.

Performance com Caffeine Cache: Camada de cache local para templates publicados, garantindo latência mínima no motor de renderização.

Segurança Avançada: Blindagem do RenderEngine contra ataques de ReDoS e sanitização automática de HTML (XSS Protection) para o canal de e-mail.

Mapeamento com MapStruct: Desacoplamento total entre as entidades de domínio e os DTOs de API, suportando inclusive Value Objects complexos (SemanticVersion).

🧱 Decisões Técnicas e Defesa

Por que CQRS para Estatísticas?

Argumento: Em sistemas de alta volumetria, contar registros em uma tabela de logs de milhões de linhas é proibitivo. A projeção de leitura permite que o endpoint de /stats responda em tempo constante ($O(1)$).

Por que Swagger com Static OpenAPI?

Argumento: Garante que o código siga fielmente o contrato desenhado (Contract-First), facilitando a integração com times de Frontend e outros microsserviços.

🚀 Como Validar

1. Subir a Infraestrutura

docker-compose up -d


2. Executar a Aplicação

./gradlew bootRun


3. Acessar Documentação e Monitoramento

Swagger UI: http://localhost:8080/swagger-ui.html

Kafdrop: http://localhost:9000 (Verifique os tópicos de eventos).

Actuator Health: http://localhost:8080/actuator/health

4. Testes de Integração

./gradlew test


Status Final da Fase 5: 100% Concluído 🟢 | Build Successful ✅