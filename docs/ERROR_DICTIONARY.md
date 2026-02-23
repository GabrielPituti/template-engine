Dicionário de Erros da API
Notification Template Engine

Referência de todos os códigos de erro de negócio retornados pelo sistema.
O objetivo é agilizar o diagnóstico e facilitar a integração de consumidores.

Todos os erros seguem o formato abaixo:

    {
      "timestamp": "2026-02-22T17:30:00Z",
      "code": "TEMPLATE_NOT_FOUND",
      "message": "Template não encontrado: abc-123"
    }

-------------------------------------------------------------------------------

TEMPLATE_NOT_FOUND
  HTTP: 400
  Causa: o identificador informado no path não existe na base de dados.
  Resolução: verifique se o templateId está correto. Use GET /v1/templates
  para listar os IDs disponíveis.

-------------------------------------------------------------------------------

TEMPLATE_ARCHIVED
  HTTP: 400
  Causa: o template está em estado terminal e não permite execuções,
  novas versões ou edições de qualquer tipo.
  Resolução: utilize um template com status ACTIVE ou crie um novo.

-------------------------------------------------------------------------------

VERSION_NOT_FOUND
  HTTP: 400
  Causa: o versionId informado não existe no histórico do template.
  Resolução: use GET /v1/templates/{id}/versions para obter os IDs válidos.

-------------------------------------------------------------------------------

VERSION_IMMUTABLE
  HTTP: 400
  Causa: tentativa de editar uma versão que já foi publicada. Versões
  PUBLISHED são imutáveis por design para preservar a integridade do
  histórico de disparos.
  Resolução: crie um novo rascunho via POST /v1/templates/{id}/versions
  para realizar as alterações necessárias.

-------------------------------------------------------------------------------

VERSION_ALREADY_PUBLISHED
  HTTP: 400
  Causa: tentativa de publicar uma versão que já está no estado PUBLISHED.
  Resolução: nenhuma ação necessária, a versão já está pronta para uso.

-------------------------------------------------------------------------------

VERSION_NOT_PUBLISHED
  HTTP: 400
  Causa: tentativa de executar um template apontando para uma versão que
  ainda está em estado DRAFT.
  Resolução: publique a versão via POST /v1/templates/{id}/versions/{versionId}/publish
  antes de utilizá-la em execuções.

-------------------------------------------------------------------------------

NO_PUBLISHED_VERSION
  HTTP: 400
  Causa: o template não possui nenhuma versão publicada. O fallback
  automático (quando templateVersionId não é informado) não encontrou
  candidatos.
  Resolução: publique ao menos uma versão do template antes de executar
  sem informar o templateVersionId.

-------------------------------------------------------------------------------

MISSING_REQUIRED_VARIABLE
  HTTP: 400
  Causa: uma variável marcada como required: true no inputSchema não foi
  fornecida no payload de execução.
  Resolução: verifique o inputSchema da versão e inclua todas as chaves
  obrigatórias no objeto variables da requisição.

-------------------------------------------------------------------------------

INVALID_VARIABLE_TYPE
  HTTP: 400
  Causa: o valor fornecido para uma variável não corresponde ao tipo
  declarado no schema (STRING, NUMBER, DATE, BOOLEAN).
  Resolução: ajuste o tipo do dado no JSON de entrada. Valores do tipo
  DATE devem seguir o formato ISO-8601 (ex: 2026-02-22 ou 2026-02-22T17:30:00Z).

-------------------------------------------------------------------------------

TEMPLATE_TOO_LARGE
  HTTP: 400
  Causa: o corpo do template excede o limite de segurança de 50KB.
  Limite aplicado para mitigar ataques de ReDoS via templates extensos.
  Resolução: reduza o tamanho do conteúdo bruto do template.

-------------------------------------------------------------------------------

INVALID_JSON_FORMAT
  HTTP: 400
  Causa: o corpo da requisição não é um JSON válido ou contém valores
  de Enum incorretos, como um canal inexistente (ex: "channel": "FAX").
  Resolução: valide a sintaxe do JSON e os valores de Enum conforme
  o contrato openapi.yaml.

-------------------------------------------------------------------------------

CONCURRENCY_CONFLICT
  HTTP: 409
  Causa: duas atualizações simultâneas foram detectadas no mesmo registro.
  O Optimistic Locking (@Version) identificou uma versão desatualizada.
  Resolução: recarregue os dados mais recentes do template e tente
  a operação novamente.

-------------------------------------------------------------------------------

INTERNAL_SERVER_ERROR
  HTTP: 500
  Causa: erro inesperado no servidor não mapeado por nenhuma regra
  de negócio conhecida.
  Resolução: verifique os logs da aplicação. Registre o campo timestamp
  da resposta para facilitar o diagnóstico.

-------------------------------------------------------------------------------

Para o contexto das decisões de design que originaram esses códigos,
consulte o ADR.md.
