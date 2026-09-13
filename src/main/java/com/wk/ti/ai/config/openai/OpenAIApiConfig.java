package com.wk.ti.ai.config.openai;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.wk.ti.ai.config.AIConfig;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class OpenAIApiConfig {

    private final OpenAIConfig aiConfig;

    public OpenAIApiConfig(@Qualifier("openAIConfig") OpenAIConfig aiConfig) {
        this.aiConfig = aiConfig;
    }

    @Bean
    public OpenAIClient openAiClient() {
        return OpenAIOkHttpClient.builder()
                .baseUrl(aiConfig.getBaseUrl())
                .apiKey(aiConfig.getApiKey())
                .putHeader("Api-Key", aiConfig.getApiKey())
                .putHeader("Cache-Control", "no-cache")
                .build();
    }

    @Bean
    public ChatModel openChatModel(OpenAIClient openAiClient) {
        AIConfig.Chat chat = aiConfig.getChat();
        AIConfig.Options opts = chat.getOptions();

        String model = (opts != null && opts.getModel() != null && !opts.getModel().isBlank())
                ? opts.getModel()
                : "gpt-4.1-mini-2025-04-14";

        OpenAiChatOptions.Builder optionsBuilder = OpenAiChatOptions.builder()
                .model(model)
                .apiKey(aiConfig.getApiKey())
                .baseUrl(aiConfig.getBaseUrl())
                .customHeaders(Map.of(
                        "Api-Key", aiConfig.getApiKey(),
                        "cache-control", "no-cache"
                ));
//                .maxRetries(2)
//                .timeout(Duration.ofSeconds(40))
//                .streamUsage(false);
        if (opts.getTemperature() != null) {
            optionsBuilder.temperature(opts.getTemperature());
        }
        return OpenAiChatModel.builder()
                .openAiClient(openAiClient)
                .options(optionsBuilder.build())
                .build();
    }

    @Bean
    public ChatClient openAiChatClient(ChatModel openChatModel) {
        return ChatClient.builder(openChatModel).build();
    }
}