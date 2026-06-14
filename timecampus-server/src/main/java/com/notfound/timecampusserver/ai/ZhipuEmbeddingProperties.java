package com.notfound.timecampusserver.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "timecampus.ai.zhipu.embedding")
public class ZhipuEmbeddingProperties {

    private boolean enabled = false;
    private String apiKey;
    private String endpoint = "https://open.bigmodel.cn/api/paas/v4/embeddings";
    private String model = "embedding-3";
    private Integer dimensions = 768;
    private int batchSize = 64;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getDimensions() {
        return dimensions;
    }

    public void setDimensions(Integer dimensions) {
        this.dimensions = dimensions;
    }

    public int getBatchSize() {
        return Math.min(Math.max(batchSize, 1), 64);
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
}
