Notification Template Engine - Fase 4: Lógica de Negócio e Serviços

Nesta fase, implementamos a "inteligência" do sistema, traduzindo os Requisitos Funcionais (RF01 e RF02) em serviços de aplicação robustos e regras de negócio claras.

🛠️ O que foi entregue nesta fase:

TemplateService: O orquestrador central que gere o ciclo de vida dos templates, desde a criação do rascunho (DRAFT) até à publicação oficial.

RenderEngine (Motor de Renderização): Implementação de alto desempenho utilizando Regex e StringBuilder para substituição de placeholders {{variable}}.

Tratamento de Exceções: Implementação da BusinessException para garantir que erros de regra de negócio sejam capturados e retornados de forma padronizada.

Testes Unitários (Mockito & JUnit 5): Cobertura completa das regras de serviço e dos cenários de renderização (sucesso, variáveis ausentes e conteúdos nulos).

🧱 Padrões e Detalhes Técnicos (Nível Sênior):

Imutabilidade: Garantia de que versões PUBLISHED não podem ser alteradas.

Performance de Texto: Uso de StringBuilder e Matcher.quoteReplacement no motor de renderização para evitar overhead de memória e erros de caracteres especiais.

Tratamento Clínico: Erros específicos como MISSING_REQUIRED_VARIABLE em vez de erros genéricos de processamento.

Atomicidade: Uso de @Transactional para garantir consistência entre Template e Versão.

🚀 Como validar esta branch:

Testes Unitários: Execute ./gradlew test.

TemplateServiceTest: Valida estados e imutabilidade.

RenderEngineTest: Valida a substituição de placeholders.

Compilação: ./gradlew classes.

⏳ Próximos Passos:

Schema Validator: Implementar a validação que garante que o tipo da variável (NUMBER, DATE, etc.) enviado no JSON condiz com o definido no inputSchema.

Template Versioning: Lógica para criar automaticamente novas versões (Patch/Minor/Major).