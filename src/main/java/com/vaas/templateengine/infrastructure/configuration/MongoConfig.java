package com.vaas.templateengine.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Configuração customizada para o MongoDB.
 * Registra conversores para suportar tipos Java 8+ como OffsetDateTime,
 * garantindo a persistência correta de fusos horários.
 */
@Configuration
public class MongoConfig {

    /**
     * Define conversores customizados para a serialização do Spring Data MongoDB.
     */
    @Bean
    public MongoCustomConversions customConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(new OffsetDateTimeToDateConverter());
        converters.add(new DateToOffsetDateTimeConverter());
        return new MongoCustomConversions(converters);
    }

    /**
     * Converte OffsetDateTime para Date (UTC) para armazenamento no MongoDB.
     */
    static class OffsetDateTimeToDateConverter implements Converter<OffsetDateTime, Date> {
        @Override
        public Date convert(OffsetDateTime source) {
            return Date.from(source.toInstant());
        }
    }

    /**
     * Converte Date (UTC) do MongoDB de volta para OffsetDateTime no sistema.
     */
    static class DateToOffsetDateTimeConverter implements Converter<Date, OffsetDateTime> {
        @Override
        public OffsetDateTime convert(Date source) {
            return source.toInstant().atOffset(ZoneOffset.UTC);
        }
    }
}