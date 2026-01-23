package io.mkalugin.gpt.service;

import io.mkalugin.gpt.client.ChromaDbClient;
import io.mkalugin.gpt.dto.DocumentInfo;
import io.mkalugin.gpt.dto.DocumentListResponse;
import io.mkalugin.gpt.dto.chroma.ChromaCollection;
import io.mkalugin.gpt.dto.chroma.ChromaGetResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Сервис для загрузки документов в векторное хранилище.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private static final int CONTENT_PREVIEW_LENGTH = 500;

    private final VectorStore vectorStore;
    private final ChromaDbClient chromaDbClient;

    /**
     * Загрузка документов из ресурсов по указанному паттерну.
     *
     * @param pattern glob-паттерн для поиска файлов
     * @return количество загруженных чанков
     * @throws IOException если произошла ошибка при чтении файлов
     */
    public int loadDocumentsFromResources(String pattern) throws IOException {
        Resource[] resources = new PathMatchingResourcePatternResolver()
                .getResources("classpath:" + pattern);

        List<Document> allDocuments = readAndSplitDocuments(resources);

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
        return chromaDbClient.getCollection()
                .map(collection -> fetchDocuments(collection, limit))
                .orElse(emptyResponse());
    }

    private List<Document> readAndSplitDocuments(Resource[] resources) {
        List<Document> allDocuments = new ArrayList<>();
        TokenTextSplitter splitter = new TokenTextSplitter();

        for (Resource resource : resources) {
            log.info("Loading document: {}", resource.getFilename());
            List<Document> documents = new TextReader(resource).get();
            allDocuments.addAll(splitter.apply(documents));
        }

        return allDocuments;
    }

    private DocumentListResponse fetchDocuments(ChromaCollection collection, int limit) {
        return chromaDbClient.getDocuments(collection.id(), limit)
                .map(response -> buildResponse(collection, response))
                .orElse(emptyResponse());
    }

    private DocumentListResponse buildResponse(ChromaCollection collection, ChromaGetResponse response) {
        List<DocumentInfo> documents = new ArrayList<>();

        for (int i = 0; i < response.ids().size(); i++) {
            documents.add(new DocumentInfo(
                    response.ids().get(i),
                    truncate(response.getDocument(i)),
                    response.getMetadata(i)
            ));
        }

        return new DocumentListResponse(
                documents,
                collection.getCountOrZero(),
                chromaDbClient.getCollectionName()
        );
    }

    private DocumentListResponse emptyResponse() {
        return new DocumentListResponse(
                Collections.emptyList(),
                0,
                chromaDbClient.getCollectionName()
        );
    }

    private String truncate(String content) {
        if (content == null || content.length() <= CONTENT_PREVIEW_LENGTH) {
            return content;
        }
        return content.substring(0, CONTENT_PREVIEW_LENGTH) + "...";
    }
}
