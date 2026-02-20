Notification Template Engine - Fase 4: Lógica de Negócio e Serviços

Nesta fase, implementamos a "inteligência" do sistema, traduzindo os Requisitos Funcionais (RF01 e RF02) em serviços de aplicação robustos e regras de negócio claras.

🛠️ O que foi entregue nesta fase:

TemplateService: O orquestrador central que gere o ciclo de vida dos templates, desde a criação do rascunho (DRAFT) até à publicação oficial.

Tratamento de Exceções: Implementação da BusinessException para garantir que erros de regra de negócio sejam capturados e retornados de forma padronizada.

Gestão de Estados: Lógica para garantir que apenas versões em rascunho possam ser alteradas ou publicadas.

Versionamento Inicial: Automação da criação da primeira versão (1.0.0) no momento da criação do template.

Testes Unitários (Mockito): Cobertura das regras de negócio do TemplateService garantindo isolamento total da camada de persistência.

🧱 Regras de Negócio Implementadas:

Imutabilidade: Uma vez que uma TemplateVersion é marcada como PUBLISHED, ela não pode mais sofrer alterações (Garantido pela lógica de serviço).

Isolamento de Erros: Utilização de códigos de erro semânticos como TEMPLATE_NOT_FOUND e VERSION_ALREADY_PUBLISHED.

Atomicidade: Utilização da anotação @Transactional para garantir que a criação do template e da sua versão inicial ocorram como uma única operação no MongoDB.

🚀 Como validar esta branch:

Testes Unitários: Execute ./gradlew test para validar as regras de negócio de forma isolada.

Compilação: Certifica-te de que o projeto compila sem erros: ./gradlew classes.

⏳ Próximos Passos:

Implementação do Motor de Renderização (RenderEngine) para processar os placeholders {{variable}}.

Validação de Schema: Garantir que os dados enviados pelo cliente batem com o inputSchema definido no template.