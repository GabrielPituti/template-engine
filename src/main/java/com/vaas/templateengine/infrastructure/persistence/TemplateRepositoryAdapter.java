package com.vaas.templateengine.infrastructure.persistence;

import com.vaas.templateengine.domain.model.Channel;
import com.vaas.templateengine.domain.model.NotificationTemplate;
import com.vaas.templateengine.domain.model.TemplateStatus;
import com.vaas.templateengine.domain.port.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adapter de infraestrutura para persistência de templates utilizando Spring Data MongoDB.
 * * Esta classe implementa a interface de saída (Port) definida na camada de domínio,
 * garantindo o desacoplamento entre as regras de negócio e a tecnologia de banco de dados.
 */
@Component
@RequiredArgsConstructor
public class TemplateRepositoryAdapter implements NotificationTemplateRepository {

    private final SpringDataMongoTemplateRepository repository;

    /**
     * Persiste ou atualiza um agregado de template no repositório.
     * * @param template Agregado a ser persistido.
     * @return O template com estado atualizado (incluindo ID e versão interna).
     */
    @Override
    public NotificationTemplate save(NotificationTemplate template) {
        return repository.save(template);
    }

    /**
     * Busca um template pelo seu identificador único.
     * * @param id Identificador do template.
     * @return Optional contendo o template se encontrado.
     */
    @Override
    public Optional<NotificationTemplate> findById(String id) {
        return repository.findById(id);
    }

    /**
     * Recupera templates de forma paginada aplicando filtros dinâmicos de busca.
     * Implementa a lógica exigida pelo requisito funcional RF01.
     * * @param orgId Identificador da organização proprietária (Multi-tenancy).
     * @param workspaceId Identificador do workspace (Multi-tenancy).
     * @param channel Filtro opcional por canal de comunicação.
     * @param status Filtro opcional por status do template.
     * @param pageable Configuração de paginação e ordenação.
     * @return Página de templates correspondentes aos critérios.
     */
    @Override
    public Page<NotificationTemplate> findAll(
            String orgId,
            String workspaceId,
            Channel channel,
            TemplateStatus status,
            Pageable pageable) {
        return repository.findByFilters(orgId, workspaceId, channel, status, pageable);
    }

    /**
     * Executa o arquivamento lógico (Soft Delete) do template.
     * * Conforme o RF01, os dados não são removidos fisicamente para preservar o histórico
     * de auditoria. A operação é delegada ao método de domínio do Agregado para garantir
     * a integridade das regras de negócio.
     * * @param id Identificador do template a ser arquivado.
     */
    @Override
    public void deleteById(String id) {
        repository.findById(id).ifPresent(template -> {
            template.archive(); // Delega a alteração de estado para o domínio (DDD)
            repository.save(template);
        });
    }
}