package io.mkalugin.gpt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO для запроса к чат-боту.
 *
 * <p>Используется для отправки сообщений в простой чат с GPT
 * через эндпоинт {@code POST /api/chat}.</p>
 *
 * @param message текст сообщения пользователя (обязательный, от 1 до 10000 символов)
 * @param conversationId идентификатор разговора (опциональный, если null - создаётся новый)
 */
@Schema(description = "Запрос к чат-боту")
public record ChatRequest(
        @Schema(description = "Текст сообщения пользователя", example = "Привет! Как дела?",
                requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 10000)
        @NotBlank(message = "Сообщение не может быть пустым")
        @Size(max = 10000, message = "Сообщение не может превышать 10000 символов")
        String message,

        @Schema(description = "Идентификатор разговора для продолжения диалога. Если не указан — создаётся новый разговор",
                example = "550e8400-e29b-41d4-a716-446655440000", nullable = true)
        String conversationId
) {
}
