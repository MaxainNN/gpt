package io.mkalugin.gpt.service;

import io.mkalugin.gpt.utils.Constants;
import io.mkalugin.gpt.utils.SystemPrompts;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для выполнения RAG запросов.
 */
@Service
@RequiredArgsConstructor
public class RagService {

    private final ChatClient.Builder chatClientBuilder;
    private final VectorStore vectorStore;
    private final InputValidationService validationService;

    /**
     * Выполнение RAG-запроса: поиск релевантных документов и генерация ответа.
     *
     * <p>Алгоритм работы:
     * <ol>
     *     <li>Выполняет similarity search по вопросу в VectorStore</li>
     *     <li>Извлекает top-K наиболее похожих документов</li>
     *     <li>Объединяет тексты документов в единый контекст</li>
     *     <li>Передаёт контекст и вопрос в GPT для генерации ответа</li>
     * </ol>
     * </p>
     *
     * @param question вопрос пользователя
     * @return ответ, сгенерированный на основе найденного контекста
     */
    @Cacheable(value = "ragQueries", key = "#question")
    public String query(String question) {
        // Валидация входящего вопроса
        validationService.validate(question);

        List<Document> documents = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(Constants.RAG_TOP_K)
                        .build()
        );

        String context = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining(Constants.DOCUMENT_SEPARATOR));

        return chatClientBuilder.build()
                .prompt()
                .system(s -> s.text(SystemPrompts.RAG_SYSTEM_PROMPT).param("context", context))
                .user(question)
                .call()
                .content();
    }
}
