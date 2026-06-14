package com.notfound.timecampusserver.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "timecampus.ai.ollama.embedding")
public class OllamaEmbeddingProperties {

    private boolean enabled = false;
    private String baseUrl = "http://localhost:11434";
    private String model = "all-minilm";
    private Integer dimensions = 384;
    private int batchSize = 32;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBaseUrl() {
        return baseUrl == null || baseUrl.isBlank() ? "http://localhost:11434" : baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getModel() {
        return model == null || model.isBlank() ? "all-minilm" : model;
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
