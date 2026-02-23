Notification Template Engine - Fase 2 & 3: Domínio e Persistência

Neste marco implementei o núcleo real da aplicação. O objetivo central foi
garantir que as regras de negócio não dependessem de nada de infraestrutura
— o domínio precisa ser testável de forma isolada, sem banco, sem Kafka.

O que foi construído

Modelei o Aggregate Root NotificationTemplate garantindo que qualquer
mudança de estado passe obrigatoriamente pelos métodos de domínio. Nenhum
setter público exposto onde não deveria estar.

Implementei os Value Objects SemanticVersion e InputVariable como Java
Records — imutáveis por natureza, sem boilerplate.

Defini as interfaces de saída (Ports) no domínio e seus adaptadores
correspondentes para o MongoDB na camada de infraestrutura.

Adicionei testes de integração com Testcontainers para cobrir os
comportamentos que mocks não conseguem simular com precisão — timezone,
concorrência otimista e queries com filtros dinâmicos.

Decisões de design

Soft Delete: optei pelo arquivamento lógico em vez de remoção física porque
o NotificationExecution referencia o templateId como chave de auditoria.
Remover o template tornaria esse log órfão, quebrando a rastreabilidade de
quem recebeu o quê e quando.

Optimistic Locking via @Version: proteção contra o cenário onde dois
administradores editam o mesmo template simultaneamente. O Spring Data lança
OptimisticLockingFailureException, capturado pelo GlobalExceptionHandler
com HTTP 409.

Multi-tenancy nativo: orgId e workspaceId estão presentes em todas as
queries desde o início. Isolamento de dados por inquilino garantido no
nível de persistência, não na lógica da aplicação.
