package io.mkalugin.gpt.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * DTO для ответа со списком документов из векторного хранилища.
 *
 * @param documents    список документов
 * @param totalCount   общее количество документов
 * @param collectionName название коллекции в ChromaDB
 */
@Schema(description = "Список документов в векторном хранилище")
public record DocumentListResponse(
        @Schema(description = "Список документов")
        List<DocumentInfo> documents,

        @Schema(description = "Общее количество документов", example = "42")
        int totalCount,

        @Schema(description = "Название коллекции в ChromaDB", example = "spring-ai-docs")
        String collectionName
) {
}
