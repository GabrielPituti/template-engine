Notification Template Engine - Fase 5: API, Mensageria e Diferenciais

Esta fase do projeto consolida a transformação do motor de templates num serviço distribuído, resiliente e de alta performance, estabelecendo as bases para a camada de exposição REST com eventos assíncronos e otimizações de nível sênior.

🛠️ O que foi consolidado nesta etapa:

Segurança Clínica (ReDoS & XSS): O motor de renderização foi blindado com expressões regulares não-gananciosas e limites estritos de tamanho de conteúdo (MAX_CONTENT_LENGTH), além de proteção automática contra injeção de scripts para o canal de e-mail.

Infraestrutura de Cache (Caffeine): Implementação de cache local para templates em estado PUBLISHED, reduzindo drasticamente a latência em cenários de alta volumetria e minimizando o I/O no MongoDB.

Fundação de Mensageria (Domain Events): Definição da hierarquia de eventos de domínio utilizando Sealed Interfaces do Java 21, garantindo que apenas eventos autorizados e tipados sejam disparados para o Kafka.

Mapeamento Profissional (MapStruct): Configuração do motor de mapeamento para assegurar o desacoplamento total entre o Core de Domínio e os DTOs de entrada e saída.

Resiliência nos Testes (Testcontainers): Ajuste de visibilidade e configuração das instâncias dinâmicas de MongoDB e Kafka, garantindo que o build seja 100% reprodutível em qualquer ambiente com Docker.

Tratamento de Concorrência (HTTP 409): Mapeamento global de falhas de Optimistic Locking para respostas semânticas de conflito, orientando o cliente da API sobre race conditions.

🧱 Decisões Técnicas & Trade-offs:

Invalidação de Cache: Optou-se pela estratégia de @CacheEvict sincronizada com o ciclo de vida de publicação de versões, garantindo consistência eventual imediata para o motor de execução.

Backtracking Controlado: A escolha por um motor Regex customizado, em detrimento de bibliotecas pesadas, justifica-se pela economia de memória heap, sendo a segurança garantida pela validação prévia de profundidade e tamanho do template.

Event-Driven Foundation: A estrutura de eventos foi desenhada para suportar o padrão Outbox, assegurando que o estado do banco e o despacho de mensagens permaneçam íntegros.

🚀 Como Validar

Execução dos Testes

Para validar a integridade da persistência, segurança do motor e a infraestrutura de containers, execute no terminal do IntelliJ:

./gradlew test


Monitorização da Infraestrutura

Swagger UI: http://localhost:8080/swagger-ui.html (Em breve com endpoints ativos).

Kafdrop: http://localhost:9000 (Monitoramento de tópicos).

MongoDB Compass: Conectar em mongodb://localhost:27017.

🛠️ Troubleshooting

Falha no Testcontainers

Caso os testes falhem por timeout ou conexão:

Valide se o Docker Desktop está funcional (docker ps).

Utilize o terminal da IDE para garantir que as variáveis de ambiente (JAVA_HOME) estão corretamente mapeadas para o SDK 21.

Status: Build Successful 🟢 | Infraestrutura e Segurança Consolidadas ✅