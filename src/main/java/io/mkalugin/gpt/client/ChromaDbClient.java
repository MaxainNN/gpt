package io.mkalugin.gpt.client;

import io.mkalugin.gpt.dto.chroma.ChromaCollection;
import io.mkalugin.gpt.dto.chroma.ChromaGetResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Клиент для работы с ChromaDB REST API.
 */
@Slf4j
@Component
public class ChromaDbClient {

    private static final String COLLECTION_URL = "/api/v2/tenants/{tenant}/databases/{database}/collections/{collection}";
    private static final String GET_DOCUMENTS_URL = "/api/v2/tenants/{tenant}/databases/{database}/collections/{collectionId}/get";

    private final RestClient restClient;

    @Value("${spring.ai.vectorstore.chroma.tenant-name}")
    private String tenantName;

    @Value("${spring.ai.vectorstore.chroma.database-name}")
    private String databaseName;

    @Value("${spring.ai.vectorstore.chroma.collection-name}")
    private String collectionName;

    public ChromaDbClient(@Value("${spring.ai.vectorstore.chroma.client.base-url}") String chromaBaseUrl) {
        this.restClient = RestClient.builder().baseUrl(chromaBaseUrl).build();
    }

    /**
     * Получение информации о коллекции.
     *
     * @return информация о коллекции или empty если не найдена
     */
    public Optional<ChromaCollection> getCollection() {
        try {
            ChromaCollection collection = restClient.get()
                    .uri(COLLECTION_URL, tenantName, databaseName, collectionName)
                    .retrieve()
                    .body(ChromaCollection.class);
            return Optional.ofNullable(collection);
        } catch (Exception e) {
            log.error("Error fetching collection '{}': {}", collectionName, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Получение документов из коллекции.
     *
     * @param collectionId ID коллекции
     * @param limit        максимальное количество документов
     * @return ответ с документами или empty при ошибке
     */
    public Optional<ChromaGetResponse> getDocuments(String collectionId, int limit) {
        try {
            Map<String, Object> request = Map.of(
                    "limit", limit,
                    "include", List.of("documents", "metadatas")
            );

            ChromaGetResponse response = restClient.post()
                    .uri(GET_DOCUMENTS_URL, tenantName, databaseName, collectionId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(ChromaGetResponse.class);

            return Optional.ofNullable(response);
        } catch (Exception e) {
            log.error("Error fetching documents from collection '{}': {}", collectionId, e.getMessage());
            return Optional.empty();
        }
    }

    public String getCollectionName() {
        return collectionName;
    }
}
