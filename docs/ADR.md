Architecture Decision Records (ADR)
Notification Template Engine

Este documento registra as decisões técnicas tomadas durante o desenvolvimento,
explicando o contexto que motivou cada escolha e as consequências para o sistema.

-------------------------------------------------------------------------------

01. Java 21 LTS e Toolchain Moderno

Contexto: precisava de uma base estável com suporte de longo prazo e acesso
a recursos modernos de linguagem.

Decisão: adoção do Java 21 como versão de runtime e compilação.

Consequência: uso de Records para imutabilidade de dados, Sealed Interfaces
para hierarquias de eventos seguras em tempo de compilação e melhorias de
performance na JVM. Virtual Threads disponíveis como caminho natural de
evolução para I/O intensivo.

-------------------------------------------------------------------------------

02. Apache Kafka em modo KRaft

Contexto: necessidade de mensageria assíncrona para desacoplar o fluxo
analítico do fluxo principal de negócio.

Decisão: Kafka 3.7+ sem Zookeeper, operando em modo KRaft.

Consequência: topologia de infraestrutura simplificada, menor consumo de
memória nos containers e alinhamento com a direção oficial da comunidade
Kafka desde a versão 3.x.

-------------------------------------------------------------------------------

03. Arquitetura Hexagonal (Ports & Adapters)

Contexto: o núcleo de negócio não pode depender de tecnologias externas que
mudam com frequência.

Decisão: separação rigorosa entre Domain, Application e Infrastructure, com
comunicação exclusiva via interfaces (Ports).

Consequência: lógica de negócio testável de forma completamente isolada.
Trocar o banco de dados ou o broker de mensagens não exige tocar no domínio.

-------------------------------------------------------------------------------

04. MongoDB como Persistência NoSQL

Contexto: o inputSchema de um template é uma lista de variáveis com estrutura
flexível, e cada template carrega suas versões aninhadas.

Decisão: banco de dados orientado a documentos.

Consequência: recuperação do agregado completo (Template + Versões) em uma
única operação. Evoluções no inputSchema não exigem migrações de schema
relacional.

-------------------------------------------------------------------------------

05. Domain-Driven Design (DDD) — Aggregate Root

Contexto: as regras de versionamento e transições de estado precisavam estar
centralizadas, não espalhadas pela camada de aplicação.

Decisão: NotificationTemplate implementado como raiz de agregado, controlando
todo acesso às versões filhas.

Consequência: invariantes de negócio centralizadas. Versões não podem ser
criadas, editadas ou publicadas fora das regras permitidas pelo estado atual
do template.

-------------------------------------------------------------------------------

06. Integridade Temporal com OffsetDateTime

Contexto: sistema multi-tenant com clientes em fusos horários diferentes e
servidores potencialmente distribuídos.

Decisão: uso obrigatório de OffsetDateTime para todos os campos de data e
hora, com conversor customizado para o MongoDB.

Consequência: rastreabilidade precisa das execuções independente da
localização geográfica do tenant ou da instância que processou a requisição.

-------------------------------------------------------------------------------

07. Value Object para Versionamento Semântico

Contexto: lógica de incremento e comparação de versões Major.Minor.Patch
não deveria estar espalhada no Service.

Decisão: record SemanticVersion com métodos nextPatch() e nextMinor()
encapsulando toda a lógica de versionamento.

Consequência: código do Service mais limpo, seguindo o princípio
"Tell, Don't Ask". A lógica de versão é testável isoladamente.

-------------------------------------------------------------------------------

08. Padrão Builder com Lombok

Contexto: agregados com muitos campos, parte deles opcionais, resultam em
construtores sobrecarregados difíceis de manter.

Decisão: anotação @Builder nas entidades de domínio e DTOs.

Consequência: legibilidade no código de criação de objetos. O @Builder.Default
garante que coleções sejam inicializadas corretamente mesmo com instanciação
parcial.

-------------------------------------------------------------------------------

09. Motor de Renderização Baseado em StringBuilder

