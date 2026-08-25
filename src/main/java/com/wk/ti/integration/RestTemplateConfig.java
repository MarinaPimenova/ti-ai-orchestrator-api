package com.wk.ti.integration;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.TimeValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("deprecation")
@Configuration
public class RestTemplateConfig {

    @Value("${http.pool.maxTotal:200}") // default Tomcat value
    private int maxTotal;

    @Value("${http.pool.defaultMaxPerRoute:100}")
    private int defaultMaxPerRoute;

    @Value("${http.client.connect-timeout:30000}") // default value
    private int connectTimeout;

    @Value("${http.client.read-timeout:120000}") // default value
    private int readTimeout;

    @Bean
    public RestTemplate restTemplate() {
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setMaxTotal(maxTotal);
        connManager.setDefaultMaxPerRoute(defaultMaxPerRoute);

        connManager.setValidateAfterInactivity( TimeValue.ofSeconds(5L)); // validate reused connections 5L

        RequestConfig requestConfig = RequestConfig.custom()
                // the connection timeout — how long to wait to establish a connection
                .setConnectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
                // he read timeout — maximum time waiting for data after the connection established
                .setResponseTimeout(readTimeout, TimeUnit.MILLISECONDS)
                .build();

        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setConnectionManager(connManager)
                .setDefaultRequestConfig(requestConfig)
                // optional: keep-alive strategy, user-agent, redirect strategy...
                .evictExpiredConnections()
                .evictIdleConnections(TimeValue.ofSeconds(120)) // evict idle after 60s
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        // set additional timeouts for RestTemplate as well
        requestFactory.setConnectionRequestTimeout(Duration.ofMillis(connectTimeout));
        //requestFactory.setConnectTimeout(Duration.ofMillis(connectTimeout));
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeout));

        return new RestTemplate(requestFactory);
    }
}