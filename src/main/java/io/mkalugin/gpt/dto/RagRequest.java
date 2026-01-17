package io.mkalugin.gpt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO для запроса к RAG системе.
 *
 * <p>Используется для отправки вопросов, на которые система ответит
 * с использованием контекста из загруженных документов.</p>
 *
 * @param question вопрос пользователя (обязательный, от 1 до 5000 символов)
 */
@Schema(description = "Запрос к RAG системе")
public record RagRequest(
        @Schema(description = "Вопрос пользователя для поиска ответа в документах",
                example = "Какие технологии используются в проекте?",
                requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 5000)
        @NotBlank(message = "Вопрос не может быть пустым")
        @Size(max = 5000, message = "Вопрос не может превышать 5000 символов")
        String question
) {
}
