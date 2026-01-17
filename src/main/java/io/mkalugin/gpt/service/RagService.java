package io.mkalugin.gpt.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
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

    /**
     * Системный промпт для AI модели с инструкциями по использованию контекста.
     */
    private static final String SYSTEM_PROMPT = """
            Ты — ассистент по программированию. Отвечай на вопросы, используя предоставленный контекст.
            Давай точные ответы с примерами кода, когда это уместно.
            Если информации в контексте недостаточно, скажи об этом.

            Контекст:
            {context}
            """;

    /**
     * Количество наиболее релевантных документов для поиска.
     */
    private static final int TOP_K = 5;

    /**
     * Выполнение RAG-запроса: поиск релевантных документов и генерирация ответа.
     *
     * <p>Алгоритм работы:
     * <ol>
     *     <li>Выполняет similarity search по вопросу в VectorStore</li>
     *     <li>Извлекает top-{@value #TOP_K} наиболее похожих документов</li>
     *     <li>Объединяет тексты документов в единый контекст</li>
     *     <li>Передаёт контекст и вопрос в GPT для генерации ответа</li>
     * </ol>
     * </p>
     *
     * @param question вопрос пользователя
     * @return ответ, сгенерированный на основе найденного контекста
     */
    public String query(String question) {
        List<Document> documents = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(TOP_K)
                        .build()
        );

        String context = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));

        return chatClientBuilder.build()
                .prompt()
                .system(s -> s.text(SYSTEM_PROMPT).param("context", context))
                .user(question)
                .call()
                .content();
    }
}
