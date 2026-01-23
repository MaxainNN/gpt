package io.mkalugin.gpt.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация приложения.
 */
@Configuration
public class AppConfig {

    private static final String API_KEY_SCHEME = "ApiKeyAuth";

    /**
     * Конфигурация Swagger с поддержкой API Key аутентификации.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("GPT API")
                        .version("1.0.0")
                        .description("API для GPT Assistant project с поддержкой RAG и чата с GPT-4o")
                        .contact(new Contact()
                                .name("Maxim Kalugin")
                                .email("imenolys23@gmail.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .components(new Components()
                        .addSecuritySchemes(API_KEY_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-API-Key")
                                .description("API Key для аутентификации (если включена)")))
                .addSecurityItem(new SecurityRequirement().addList(API_KEY_SCHEME));
    }

    /**
     * Конфигурация памяти чата (InMemory).
     * Хранит последние 20 сообщений в контексте разговора.
     */
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(20)
                .build();
    }
}
