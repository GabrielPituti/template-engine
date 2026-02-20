Notification Template Engine - Fase 1: Infraestrutura

Este marco do projeto foca na configuração do ambiente de desenvolvimento e na garantia de que todas as dependências externas estejam isoladas e funcionais via containers.

🛠️ O que foi entregue nesta fase:

Java 21 & Gradle: Configuração do Toolchain moderno e gerenciamento de dependências.

Docker Compose: Orquestração do MongoDB e Kafka (modo KRaft).

CI/CD Inicial: Configuração do GitHub Actions para validar builds automaticamente em cada push.

🚀 Como validar esta branch:

Certifique-se de que o Docker Desktop está rodando.

Execute: docker-compose up -d

Verifique os serviços:

MongoDB: localhost:27017

Kafka: localhost:9092

Kafdrop (Visualizador Kafka): http://localhost:9000

📝 Decisões Técnicas:

Kafka KRaft: Optamos por não usar Zookeeper para simplificar a infraestrutura e reduzir o consumo de memória.

Spring Boot 3.5 Docker Compose: A aplicação está configurada para reconhecer os containers automaticamente durante o desenvolvimento.