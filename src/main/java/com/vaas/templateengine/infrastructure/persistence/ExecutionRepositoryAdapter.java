package com.vaas.templateengine.infrastructure.persistence;

import com.vaas.templateengine.domain.model.NotificationExecution;
import com.vaas.templateengine.domain.port.NotificationExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

/**
 * Adapter para a execução. Como é um log simples, podemos usar uma interface interna.
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

// Interface auxiliar para o Spring Data
@Repository
interface SpringDataMongoExecutionRepository extends MongoRepository<NotificationExecution, String> {}