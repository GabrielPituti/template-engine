Notification Template Engine - Fase 2 & 3: Domínio e Persistência

Neste marco, implementei o núcleo da aplicação (Core) seguindo os princípios de Domain-Driven Design (DDD) e Arquitetura Hexagonal. O meu objetivo foi isolar completamente as regras de negócio das tecnologias de banco de dados e mensageria.

Entregas Técnicas

Modelei o Aggregate Root NotificationTemplate e suas entidades relacionadas, garantindo que o estado do template seja alterado exclusivamente através de métodos de domínio.

Implementei Value Objects como SemanticVersion e InputVariable utilizando Java Records, assegurando imutabilidade e clareza semântica.

Defini as interfaces de saída (Ports) no domínio e desenvolvi seus respectivos adaptadores de infraestrutura para o MongoDB.

Integrei o Spring Data MongoDB para a persistência de templates e logs de execução.

Implementei testes de integração utilizando Testcontainers para garantir que a camada de dados se comporte de maneira idêntica ao ambiente de produção.

Padrões de Projeto e Integridade

Soft Delete: Implementei o arquivamento lógico para preservar o histórico e garantir a rastreabilidade exigida para auditoria, sem remover fisicamente os dados.

Optimistic Locking: Utilizei o controle de concorrência via @Version para mitigar riscos de Race Conditions em ambientes distribuídos.

Multi-tenancy: Desenhei a estrutura de dados com isolamento por orgId e workspaceId desde o início, garantindo a segurança e o isolamento entre diferentes inquilinos.
