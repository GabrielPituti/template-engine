Guia de Monitoramento Operacional

Este documento descreve como o sistema deve ser monitorado em produção utilizando as métricas multidimensionais implementadas via Micrometer.

1. Métrica Principal: notifications.execution.total

Esta métrica é um contador que registra cada tentativa de execução de template. Graças às tags implementadas, podemos extrair visões granulares:

Consultas Recomendadas (PromQL):

Taxa de Erro por Canal:
sum(rate(notifications_execution_total_total{status="VALIDATION_ERROR"}[5m])) by (channel)

Volume de Disparos por Cliente (Multi-tenancy):
sum(rate(notifications_execution_total_total{status="SUCCESS"}[1h])) by (orgId)

Alertas de Saúde: Se a tag status="ARCHIVED_ERROR" subir subitamente, indica que algum cliente está tentando disparar notificações para templates desativados em massa.

2. Monitoramento de Infraestrutura

A aplicação expõe via Actuator (/actuator/health) o estado de:

MongoDB: Conectividade e latência.

Kafka: Estado dos clusters e tópicos.

Caffeine: Hit Ratio do cache (permite ajustar o maximumSize no CacheConfig).

3. Rastreabilidade

Para cada despacho, o executionId retornado pela API deve ser correlacionado com os logs do Kafka para garantir o fechamento do ciclo de vida da notificação no Read Model.