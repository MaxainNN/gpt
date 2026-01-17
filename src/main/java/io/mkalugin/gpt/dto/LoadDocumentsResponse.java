package io.mkalugin.gpt.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO для ответа о результате загрузки документов в векторное хранилище.
 *
 * <p>Возвращается после вызова {@code POST /api/rag/load} и содержит
 * информацию о количестве загруженных чанков и статусе операции.</p>
 *
 * @param chunksLoaded количество загруженных чанков
 * @param message      статусное сообщение о результате операции
 */
@Schema(description = "Результат загрузки документов в векторное хранилище")
public record LoadDocumentsResponse(
        @Schema(description = "Количество загруженных чанков (фрагментов документов)", example = "2")
        int chunksLoaded,

        @Schema(description = "Статусное сообщение о результате операции", example = "Documents loaded successfully")
        String message
) {
}
