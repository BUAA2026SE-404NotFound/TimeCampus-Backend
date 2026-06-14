package com.notfound.timecampusserver.ai;

import com.notfound.timecampuscommon.api.ResultCode;
import com.notfound.timecampuscommon.exception.BizException;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DeepSeekChatGenerator implements TimeCampusChatGenerator {

    private final DeepSeekChatProperties properties;
    private final RestClient restClient;

    public DeepSeekChatGenerator(DeepSeekChatProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder.build();
    }

    @Override
    public String generate(String systemPrompt, String userPrompt) {
        if (!StringUtils.hasText(properties.getApiKey())) {
            throw new BizException(ResultCode.INTERNAL_ERROR, "DeepSeek api key is not configured");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", properties.getModel());
        body.put("temperature", properties.getTemperature());
        body.put("max_tokens", properties.getMaxTokens());
        body.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        ));

        DeepSeekChatResponse response = restClient.post()
                .uri(properties.getEndpoint())
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.setBearerAuth(properties.getApiKey()))
                .body(body)
                .retrieve()
                .body(DeepSeekChatResponse.class);

        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new BizException(ResultCode.INTERNAL_ERROR, "DeepSeek chat response is empty");
        }
        DeepSeekMessage message = response.choices().get(0).message();
        return message == null || message.content() == null ? "" : message.content();
    }

    private record DeepSeekChatResponse(List<DeepSeekChoice> choices) {
    }

    private record DeepSeekChoice(DeepSeekMessage message) {
    }

    private record DeepSeekMessage(String content) {
    }
}
