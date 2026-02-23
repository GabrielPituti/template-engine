package com.vaas.templateengine.infrastructure.persistence;

import com.vaas.templateengine.domain.model.NotificationExecution;
import com.vaas.templateengine.domain.port.NotificationExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

/**
 * Adaptador de infraestrutura para persistência de logs de execução.
 * Implementa o contrato definido na camada de domínio, isolando a tecnologia
 * de persistência (MongoDB) das regras de negócio.
 */
@Component
@RequiredArgsConstructor
public class ExecutionRepositoryAdapter implements NotificationExecutionRepository {

    private final SpringDataMongoExecutionRepository repository;

    @Override
    public NotificationExecution save(NotificationExecution execution) {
        return repository.save(execution);
    }
}

@Repository
interface SpringDataMongoExecutionRepository extends MongoRepository<NotificationExecution, String> {}