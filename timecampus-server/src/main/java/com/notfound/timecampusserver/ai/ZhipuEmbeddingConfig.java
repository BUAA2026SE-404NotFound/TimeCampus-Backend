package com.notfound.timecampusserver.ai;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ZhipuEmbeddingConfig {

    @Bean
    @ConditionalOnMissingBean(EmbeddingModel.class)
    @ConditionalOnProperty(prefix = "timecampus.ai.zhipu.embedding", name = "enabled", havingValue = "true")
    public EmbeddingModel zhipuEmbeddingModel(ZhipuEmbeddingProperties properties,
                                              RestClient.Builder restClientBuilder) {
        return new ZhipuEmbeddingModel(properties, restClientBuilder);
    }
}
