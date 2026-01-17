package io.mkalugin.gpt.exception;

import io.swagger.v3.oas.annotations.media.Schema;

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
 * @param details    детали ошибки (для валидации)
 */
@Schema(description = "Ответ об ошибке")
public record ErrorResponse(
        @Schema(description = "Время возникновения ошибки", example = "2024-01-15T10:30:00")
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
    public ErrorResponse(int status, String error, String message, String path) {
        this(LocalDateTime.now(), status, error, message, path, null);
    }

    public ErrorResponse(int status, String error, String message, String path, List<String> details) {
        this(LocalDateTime.now(), status, error, message, path, details);
    }
}
