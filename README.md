Notification Template Engine - Fase 2 & 3: Domínio e Persistência

Nesta fase, implementamos o "Coração" do sistema seguindo os princípios de Domain-Driven Design (DDD) e Arquitetura Hexagonal.

🛠️ O que foi entregue nesta fase:

Modelo de Domínio: Criação do Aggregate Root NotificationTemplate e entidades filhas.

Value Objects: Uso de Java Records para SemanticVersion e InputVariable, garantindo imutabilidade.

Ports & Adapters: Definição de interfaces de repositório no domínio e implementação técnica na camada de infraestrutura.

MongoDB Integration: Configuração de repositórios Spring Data para persistência dos templates e logs de execução.

🧱 Padrões Utilizados:

Soft Delete: Templates não são removidos fisicamente, mantendo a integridade histórica.

Optimistic Locking: Uso de @Version para evitar que edições simultâneas causem perda de dados.

Multi-tenancy: Todos os modelos de persistência incluem orgId e workspaceId para isolamento lógico de dados.

🚀 Próximos Passos:

Implementação da camada de aplicação (Services) e lógica de versionamento.

Configuração do motor de renderização de placeholders.