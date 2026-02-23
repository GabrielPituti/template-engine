Notification Template Engine - Fase 4: Lógica de Negócio e Serviços

Aqui entrou a inteligência do sistema. Motor de renderização, validação de
schema e versionamento semântico — tudo pensado para falhar rápido e com
mensagem clara.

O que foi construído

Motor de Renderização: implementado com Regex não-gananciosa (.+?) e
StringBuilder para performance. A escolha de Regex não-gananciosa não é
acidente — ela previne ataques de ReDoS onde expressões gananciosas podem
travar o processador em templates maliciosos. Limite de 50KB aplicado.

Versionamento Semântico: criei o Value Object SemanticVersion com métodos
nextPatch() e nextMinor() encapsulando a lógica dentro do domínio. A decisão
de qual incremento aplicar fica com o autor da versão no momento da criação
— consciente de que inferir isso automaticamente exigiria análise de diff de
schema, o que estava fora do escopo.

SchemaValidator: validação clínica de tipos (STRING, NUMBER, BOOLEAN, DATE)
e obrigatoriedade antes da renderização. Falha antecipada com código de erro
específico, não um 500 genérico.

Sanitização XSS: aplicada automaticamente no canal EMAIL via HtmlUtils do
Spring. Canais SMS e WEBHOOK não recebem esse tratamento por não renderizarem
HTML.

Princípio adotado

Mantive a lógica de transição de estado e cálculo de versão dentro dos
próprios agregados e Value Objects. O TemplateService orquestra, não decide.

Como validar

  ./gradlew test
