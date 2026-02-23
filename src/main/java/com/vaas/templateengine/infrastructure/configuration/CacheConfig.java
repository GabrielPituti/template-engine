package com.vaas.templateengine.infrastructure.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuração de cache local em memória.
 * Otimiza a performance de leitura para templates estáveis (PUBLISHED), reduzindo a latência
 * e a carga sobre o MongoDB em cenários de disparos massivos de notificações.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Instancia o gerenciador de cache com políticas de retenção baseadas em tempo e tamanho.
     * A estratégia de 'expireAfterWrite' garante que atualizações sejam propagadas após o TTL definido.
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("templates");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(500)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats());
        return cacheManager;
    }
}