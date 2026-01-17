package io.mkalugin.gpt.controller;

import io.mkalugin.gpt.dto.ChatResponse;
import io.mkalugin.gpt.dto.DocumentListResponse;
import io.mkalugin.gpt.dto.LoadDocumentsResponse;
import io.mkalugin.gpt.dto.RagRequest;
import io.mkalugin.gpt.service.DocumentService;
import io.mkalugin.gpt.service.RagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * REST контроллер для работы с RAG системой.
 */
@RestController
@RequestMapping("/api/rag")
@RequiredArgsConstructor
@Tag(name = "RAG", description = "API для работы с RAG (Retrieval-Augmented Generation) системой")
public class RagController {

    private final DocumentService documentService;
    private final RagService ragService;

    /**
     * Загрузка документов из ресурсов в векторное хранилище.
     *
     * @param pattern glob-паттерн для поиска файлов
     * @return информация о количестве загруженных чанков
     * @throws IOException если произошла ошибка при чтении файлов
     */
    @Operation(
            summary = "Загрузить документы",
            description = "Загружает документы из ресурсов приложения в векторное хранилище"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Документы успешно загружены"),
            @ApiResponse(responseCode = "500", description = "Ошибка при чтении или обработке файлов")
    })
    @PostMapping("/load")
    public LoadDocumentsResponse loadDocuments(
            @Parameter(description = "Glob-паттерн для поиска файлов в ресурсах", example = "documents/*.txt")
            @RequestParam(defaultValue = "documents/*.txt") String pattern) throws IOException {
        int chunks = documentService.loadDocumentsFromResources(pattern);
        return new LoadDocumentsResponse(chunks, "Documents loaded successfully");
    }

    /**
     * Получение списка документов из векторного хранилища.
     *
     * @param limit максимальное количество документов
     * @return список документов с метаданными
     */
    @Operation(
            summary = "Получить список документов",
            description = "Возвращает список документов, загруженных в векторное хранилище ChromaDB"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список документов успешно получен"),
            @ApiResponse(responseCode = "500", description = "Ошибка при получении данных из ChromaDB")
    })
    @GetMapping("/documents")
    public DocumentListResponse getDocuments(
            @Parameter(description = "Максимальное количество документов для возврата", example = "100")
            @RequestParam(defaultValue = "100") int limit) {
        return documentService.getDocuments(limit);
    }

    /**
     * Выполнение RAG-запроса: поиск релевантных документов и генерирация ответа.
     *
     * @param request запрос с вопросом пользователя
     * @return ответ, сгенерированный на основе найденного контекста
     */
    @Operation(
            summary = "Выполнить RAG-запрос",
            description = "Ищет релевантные документы в векторном хранилище и генерирует ответ на основе найденного контекста"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешный ответ на основе контекста"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос (пустой вопрос или превышен лимит символов)")
    })
    @PostMapping("/query")
    public ChatResponse query(@Valid @RequestBody RagRequest request) {
        String response = ragService.query(request.question());
        return new ChatResponse(response, null);
    }
}
