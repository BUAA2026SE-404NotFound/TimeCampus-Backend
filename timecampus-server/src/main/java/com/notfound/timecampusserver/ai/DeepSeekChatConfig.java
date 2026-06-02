package com.notfound.timecampusserver.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class DeepSeekChatConfig {

    @Bean
    @ConditionalOnMissingBean(TimeCampusChatGenerator.class)
    @ConditionalOnProperty(prefix = "timecampus.ai.deepseek.chat", name = "enabled", havingValue = "true")
    public TimeCampusChatGenerator deepSeekChatGenerator(DeepSeekChatProperties properties,
                                                         RestClient.Builder restClientBuilder) {
        return new DeepSeekChatGenerator(properties, restClientBuilder);
    }
}
