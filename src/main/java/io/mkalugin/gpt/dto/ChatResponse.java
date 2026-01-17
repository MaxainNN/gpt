package io.mkalugin.gpt.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO для ответа от чат-бота.
 *
 * <p>Содержит сгенерированный ответ от GPT модели.
 * Используется как для простого чата, так и для RAG запросов.</p>
 *
 * @param response текст ответа от модели
 * @param conversationId идентификатор разговора для продолжения диалога
 */
@Schema(description = "Ответ от чат-бота")
public record ChatResponse(
        @Schema(description = "Текст ответа от GPT модели", example = "Привет! Я — AI-ассистент. Чем могу помочь?")
        String response,

        @Schema(description = "Идентификатор разговора для продолжения диалога",
                example = "550e8400-e29b-41d4-a716-446655440000", nullable = true)
        String conversationId
) {
}
