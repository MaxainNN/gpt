package io.mkalugin.gpt.config;

import io.mkalugin.gpt.filter.ApiKeyAuthFilter;
import io.mkalugin.gpt.filter.RateLimitFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация безопасности приложения.
 * Настраивает фильтры для API Key аутентификации и Rate Limiting.
 */
@Configuration
public class SecurityConfig {

    @Value("${app.security.api-key:}")
    private String apiKey;

    @Value("${app.security.enabled:false}")
    private boolean securityEnabled;

    @Value("${app.rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;

    /**
     * Регистрация фильтра API Key аутентификации.
     */
    @Bean
    public FilterRegistrationBean<ApiKeyAuthFilter> apiKeyAuthFilter() {
        FilterRegistrationBean<ApiKeyAuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new ApiKeyAuthFilter(apiKey, securityEnabled));
        registration.addUrlPatterns("/api/*");
        registration.setOrder(1);
        return registration;
    }

    /**
     * Регистрация фильтра Rate Limiting.
     */
    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilter() {
        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RateLimitFilter(requestsPerMinute));
        registration.addUrlPatterns("/api/*");
        registration.setOrder(2);
        return registration;
    }
}
