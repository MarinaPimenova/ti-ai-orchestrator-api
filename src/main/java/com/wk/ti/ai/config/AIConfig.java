package com.wk.ti.ai.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Validated
public class AIConfig {
    //       api-key: ${OPEN_AI_API_KEY}
    //      base-url: ${OPEN_AI_ENDPOINT:https://ai-proxy.lab.epam.com}
    //      chat:
    //        options:
    //          model: "${CHAT_MODEL:gpt-4.1-mini-2025-04-14}"
    //        completions-path: "${OPEN_AI_COMPLETIONS_PATH:/openai/deployments/gpt-4.1-mini-2025-04-14/chat/completions}"
    //      embedding:
    //        options:
    //          model: "text-embedding-ada-002"
    //        embeddings-path: "${OPEN_AI_EMBEDDINGS_PATH:/openai/deployments/text-embedding-ada-002/embeddings}"
    @NotBlank
    private String apiKey;

    @NotBlank
    private String baseUrl;

    @NotNull
    private Chat chat;

    @NotNull
    private Embedding embedding;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Validated
    public static class Chat {
        @NotBlank
        private String completionsPath;

        @NotNull
        private Options options;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Validated
    public static class Embedding {
        @NotBlank
        private String embeddingsPath;

        @NotNull
        private Options options;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Options {
        @NotBlank
        private String model;

        private Integer maxTokens;

        private Double temperature;

        private Boolean streamUsage;
    }
}