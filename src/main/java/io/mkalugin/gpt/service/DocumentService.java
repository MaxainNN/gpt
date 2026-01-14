package io.mkalugin.gpt.service;

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
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final VectorStore vectorStore;

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
}
