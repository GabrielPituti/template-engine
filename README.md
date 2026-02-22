Notification Template Engine - Fase 4: Lógica de Negócio e Serviços

Nesta etapa, desenvolvi a inteligência central da engine, transformando os requisitos funcionais em serviços robustos e regras de negócio testáveis. Foquei em garantir que a renderização de templates fosse segura, precisa e performática.

Entregas Técnicas

TemplateService: Desenvolvi o orquestrador responsável pela gestão do ciclo de vida dos templates, incluindo criação, publicação e preparação para execução.

Motor de Renderização: Implementei um motor focado em performance utilizando Regex não-gananciosa e StringBuilder, mitigando riscos de segurança como ataques de ReDoS.

Segurança e Sanitização: Incluí a proteção automática contra XSS para o canal de e-mail, garantindo que conteúdos dinâmicos não comprometam a segurança do destinatário final.

Validador de Schema: Criei um componente dedicado para validar o payload de variáveis contra o contrato definido na versão do template, suportando tipos como STRING, NUMBER, BOOLEAN e DATE.

Versionamento Automático: Implementei a lógica para incremento de versões semânticas (Patch e Minor) baseada no impacto das alterações no corpo ou no schema.

Defesa Técnica

Princípio Tell, Don't Ask: Mantive a lógica de cálculo de versão e transições de estado dentro dos Value Objects e Agregados, evitando uma arquitetura procedural centrada no serviço.

Proteção de Imutabilidade: Estabeleci travas para impedir a edição de versões que já foram publicadas, assegurando a confiabilidade do histórico de disparos.

Como Validar

A integridade desta fase pode ser confirmada executando os testes unitários que desenvolvi: ./gradlew test. Os testes cobrem desde a substituição de placeholders até casos complexos de validação de tipos e datas.
