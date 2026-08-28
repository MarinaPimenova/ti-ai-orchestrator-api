package com.wk.ti.ai.config.openai;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.wk.ti.ai.config.AIConfig;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.ai.chat.client.ChatClient;

import java.time.Duration;
import java.util.Map;

@Configuration
public class OpenAIApiConfig {

    private final AIConfig aiConfig;

    public OpenAIApiConfig(AIConfig aiConfig) {
        this.aiConfig = aiConfig;
    }

    @Bean
    public OpenAIClient openAIClient() {

        return OpenAIOkHttpClient.builder()
                .apiKey(aiConfig.getOpenai().getApiKey())
                .baseUrl(aiConfig.getOpenai().getBaseUrl())
                .build();
    }

    @Bean
    public OpenAiChatModel chatModel(OpenAIClient openAIClient) {

        AIConfig.Chat chat = aiConfig.getOpenai().getChat();

        OpenAiChatOptions.Builder optionsBuilder = OpenAiChatOptions.builder()
                .model(chat.getModel())
                .apiKey(aiConfig.getOpenai().getApiKey())
                .baseUrl(aiConfig.getOpenai().getBaseUrl())
                .customHeaders(Map.of(
                        "Api-Key", aiConfig.getOpenai().getApiKey(),
                        "cache-control", "no-cache"
                ))
                .maxRetries(2)
                .timeout(Duration.ofSeconds(40))
                .streamUsage(false);

        if (chat.getTemperature() != null) {
            optionsBuilder.temperature(chat.getTemperature());
        }

        return OpenAiChatModel.builder()
                .openAiClient(openAIClient)
                .options(optionsBuilder.build())
                .build();
    }

    @Bean
    public ChatClient openAiChatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }
}