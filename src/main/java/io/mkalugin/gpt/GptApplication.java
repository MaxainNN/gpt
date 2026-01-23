package io.mkalugin.gpt;

import org.springframework.ai.vectorstore.chroma.autoconfigure.ChromaVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(exclude = ChromaVectorStoreAutoConfiguration.class)
@EnableCaching
public class GptApplication {

    public static void main(String[] args) {
        SpringApplication.run(GptApplication.class, args);
    }
}
