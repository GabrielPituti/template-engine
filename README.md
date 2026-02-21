Notification Template Engine - Fase 4: Lógica de Negócio e Serviços

Nesta fase, implementamos a "inteligência" do sistema, elevando o projeto ao nível de maturidade exigido para sistemas críticos, resilientes e multi-tenant.

🛠️ O que foi entregue nesta fase:

TemplateService: Orquestrador central com suporte a Optimistic Locking para evitar race conditions em ambientes distribuídos.

Motor de Renderização Sênior: Substituição de placeholders {{variable}} com XSS Protection (HTML Escape) automático para o canal de E-mail.

Integridade Temporal ISO-8601: Migração total para OffsetDateTime, garantindo rastreabilidade temporal absoluta independente da localização do servidor.

Persistência Global: Implementação de conversores de fuso horário para compatibilidade total entre Java e MongoDB.

SchemaValidator: Validação clínica de tipos (NUMBER, STRING, BOOLEAN, DATE) e obrigatoriedade antes do processamento.

Testes de Unidade e Integração: Cobertura total das regras de imutabilidade, concorrência e integridade de dados.

🧱 Decisões Técnicas & Trade-offs:

OffsetDateTime vs LocalDateTime: Optamos por OffsetDateTime para eliminar ambiguidades de fuso horário, essencial em sistemas multi-tenant.

Mongo Custom Converters: Como o MongoDB nativo não suporta OffsetDateTime, implementamos WritingConverter e ReadingConverter para manter a precisão dos dados sem perder a compatibilidade com o banco.

Segurança de Canal: O motor de renderização aplica escape de HTML apenas para o canal EMAIL, preservando a integridade de dados brutos para SMS e WEBHOOK.

🚀 Como validar:

Execute ./gradlew test para validar todas as proteções e a integridade da persistência.