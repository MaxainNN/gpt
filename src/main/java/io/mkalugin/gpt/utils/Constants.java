package io.mkalugin.gpt.utils;

import lombok.experimental.UtilityClass;

/**
 * Общие константы приложения.
 */
@UtilityClass
public class Constants {

    /**
     * Количество наиболее релевантных документов для поиска в RAG.
     */
    public final int RAG_TOP_K = 5;

    /**
     * Разделитель между документами в контексте RAG.
     */
    public final String DOCUMENT_SEPARATOR = "\n\n---\n\n";
}
