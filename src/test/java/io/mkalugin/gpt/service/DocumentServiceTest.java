package io.mkalugin.gpt.service;

import io.mkalugin.gpt.client.ChromaDbClient;
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Unit-тесты на {@link DocumentService}
 */
@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private VectorStore vectorStore;

    @Mock
    private ChromaDbClient chromaDbClient;

    @Captor
    private ArgumentCaptor<List<Document>> documentsCaptor;

    private DocumentService documentService;

    @BeforeEach
    void setUp() {
        documentService = new DocumentService(vectorStore, chromaDbClient);
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
    @DisplayName("loadDocumentsFromResources() должен выбросить исключение если папка не существует")
    void loadDocumentsFromResources_shouldThrowExceptionWhenFolderNotFound() {
        assertThatThrownBy(() -> documentService.loadDocumentsFromResources("non-existent-folder/*.txt"))
                .isInstanceOf(FileNotFoundException.class);
        verifyNoInteractions(vectorStore);
    }
}
