Notification Template Engine - Fase 1: Infraestrutura

Nesta etapa inicial, meu foco foi garantir que qualquer pessoa que clonasse
o projeto conseguisse rodar tudo com um único comando, sem configurações
manuais de porta ou endereço. Padronização desde o primeiro commit.

Entregas desta fase

Configurei o ecossistema Java 21 com Gradle (Kotlin DSL), aproveitando as
features mais recentes da linguagem que uso nas fases seguintes.

Orquestrei os serviços via Docker Compose: MongoDB para persistência e Kafka
para mensageria, ambos integrados nativamente ao Spring Boot para o ambiente
de desenvolvimento.

Implementei o pipeline de CI via GitHub Actions para validar builds e rodar
os testes automaticamente em cada push.

Decisões técnicas

Kafka em modo KRaft: escolha deliberada para rodar sem Zookeeper. Simplifica
a topologia, reduz o consumo de memória e segue a direção que a comunidade
Kafka está tomando desde a versão 3.x.

Docker Compose com integração nativa Spring Boot: a aplicação descobre os
containers automaticamente em tempo de desenvolvimento, sem variáveis de
ambiente manuais.

Como validar

Com o Docker Desktop em execução:

  docker-compose up -d

Serviços disponíveis:
  MongoDB   → localhost:27017
  Kafka     → localhost:9092
  Kafdrop   → http://localhost:9000
