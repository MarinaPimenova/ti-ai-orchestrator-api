package com.wk.ti.orchestrator.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "app.sse.threadpool")
public class SseThreadPoolProperties {
    private int coreSize = 40;
    private int maxSize = 120;
    private int queueCapacity = 200;
}

