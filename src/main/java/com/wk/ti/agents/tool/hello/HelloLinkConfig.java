package com.wk.ti.agents.tool.hello;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "link.hello")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Validated
public class HelloLinkConfig {
    @NotBlank
    private String questions;

}
