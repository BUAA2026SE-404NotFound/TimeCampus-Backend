package com.notfound.timecampusserver.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "timecampus.ai.deepseek.chat")
public class DeepSeekChatProperties {

    private boolean enabled = false;
    private String apiKey;
    private String endpoint = "https://api.deepseek.com/v1/chat/completions";
    private String model = "deepseek-chat";
    private double temperature = 0.2;
    private int maxTokens = 1200;

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

    public double getTemperature() {
        return Math.max(0.0, Math.min(temperature, 1.0));
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getMaxTokens() {
        return Math.min(Math.max(maxTokens, 256), 4096);
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }
}
