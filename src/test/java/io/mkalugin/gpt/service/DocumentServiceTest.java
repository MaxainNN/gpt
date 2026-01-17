package io.mkalugin.gpt.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Unit-тесты на {@link DocumentService}
 */
@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private VectorStore vectorStore;

    @Captor
    private ArgumentCaptor<List<Document>> documentsCaptor;

    private DocumentService documentService;

    @BeforeEach
    void setUp() {
        documentService = new DocumentService(vectorStore, "http://localhost:8000");
    }

    @Test
    @DisplayName("loadDocumentsFromResources() должен загрузить документы из тестовых ресурсов")
    void loadDocumentsFromResources_shouldLoadDocumentsFromTestResources() throws IOException {
        int result = documentService.loadDocumentsFromResources("test-documents/*.txt");

        assertThat(result).isGreaterThan(0);
        verify(vectorStore).add(documentsCaptor.capture());
        List<Document> capturedDocuments = documentsCaptor.getValue();
        assertThat(capturedDocuments).isNotEmpty();
    }

    @Test
    @DisplayName("loadDocumentsFromResources() должен вернуть 0 если файлы не найдены")
    void loadDocumentsFromResources_shouldReturnZeroWhenNoFilesFound() throws IOException {
        int result = documentService.loadDocumentsFromResources("non-existent-folder/*.txt");

        assertThat(result).isZero();
        verifyNoInteractions(vectorStore);
    }
}
