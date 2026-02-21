Notification Template Engine - Fase 5: API, Mensageria e Diferenciais Sênior

Esta fase final consolida a transformação do motor de templates num serviço distribuído, resiliente e de alta performance. O projeto não apenas cumpre os requisitos funcionais, mas implementa diferenciais de engenharia que garantem a segurança e a escalabilidade em ambientes multi-tenant de alta volumetria.

🛠️ O que está sendo entregue (Foco em Excelência)

API RESTful & Mapeamento Profissional: Exposição de endpoints via Spring Web, utilizando MapStruct para garantir que o modelo de banco de dados (Entidades) nunca vaze para o consumidor da API (DTOs).

Segurança ReDoS & XSS: Motor de renderização blindado contra Regular Expression Denial of Service através de limites de tamanho (MAX_CONTENT_LENGTH) e regex não-gananciosa. Proteção ativa contra Cross-Site Scripting no canal de e-mail.

Performance com Caffeine Cache: Implementação de cache em memória para templates publicados. Estratégia de consistência garantida via @CacheEvict em operações de publicação e arquivamento.

Mensageria com Kafka (KRaft): Disparo de eventos de domínio utilizando Sealed Interfaces do Java 21, permitindo uma integração assíncrona e desacoplada para auditoria e projeções CQRS.

Integridade de Dados: Uso de OffsetDateTime para rastreabilidade global e Optimistic Locking (@Version) para prevenir conflitos de escrita (Race Conditions).

🧱 Decisões Técnicas & Trade-offs (Para Defesa em Entrevista)

Por que Motor Regex Customizado?

Argumento: Bibliotecas como Freemarker possuem um overhead de memória significativo. Optamos por uma implementação leve com StringBuilder para reduzir pausas de Garbage Collection em cenários de alta carga.

Por que Sealed Interfaces nos Eventos?

Argumento: Garante segurança de tipos em tempo de compilação e exaustividade no processamento de eventos, seguindo as melhores práticas do Java moderno.

Resiliência do Pipeline (CI/CD):

Argumento: Optamos pelo uso exclusivo de Testcontainers no pipeline de integração contínua, eliminando a necessidade de scripts manuais de Docker Compose no CI e garantindo que os testes sejam agnósticos ao ambiente.

🚀 Como Validar

Execução de Testes de Alta Fidelidade

Utilizamos Testcontainers para validar o fluxo real de persistência e mensageria:

./gradlew test


Monitorização

Swagger UI: http://localhost:8080/swagger-ui.html

Kafdrop: http://localhost:9000 (Visualização de eventos em tempo real).

🛠️ Troubleshooting (Resolução de Problemas)

Erros de Docker/Testcontainers no CI (GitHub Actions)

Caso o teste falhe com ContainerLaunchException ou LogMessageWaitStrategy:

Conflito de Infraestrutura: Verifique se o CI não está tentando subir containers via Docker Compose manualmente. O Testcontainers deve ser o único responsável pela infra durante os testes para evitar contenção de recursos.

Visibilidade da Configuração: Garanta que TestcontainersConfiguration é public para que o Spring consiga injetar as propriedades de conexão dinâmicas corretamente.

Timeout de Inicialização: Em ambientes de CI limitados, imagens "native" podem demorar mais para sinalizar prontidão. A simplificação do pipeline resolve a maioria desses casos.

Status da Branch: Build Successful Local 🟢 | Pipeline CI em Otimização ⚙️