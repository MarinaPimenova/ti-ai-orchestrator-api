package com.wk.ti.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.wk.ti.ai.logger.interceptor.DetailedLLMLoggingInterceptor;
import com.wk.ti.ai.logger.interceptor.LLMRetryInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.*;

@Configuration
public class AppConfig {
    @Bean
    public Executor agentExecutor() {
        return Executors.newFixedThreadPool(10);
    }

    @Bean(name = "aiAgentExecutor")
    public ExecutorService aiAgentExecutor() {
        int cores = Runtime.getRuntime().availableProcessors();
        int corePool = cores * 2;
        int maxPool = cores * 4;
        int queue = 2000;
        return new ThreadPoolExecutor(corePool, maxPool, 30, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queue),
                runnable -> {
                    Thread t = new Thread(runnable);
                    t.setName("orch-ai-agent-exec-" + t.getId());
                    return t;
                },
                new ThreadPoolExecutor.AbortPolicy()); // Better protection for web frontends
    }


    @Bean
    public ObjectMapper mapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                .configure(JsonParser.Feature.ALLOW_COMMENTS, true);
    }

    // Increase HTTP client read timeout to 120+ seconds
    @Bean
    public SimpleClientHttpRequestFactory simpleClientHttpRequestFactory(
            @Value("${http.client.connect-timeout:30000}") int connectTimeout,
            @Value("${http.client.read-timeout:120000}") int readTimeout) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout); // from property, default 30 seconds
        factory.setReadTimeout(readTimeout);       // from property, default 120 seconds
        return factory;
    }

    @Bean
    public RestTemplate llmRestTemplate(
            SimpleClientHttpRequestFactory simpleClientHttpRequestFactory,
            ObjectMapper objectMapper,
            @Value("${spring.ai.openai.chat.completions-path}") String gptCompletionUrl) {
        RestTemplate restTemplate = new RestTemplate(
                new BufferingClientHttpRequestFactory(simpleClientHttpRequestFactory)
        );

        restTemplate.setInterceptors(
                List.of(
                        new LLMRetryInterceptor(gptCompletionUrl),
                        new DetailedLLMLoggingInterceptor(objectMapper)
                ));

        return restTemplate;
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
        restTemplate.getMessageConverters().add(new ResourceHttpMessageConverter());
        restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());

        return restTemplate;
    }

    // Create a RestClient.Builder that is configured to use your RestTemplate's factory and interceptors
    @Bean
    public RestClient.Builder llmRestClientBuilder(RestTemplate llmRestTemplate) {
        RestClient.Builder builder = RestClient.builder();
        // Set the request factory from your RestTemplate
        builder.requestFactory(llmRestTemplate.getRequestFactory());
        // Explicitly add interceptors from RestTemplate to RestClient.Builder
        if (llmRestTemplate.getInterceptors() != null) {
            for (ClientHttpRequestInterceptor interceptor : llmRestTemplate.getInterceptors()) {
                builder.requestInterceptor(interceptor);
            }
        }
        return builder;
    }

}