Contexto: substituição de placeholders em alta volumetria com String.replace()
encadeado cria instâncias intermediárias desnecessárias, gerando pressão no GC.

Decisão: implementação customizada via Matcher + StringBuilder.

Consequência: otimização do uso de memória Heap ao evitar objetos String
intermediários para cada substituição de placeholder.

-------------------------------------------------------------------------------

10. Mitigação de Vulnerabilidade ReDoS

Contexto: expressões regulares gananciosas podem causar backtracking
exponencial em strings especialmente construídas, travando o processador.

Decisão: Regex não-gananciosa (.+?) com limite rígido de 50KB por template.

Consequência: estabilidade garantida mesmo sob tentativas de exploração via
templates com padrões complexos de placeholder.

-------------------------------------------------------------------------------

11. Proteção contra Cross-Site Scripting (XSS)

Contexto: variáveis dinâmicas fornecidas pelo cliente podem injetar scripts
maliciosos em templates de e-mail.

Decisão: sanitização automática via HtmlUtils.htmlEscape() aplicada
exclusivamente ao canal EMAIL.

Consequência: segurança do destinatário assegurada. Canais SMS e WEBHOOK não
recebem esse tratamento por não renderizarem HTML.

-------------------------------------------------------------------------------

12. Validação Clínica de Schema de Entrada

Contexto: tipos de dados incorretos nas variáveis de execução causam falhas
silenciosas nos sistemas de destino.

Decisão: SchemaValidator dedicado com verificação rigorosa de tipos
(NUMBER, DATE, BOOLEAN, STRING) e obrigatoriedade antes da renderização.

Consequência: falha antecipada com código de erro específico antes de consumir
recursos de renderização. O cliente sabe exatamente o que corrigir.

-------------------------------------------------------------------------------

13. Testes de Integração com Testcontainers

Contexto: mocks divergem do comportamento real em queries com filtros
dinâmicos, concorrência otimista e conversores de tipos.

Decisão: containers Docker reais via Testcontainers para as suítes de teste
de persistência e concorrência.

Consequência: Optimistic Locking, queries com filtros opcionais e conversores
de OffsetDateTime validados contra infraestrutura idêntica à de produção.

-------------------------------------------------------------------------------

14. Hierarquia de Eventos de Domínio Selados (Sealed)

Contexto: novos tipos de evento adicionados sem o tratamento correspondente
causariam falhas silenciosas em runtime.

Decisão: Sealed Interface para DomainEvent com quatro implementações
permitidas: TemplateCreatedEvent, TemplateVersionPublishedEvent,
NotificationDispatchedEvent e TemplateArchivedEvent.

Consequência: exhaustive switch garante que adicionar um novo tipo de evento
sem tratar no NotificationProducer resulte em erro de compilação, não em
falha silenciosa em produção.

-------------------------------------------------------------------------------

15. Padrão CQRS para Estatísticas de Execução

Contexto: consultas analíticas sobre a coleção principal de escrita causariam
degradação de performance em cenários de alta volumetria.

Decisão: separação entre escrita de templates e leitura de estatísticas.
A TemplateStatsView fica em coleção dedicada, alimentada por eventos Kafka.

Consequência: consultas de dashboard em O(1) sem impactar a latência da API
de gestão. A projeção é atualizada de forma assíncrona e tolerante a falhas.

-------------------------------------------------------------------------------

16. Estratégia de Cache Local com Caffeine

Contexto: templates publicados são lidos com alta frequência em disparos
massivos e raramente mudam após publicados.

Decisão: cache em memória com TTL de 10 minutos e limite de 500 entradas,
com invalidação explícita via @CacheEvict em toda operação de escrita.

Consequência: redução drástica de I/O no MongoDB em picos de tráfego.
Trade-off consciente: cache local não é consistente em ambientes multi-pod.
Em produção, Redis substituiria o Caffeine (ver SCALABILITY.md).

-------------------------------------------------------------------------------

