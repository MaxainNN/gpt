package io.mkalugin.gpt.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

/**
 * DTO с информацией о документе в векторном хранилище.
 *
 * @param id       уникальный идентификатор документа
 * @param content  текстовое содержимое (или его часть)
 * @param metadata метаданные документа
 */
@Schema(description = "Информация о документе в векторном хранилище")
public record DocumentInfo(
        @Schema(description = "Уникальный идентификатор документа", example = "doc-123-abc")
        String id,

        @Schema(description = "Текстовое содержимое документа (может быть обрезано)", example = "Это пример текста документа...")
        String content,

        @Schema(description = "Метаданные документа (источник, дата и т.д.)")
        Map<String, Object> metadata
) {
}
