Notification Template Engine - Fase 4: Lógica de Negócio e Serviços

Nesta fase, implementamos a "inteligência" do sistema, traduzindo os Requisitos Funcionais (RF01 e RF02) em serviços de aplicação robustos e regras de negócio claras.

🛠️ O que foi entregue nesta fase:

TemplateService: Orquestrador central que integra criação, publicação e execução de templates.

Versionamento Semântico Automático: Motor que detecta mudanças no corpo (Patch) ou no schema (Minor) e gera automaticamente a próxima versão estável.

RenderEngine: Motor de substituição de placeholders {{var}} utilizando Regex e StringBuilder para alta performance.

SchemaValidator: Validação clínica de tipos (NUMBER, STRING, BOOLEAN, DATE) e obrigatoriedade de campos.

Tratamento de Exceções: Implementação da BusinessException com códigos de erro semânticos.

Testes Unitários: Cobertura de 100% dos cenários críticos de negócio, garantindo que rascunhos são mutáveis e versões publicadas são imutáveis.

🧱 Padrões Sênior Aplicados:

Tell, Don't Ask: A lógica de cálculo de versão reside no Value Object SemanticVersion.

Imutabilidade: Proteção rígida contra alteração de versões PUBLISHED.

Auditoria por Padrão: Toda execução (executeTemplate) gera obrigatoriamente um registro em NotificationExecution.

🚀 Como validar esta branch:

Testes: ./gradlew test (Deve passar em menos de 5 segundos).

Tree Check: Verifique se os pacotes service e exception contêm as classes implementadas.

⏳ Próximos Passos (Fase 5):

Exposição REST: Controllers para os comandos e queries.

Kafka Messaging: Disparo de eventos NotificationDispatchedEvent e TemplateVersionPublishedEvent.

CQRS: Implementação de consumers para projeções de auditoria rápida.