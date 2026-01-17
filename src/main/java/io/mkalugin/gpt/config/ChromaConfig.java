package io.mkalugin.gpt.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chroma.vectorstore.ChromaApi;
import org.springframework.ai.chroma.vectorstore.ChromaVectorStore;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Конфигурация для интеграции с ChromaDB — векторной базой данных.
 *
 * <p>ChromaDB используется для хранения документов с их векторными представлениями
 * (embeddings) и выполнения семантического поиска по ним.</p>
 */
@Slf4j
@Configuration
public class ChromaConfig {

    @Value("${spring.ai.vectorstore.chroma.client.base-url}")
    private String chromaBaseUrl;

    @Value("${spring.ai.vectorstore.chroma.tenant-name}")
    private String tenantName;

    @Value("${spring.ai.vectorstore.chroma.database-name}")
    private String databaseName;

    @Value("${spring.ai.vectorstore.chroma.collection-name}")
    private String collectionName;

    /**
     * Клиент для взаимодействия с ChromaDB API.
     *
     * @return экземпляр {@link ChromaApi}
     */
    @Bean
    public ChromaApi chromaApi() {
        return ChromaApi.builder()
                .baseUrl(chromaBaseUrl)
                .restClientBuilder(RestClient.builder())
                .build();
    }

    /**
     * Векторное хранилище на базе ChromaDB.
     *
     * @param chromaApi      клиент ChromaDB API
     * @param embeddingModel модель для генерации embeddings
     * @return экземпляр {@link ChromaVectorStore}
     */
    @Bean
    public ChromaVectorStore vectorStore(ChromaApi chromaApi, EmbeddingModel embeddingModel) {
        try {
            var request = new ChromaApi.CreateCollectionRequest(collectionName);
            chromaApi.createCollection(tenantName, databaseName, request);
            log.info("Created ChromaDB collection: {}", collectionName);
        } catch (Exception e) {
            log.debug("Collection '{}' already exists or error: {}", collectionName, e.getMessage());
        }

        return ChromaVectorStore.builder(chromaApi, embeddingModel)
                .tenantName(tenantName)
                .databaseName(databaseName)
                .collectionName(collectionName)
                .build();
    }
}
