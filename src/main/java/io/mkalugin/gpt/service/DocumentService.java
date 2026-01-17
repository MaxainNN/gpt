package io.mkalugin.gpt.service;

import io.mkalugin.gpt.dto.DocumentInfo;
import io.mkalugin.gpt.dto.DocumentListResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Сервис для загрузки документов в векторное хранилище.
 */
@Slf4j
@Service
public class DocumentService {

    private final VectorStore vectorStore;
    private final RestClient restClient;

    @Value("${spring.ai.vectorstore.chroma.tenant-name}")
    private String tenantName;

    @Value("${spring.ai.vectorstore.chroma.database-name}")
    private String databaseName;

    @Value("${spring.ai.vectorstore.chroma.collection-name}")
    private String collectionName;

    public DocumentService(VectorStore vectorStore,
                           @Value("${spring.ai.vectorstore.chroma.client.base-url}") String chromaBaseUrl) {
        this.vectorStore = vectorStore;
        this.restClient = RestClient.builder().baseUrl(chromaBaseUrl).build();
    }

    /**
     * Загрузка документов из ресурсов по указанному паттерну.
     *
     * <p>Процесс загрузки:
     * <ol>
     *     <li>Поиск файлов по glob-паттерну в classpath</li>
     *     <li>Чтение содержимого каждого файла</li>
     *     <li>Разбиение на чанки для оптимального поиска</li>
     *     <li>Генерация embeddings и сохранение в VectorStore</li>
     * </ol>
     * </p>
     *
     * @param pattern glob-паттерн для поиска файлов
     * @return количество загруженных чанков
     * @throws IOException если произошла ошибка при чтении файлов
     */
    public int loadDocumentsFromResources(String pattern) throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:" + pattern);

        List<Document> allDocuments = new ArrayList<>();
        TokenTextSplitter splitter = new TokenTextSplitter();

        for (Resource resource : resources) {
            log.info("Loading document: {}", resource.getFilename());
            TextReader reader = new TextReader(resource);
            List<Document> documents = reader.get();
            List<Document> splitDocuments = splitter.apply(documents);
            allDocuments.addAll(splitDocuments);
        }

        if (!allDocuments.isEmpty()) {
            vectorStore.add(allDocuments);
            log.info("Loaded {} document chunks into vector store", allDocuments.size());
        }

        return allDocuments.size();
    }

    /**
     * Получение списка документов из векторного хранилища.
     *
     * @param limit максимальное количество документов для возврата
     * @return список документов с метаданными
     */
    public DocumentListResponse getDocuments(int limit) {
        try {
            // Получаем коллекцию
            String collectionUrl = "/api/v2/tenants/{tenant}/databases/{database}/collections/{collection}";
            Map<String, Object> collection = restClient.get()
                    .uri(collectionUrl, tenantName, databaseName, collectionName)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (collection == null) {
                return new DocumentListResponse(Collections.emptyList(), 0, collectionName);
            }

            String collectionId = (String) collection.get("id");

            // Запрос документов
            String getUrl = "/api/v2/tenants/{tenant}/databases/{database}/collections/{collectionId}/get";
            Map<String, Object> request = Map.of(
                    "limit", limit,
                    "include", List.of("documents", "metadatas")
            );

            Map<String, Object> response = restClient.post()
                    .uri(getUrl, tenantName, databaseName, collectionId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            List<DocumentInfo> documents = new ArrayList<>();
            if (response != null) {
                @SuppressWarnings("unchecked")
                List<String> ids = (List<String>) response.get("ids");
                @SuppressWarnings("unchecked")
                List<String> contents = (List<String>) response.get("documents");
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> metadatas = (List<Map<String, Object>>) response.get("metadatas");

                if (ids != null) {
                    for (int i = 0; i < ids.size(); i++) {
                        String id = ids.get(i);
                        String content = (contents != null && i < contents.size())
                                ? truncateContent(contents.get(i), 500)
                                : null;
                        Map<String, Object> metadata = (metadatas != null && i < metadatas.size())
                                ? metadatas.get(i)
                                : Collections.emptyMap();

                        documents.add(new DocumentInfo(id, content, metadata));
                    }
                }
            }

            int totalCount = collection.get("count") != null
                    ? ((Number) collection.get("count")).intValue()
                    : documents.size();
            return new DocumentListResponse(documents, totalCount, collectionName);

        } catch (Exception e) {
            log.error("Error fetching documents from ChromaDB: {}", e.getMessage());
            return new DocumentListResponse(Collections.emptyList(), 0, collectionName);
        }
    }

    private String truncateContent(String content, int maxLength) {
        if (content == null) return null;
        if (content.length() <= maxLength) return content;
        return content.substring(0, maxLength) + "...";
    }
}
