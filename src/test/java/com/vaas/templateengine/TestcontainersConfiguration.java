package com.vaas.templateengine;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Configuração central do Testcontainers para o ambiente de testes.
 * Esta classe deve ser pública para permitir o acesso de classes de teste em pacotes de infraestrutura.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    /**
     * Define o container do Kafka utilizando a imagem nativa para maior performance.
     * @return Instância gerenciada do KafkaContainer.
     */
    @Bean
    @ServiceConnection
    public KafkaContainer kafkaContainer() {
        return new KafkaContainer(DockerImageName.parse("apache/kafka-native:latest"));
    }

    /**
     * Define o container do MongoDB para persistência de dados em testes de integração.
     * @return Instância gerenciada do MongoDBContainer.
     */
    @Bean
    @ServiceConnection
    public MongoDBContainer mongoDbContainer() {
        return new MongoDBContainer(DockerImageName.parse("mongo:latest"));
    }
}