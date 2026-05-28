package com.campus.secondhand.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ai.deepseek")
@Data
public class DeepSeekProperties {
    private String apiKey;
    private String baseUrl;
    private String model;
}
