package io.mkalugin.gpt.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO для ответа об ошибке.
 *
 * @param timestamp  время возникновения ошибки
 * @param status     HTTP статус код
 * @param error      название ошибки
 * @param message    описание ошибки
 * @param path       путь запроса
 * @param details    детали ошибки
 */
@Builder
@Schema(description = "Ответ об ошибке")
public record ErrorResponse(

        @Schema(description = "Время возникновения ошибки", example = "2026-01-22T10:30:00")
        LocalDateTime timestamp,

        @Schema(description = "HTTP статус код", example = "400")
        int status,

        @Schema(description = "Название ошибки", example = "Bad Request")
        String error,

        @Schema(description = "Описание ошибки", example = "Validation failed")
        String message,

        @Schema(description = "Путь запроса", example = "/api/chat")
        String path,

        @Schema(description = "Детали ошибки валидации", nullable = true)
        List<String> details
) {
    public ErrorResponse {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