17. Isolamento de Camadas via MapStruct

Contexto: expor o modelo interno de domínio diretamente na API acopla o
contrato REST à estrutura interna do banco de dados.

Decisão: MapStruct para conversão explícita entre agregados e DTOs, com
InputVariableDto dedicado para a camada Web.

Consequência: refatorações internas não quebram consumidores externos.
Validações Bean Validation ficam no DTO, não no domínio.

-------------------------------------------------------------------------------

18. Padronização Global de Tratamento de Exceções

Contexto: respostas de erro inconsistentes entre endpoints dificultam
integração e diagnóstico.

Decisão: @RestControllerAdvice com handlers específicos para
BusinessException, OptimisticLockingFailureException,
MethodArgumentNotValidException e HttpMessageNotReadableException.

Consequência: payload de erro estruturado e consistente (timestamp, code,
message) em todos os endpoints. Ver ERROR_DICTIONARY.md para referência
completa dos códigos.

-------------------------------------------------------------------------------

19. Controle de Concorrência Otimista (Optimistic Locking)

Contexto: dois administradores editando o mesmo template simultaneamente
podem sobrescrever mudanças um do outro silenciosamente.

Decisão: anotação @Version do Spring Data MongoDB no campo internalVersion
do agregado.

Consequência: atualização concorrente resulta em HTTP 409 com código
CONCURRENCY_CONFLICT. O cliente recarrega os dados e tenta novamente com
a versão mais recente.

-------------------------------------------------------------------------------

20. Design Multi-tenant Nativo na Camada de Dados

Contexto: dados de organizações distintas compartilham a mesma infraestrutura
e precisam de isolamento lógico garantido.

Decisão: orgId e workspaceId obrigatórios em todas as queries de listagem
e persistências, sem exceção.

Consequência: vazamento de dados entre tenants é impossível por design, não
por convenção. O isolamento é imposto na query, não na lógica da aplicação.

-------------------------------------------------------------------------------

21. Blindagem de Variáveis de Domínio com DTOs

Contexto: o Value Object InputVariable estava sendo exposto diretamente no
contrato REST, acoplando a API ao modelo interno.

Decisão: InputVariableDto específico para a camada Web com mapeamento
bidirecional via TemplateMapper.

Consequência: mudanças na estrutura interna de InputVariable não afetam o
contrato de API. As anotações de validação ficam no DTO, não no domínio.

-------------------------------------------------------------------------------

22. Mutação Controlada de Estado (Imutabilidade Pós-Publicação)

Contexto: alterar um template já utilizado em produção compromete a integridade
dos logs de auditoria de execuções passadas.

Decisão: trava de estado no método updateContent() da TemplateVersion que
lança VERSION_IMMUTABLE se a versão já estiver publicada.

Consequência: versão publicada torna-se registro histórico imutável.
Alterações exigem criação de nova versão draft, preservando o histórico
completo de disparos anteriores.

-------------------------------------------------------------------------------

23. Observabilidade Multidimensional via Micrometer

Contexto: em ambiente multi-tenant, métricas agregadas mascaram problemas
isolados a um cliente ou canal específico.

Decisão: contador notifications.execution.total com tags de channel, status
e orgId via Micrometer, integrado ao Spring Actuator.

Consequência: o time de SRE consegue filtrar por tenant ou canal diretamente
no Grafana. Anomalias isoladas são identificáveis em tempo real sem parsing
manual de logs.

-------------------------------------------------------------------------------

24. Resiliência do Consumidor Kafka (Fault Tolerance)

Contexto: mensagens malformadas (Poison Pills) no tópico
notification-dispatched poderiam bloquear indefinidamente a atualização das
projeções analíticas.

Decisão: try-catch estruturado isolando a atualização da TemplateStatsView,
com log de erro estruturado sem relançamento da exceção.

Consequência: falhas pontuais na camada analítica não interrompem o consumo
do tópico principal. O fluxo crítico de notificações permanece operacional
mesmo com o Read Model temporariamente instável.
