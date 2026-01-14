package io.mkalugin.gpt.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RagService {

    private final ChatClient.Builder chatClientBuilder;
    private final VectorStore vectorStore;

    private static final String SYSTEM_PROMPT = """
            Ты — ассистент по программированию. Отвечай на вопросы, используя предоставленный контекст.
            Давай точные ответы с примерами кода, когда это уместно.
            Если информации в контексте недостаточно, скажи об этом.

            Контекст:
            {context}
            """;

    public String query(String question) {
        List<Document> documents = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(5)
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
