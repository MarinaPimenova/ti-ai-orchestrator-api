package com.wk.ti.agents.tool.hello;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@SuppressWarnings("unused")
@Component
@RequiredArgsConstructor
@Slf4j
public class HelloTool {
    private static final String HELLO_PROMPT = """
                I'm here to assist you with knowledge resources, making your work faster and more effective.
            
                Here's how I can help:
            
                - [NEWS](%s): I retrieve information about news data.
                - [WEATHER](%s): I search for relevant weather forecasts.
                - Document: I can perform semantic search in the uploaded documents.
                - Natural Disaster: I can search natural disaster information.
            
                Simply ask your question, and I'll locate the most relevant information, summarize key points, or provide a direct answer.
            """;
    private final HelloLinkConfig helloLinkConfig;

    @Tool(description = """
            Use this tool to respond to greetings, small talk, or when the user asks what the assistant can do.
            
            Use this tool ONLY when:
            - The user says hello, hi, hey, good morning, etc.
            - The user asks what you can do
            - The user asks for help or capabilities
            - The user thanks you or says goodbye
            - The request is NOT related to news, weather, documents, or natural disasters
            
            DO NOT use this tool if the user asks for:
            - News
            - Weather
            - Document search
            - Natural disaster information
            
            Input:
            - No input required
            
            Output:
            - A friendly greeting and explanation of available capabilities
            """)
    public String getGreeting() {
        try {
            var resource = new ClassPathResource("hello-info.md");
            // Use getInputStream() instead of getFile() to support reading from JAR
            try (var inputStream = resource.getInputStream()) {
                String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8)
                        .replace("WEATHER_URL", helloLinkConfig.getWeather())
                        .replace("NEWS_URL", helloLinkConfig.getNews());
                return content.replace("\uFEFF", ""); //remove BOM character
            }
        } catch (Exception e) {
            log.error("Failed to read file 'hello-info.md' from resources: {}", e.getMessage(), e);
            return HELLO_PROMPT.formatted(
                    helloLinkConfig.getNews(),
                    helloLinkConfig.getWeather()
            );
        }
    }
}
