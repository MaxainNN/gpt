package io.mkalugin.gpt.dto.chroma;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * DTO для ответа на GET запрос документов из ChromaDB.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ChromaGetResponse(
        List<String> ids,
        List<String> documents,
        List<Map<String, Object>> metadatas
) {
    public List<String> ids() {
        return ids != null ? ids : Collections.emptyList();
    }

    public List<String> documents() {
        return documents != null ? documents : Collections.emptyList();
    }

    public List<Map<String, Object>> metadatas() {
        return metadatas != null ? metadatas : Collections.emptyList();
    }

    public String getDocument(int index) {
        return index < documents().size() ? documents().get(index) : null;
    }

    public Map<String, Object> getMetadata(int index) {
        return index < metadatas().size() ? metadatas().get(index) : Collections.emptyMap();
    }
}
