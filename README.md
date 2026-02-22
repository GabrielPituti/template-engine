Notification Template Engine - Fase 1: Infraestrutura

Nesta etapa inicial, estabeleci a base tecnológica do projeto para garantir que o ambiente de desenvolvimento fosse padronizado e isolado através de containers. Meu foco principal foi a automação do setup e a garantia de integridade desde o primeiro commit.

Entregas Técnicas

Configurei o ecossistema Java 21 e Gradle, utilizando as funcionalidades mais recentes da linguagem para garantir um código moderno e eficiente.

Realizei a orquestração de serviços via Docker Compose, integrando o MongoDB para persistência e o Kafka para mensageria.

Implementei o pipeline de Integração Contínua (CI) via GitHub Actions, configurado para validar builds e executar testes automaticamente em cada push ou pull request.

Decisões Técnicas e Fundamentação

Kafka em modo KRaft: Optei por utilizar o Kafka sem a dependência do Zookeeper. Essa escolha simplifica a topologia da infraestrutura, reduz o consumo de memória e facilita a manutenção do ambiente, seguindo a tendência atual da comunidade.

Integração Nativa Spring Boot: Configurei a aplicação para reconhecer os containers Docker automaticamente durante o tempo de desenvolvimento, eliminando a necessidade de configurações manuais de portas e endereços no ambiente local.

Como Validar

Certifique-se de que o Docker Desktop está em execução.

Execute o comando: docker-compose up -d

Os serviços estarão disponíveis nos endereços padrão:

MongoDB: localhost:27017

Kafka: localhost:9092

Kafdrop (Visualizador do Kafka): http://localhost:9000
