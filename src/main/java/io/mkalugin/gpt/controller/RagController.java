package io.mkalugin.gpt.controller;

import io.mkalugin.gpt.dto.ChatResponse;
import io.mkalugin.gpt.dto.LoadDocumentsResponse;
import io.mkalugin.gpt.dto.RagRequest;
import io.mkalugin.gpt.service.DocumentService;
import io.mkalugin.gpt.service.RagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/rag")
@RequiredArgsConstructor
public class RagController {

    private final DocumentService documentService;
    private final RagService ragService;

    @PostMapping("/load")
    public LoadDocumentsResponse loadDocuments(@RequestParam(defaultValue = "documents/*.txt") String pattern) throws IOException {
        int chunks = documentService.loadDocumentsFromResources(pattern);
        return new LoadDocumentsResponse(chunks, "Documents loaded successfully");
    }

    @PostMapping("/query")
    public ChatResponse query(@RequestBody RagRequest request) {
        String response = ragService.query(request.question());
        return new ChatResponse(response);
    }
}
