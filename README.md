Notification Template Engine - Fase 6: Observabilidade e Revisão Final

Nesta etapa fechei as pontas: métricas operacionais, encapsulamento completo
do domínio e resiliência no consumer Kafka.

O que foi feito

Observabilidade com Micrometer: adicionei o contador
notifications.execution.total com tags de channel, status e orgId. A escolha
por métricas multidimensionais em vez de logs simples permite que o time de
SRE filtre por inquilino ou canal diretamente no Grafana, sem queries
custosas no banco. Disponível em:
http://localhost:8080/actuator/metrics/notifications.execution.total

Encapsulamento do Aggregate Root: removi os setters públicos desnecessários
e adicionei o método updateInformation() com validação de estado. Qualquer
mudança no template agora passa pelas invariantes de negócio — incluindo a
trava que bloqueia edições em templates arquivados.

Imutabilidade de versões publicadas: o método updateContent() na
TemplateVersion lança VERSION_IMMUTABLE se a versão já estiver publicada.
Versão publicada é registro histórico — não pode ser alterada.

Resiliência no Consumer: o NotificationConsumer tem try-catch estruturado
isolando falhas de projeção. Se o banco de estatísticas estiver instável, o
consumo do tópico principal continua sem interrupção.

Sobre as decisões de design

Preferi Micrometer a logs para métricas porque logs exigem parsing posterior
para agregar dados — métricas com tags já chegam prontas para dashboards.

Fechar o agregado contra mutações externas garante que o disparo de eventos
Kafka ocorra sempre junto da mudança de estado, nunca desacoplado.

Garantia de qualidade

Pirâmide de testes aplicada: unitários com Mockito para velocidade e
cobertura de regras de negócio; Testcontainers apenas para persistência e
concorrência, onde o mock seria impreciso.