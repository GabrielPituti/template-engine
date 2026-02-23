Roadmap de Evolução Técnica e Escalabilidade
Notification Template Engine

Este documento projeta as adaptações arquiteturais necessárias para suportar
o crescimento da plataforma em cenários de alta disponibilidade e escala global.

A implementação atual foi desenhada para demonstrar os padrões corretos de
arquitetura dentro do escopo do desafio. Cada decisão de trade-off tem um
caminho natural de evolução descrito abaixo.

-------------------------------------------------------------------------------

1. Persistência — de standalone para distribuído

O MongoDB opera hoje em modo standalone, o que significa que o @Transactional
documenta a intenção arquitetural mas não garante rollback real em caso de
falhas parciais envolvendo múltiplos documentos.

O primeiro passo para produção seria migrar para um Replica Set. Isso habilita
transações ACID multi-documento reais, leitura de réplicas para queries
analíticas e failover automático em caso de queda do nó primário.

Para escala com dezenas de milhares de tenants ativos, o próximo nível seria
o MongoDB Sharding utilizando orgId como chave de partição. Isso garante que
o crescimento de dados de um cliente não impacte a performance dos demais,
já que cada shard atende um subconjunto de organizações de forma isolada.

-------------------------------------------------------------------------------

2. Mensageria — de fire-and-forget para garantia de entrega

A publicação de eventos hoje é feita via kafkaTemplate.send() assíncrono.
Em caso de falha do broker após a gravação no MongoDB, o evento não é entregue
e as estatísticas CQRS ficam desatualizadas sem qualquer alerta ou possibilidade
de reprocessamento.

A solução para isso é o Transactional Outbox Pattern: ao invés de publicar
diretamente no Kafka, o evento é gravado em uma coleção outbox dentro da mesma
operação do MongoDB. Um processo dedicado lê essa coleção e publica no Kafka
com garantia de entrega. A janela de inconsistência entre banco e broker é
eliminada.

Para volumes de milhões de disparos por segundo, o Debezium substituiria o
polling da coleção outbox, capturando mudanças diretamente do oplog do MongoDB
via CDC (Change Data Capture). O particionamento por orgId no Kafka garante
ordenação por tenant e paralelismo real no processamento dos consumidores.

-------------------------------------------------------------------------------

3. Cache — de local para distribuído

O Caffeine funciona bem para instância única, que é o cenário do desafio.
O problema aparece em ambientes com auto-scaling: cada pod mantém seu próprio
cache independente. Uma invalidação em um pod não se propaga para os demais,
criando janelas onde instâncias diferentes servem versões diferentes do mesmo
template.

O Redis resolve isso nativamente como cache distribuído. A invalidação de um
template publicado é propagada instantaneamente para todo o cluster. Em
cenários de cold start de novos pods, o cache warming pré-aqueceria as entradas
de maior acesso antes de o pod entrar no load balancer, evitando picos de
latência no início do ciclo de vida da instância.

-------------------------------------------------------------------------------

4. Resiliência operacional — de try-catch para isolamento completo

O NotificationConsumer hoje usa try-catch estruturado para evitar que poison
pills bloqueiem o processamento. Mensagens que falham são logadas e descartadas.
Isso é suficiente para isolar a falha, mas não permite reprocessamento posterior.

Com Dead Letter Topics (DLT), mensagens que falham após N tentativas são
redirecionadas para um tópico dedicado notification-dispatched.DLT. O time
de operações pode inspecionar, corrigir e reprocessar essas mensagens sem
perda de dados e sem impacto no tópico principal.

O retry com backoff exponencial complementa o DLT: falhas transitórias como
banco instável ou timeout de rede se beneficiam de espera crescente entre
tentativas, evitando sobrecarga em cascata sobre infraestrutura já degradada.

-------------------------------------------------------------------------------

5. Contrato de API — de validação manual para automatizada

O contrato OpenAPI é mantido manualmente hoje. Não há garantia automática de
que evoluções na API não quebrem consumidores existentes em um ecossistema
de microsserviços multi-tenant.

Com Pact, cada microsserviço consumidor publica seu contrato esperado. Em
cada pipeline de CI, o Pact verifica automaticamente se o Notification Template
Engine ainda satisfaz todos os contratos registrados. Breaking changes são
detectados antes do deploy, não em produção.

-------------------------------------------------------------------------------

Resumo de evolução por componente

    Componente      Hoje                  Produção            Escala global
    MongoDB         Standalone            Replica Set         Sharding/orgId
    Kafka           Fire-and-forget       Outbox Pattern      CDC + Debezium
    Cache           Caffeine local        Redis distribuído   Redis + Warming
    Consumer        Try-catch             DLT + Retry         Backoff exponencial
    Contrato API    OpenAPI manual        Pact                Pact + CI Gate
