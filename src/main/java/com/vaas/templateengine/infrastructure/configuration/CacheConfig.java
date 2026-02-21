package com.vaas.templateengine.infrastructure.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuração de Cache utilizando Caffeine.
 * Otimiza a performance do sistema evitando consultas repetitivas ao banco de dados
 * para templates que raramente mudam após publicados.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Define o gerenciador de cache com políticas de expiração e tamanho máximo.
     * @return CacheManager configurado.
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("templates");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(500)
                .expireAfterWrite(10, TimeUnit.MINUTES) // Expira após 10 minutos de escrita
                .recordStats()); // Habilita métricas para observabilidade
        return cacheManager;
    }
}