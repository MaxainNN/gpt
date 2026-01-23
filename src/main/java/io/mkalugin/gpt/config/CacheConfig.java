package io.mkalugin.gpt.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Конфигурация кэширования с использованием Caffeine.
 */
@Configuration
public class CacheConfig {

    /**
     * Создает CacheManager с настройками Caffeine.
     * - Максимум 500 записей в кэше
     * - Записи истекают через 10 минут после записи
     * - Статистика кэша включена для мониторинга
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("ragQueries");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats());
        return cacheManager;
    }
}
