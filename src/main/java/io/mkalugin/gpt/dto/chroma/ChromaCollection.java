package io.mkalugin.gpt.dto.chroma;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO для коллекции ChromaDB.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ChromaCollection(
        String id,
        String name,
        Integer count
) {
    public int getCountOrZero() {
        return count != null ? count : 0;
    }
}
