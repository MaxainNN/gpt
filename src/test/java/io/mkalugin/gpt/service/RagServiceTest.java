package io.mkalugin.gpt.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты на {@link RagService}
 */
@ExtendWith(MockitoExtension.class)
class RagServiceTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec callSpec;

    @Mock
    private VectorStore vectorStore;

    @Captor
    private ArgumentCaptor<SearchRequest> searchRequestCaptor;

    @Mock
    private InputValidationService inputValidationService;

    private RagService ragService;

    @BeforeEach
    void setUp() {
        ragService = new RagService(chatClientBuilder, vectorStore, inputValidationService);
    }

    @Test
    @DisplayName("query() должен искать документы и генерировать ответ")
    void query_shouldSearchDocumentsAndGenerateResponse() {
        String question = "What is Spring AI?";
        String expectedResponse = "Spring AI is a framework for AI integration.";
        List<Document> documents = List.of(
                new Document("Spring AI provides integration with various AI models."),
                new Document("It supports OpenAI, Anthropic, and other providers.")
        );

        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(documents);
        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(any(Consumer.class))).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callSpec);
        when(callSpec.content()).thenReturn(expectedResponse);

        String result = ragService.query(question);

        assertThat(result).isEqualTo(expectedResponse);
        verify(vectorStore).similaritySearch(searchRequestCaptor.capture());
        SearchRequest capturedRequest = searchRequestCaptor.getValue();
        assertThat(capturedRequest.getQuery()).isEqualTo(question);
        assertThat(capturedRequest.getTopK()).isEqualTo(5);
    }

    @Test
    @DisplayName("query() должен корректно обрабатывать пустой результат поиска")
    void query_shouldHandleEmptySearchResults() {
        String question = "Unknown topic";
        String expectedResponse = "No relevant information found.";

        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(Collections.emptyList());
        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(any(Consumer.class))).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callSpec);
        when(callSpec.content()).thenReturn(expectedResponse);

        String result = ragService.query(question);

        assertThat(result).isEqualTo(expectedResponse);
        verify(vectorStore).similaritySearch(any(SearchRequest.class));
    }
}
