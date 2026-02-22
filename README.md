Notification Template Engine - Fase 6: Observabilidade e Auditoria Final

Esta etapa consolida os diferenciais técnicos do projeto, transformando a engine funcional em um serviço pronto para produção, com foco em monitoramento, integridade de dados e princípios de design sênior.

Implementações Técnicas (Diferenciais)

Observabilidade com Micrometer: Integração de métricas customizadas. O sistema agora expõe indicadores de execução segmentados por canal, status da operação e organização. Isso permite a criação de dashboards granulares para monitorar a saúde do serviço em ambientes multi-tenant.

Refatoração de Domínio (DDD): Reestruturação do agregado NotificationTemplate para garantir o encapsulamento. Removi setters públicos desnecessários, forçando que qualquer mudança de estado (como publicação ou arquivamento) ocorra através de métodos de domínio que validam as regras de negócio.

Garantia de Imutabilidade: Implementação de travas lógicas que impedem a alteração de conteúdos em versões já publicadas e bloqueiam a execução de versões em rascunho (DRAFT).

Resiliência em Sistemas Distribuídos: Adição de tratamento de exceções estruturado no consumidor Kafka, mitigando riscos de loops de reprocessamento em caso de falhas na persistência das projeções de leitura (CQRS).

Exclusão Lógica (Soft Delete): Transição completa para arquivamento lógico. O sistema preserva a integridade histórica e a rastreabilidade, cumprindo os requisitos de auditoria do desafio.

Defesa de Arquitetura para Entrevista

1. Estratégia de Métricas Customizadas

Em sistemas de alta volumetria e múltiplos inquilinos, métricas genéricas de infraestrutura (CPU/Memória) são insuficientes. Implementei métricas com tags para que o time de operações consiga identificar se um aumento na taxa de erro é global ou isolado a um canal específico (ex: falhas em Webhooks) ou a um cliente específico.

2. Encapsulamento vs. Modelo Anêmico

Optei por fechar os setters do Aggregate Root para evitar que a lógica de negócio vaze para a camada de aplicação. Ao centralizar as mudanças de estado no próprio agregado, garantimos que eventos colaterais (como o disparo de mensagens para o Kafka) sempre acompanhem a mudança de estado, reduzindo o risco de inconsistência entre o banco de dados e o broker de mensagens.

Validação de Métricas

Os indicadores podem ser consultados via Spring Actuator no endpoint:
http://localhost:8080/actuator/metrics/notifications.execution.total

Status do Projeto: Finalizado com todos os diferenciais obrigatórios e valorizados implementados.