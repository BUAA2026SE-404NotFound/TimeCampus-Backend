package com.notfound.timecampusserver.ai;

import com.notfound.timecampuscommon.api.ResultCode;
import com.notfound.timecampuscommon.exception.BizException;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.AbstractEmbeddingModel;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ZhipuEmbeddingModel extends AbstractEmbeddingModel {

    private final ZhipuEmbeddingProperties properties;
    private final RestClient restClient;

    public ZhipuEmbeddingModel(ZhipuEmbeddingProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder.build();
    }

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        if (!StringUtils.hasText(properties.getApiKey())) {
            throw new BizException(ResultCode.INTERNAL_ERROR, "Zhipu embedding api key is not configured");
        }
        List<String> inputs = request.getInstructions();
        if (inputs == null || inputs.isEmpty()) {
            return new EmbeddingResponse(List.of());
        }

        List<Embedding> embeddings = new ArrayList<>();
        int globalIndex = 0;
        for (int start = 0; start < inputs.size(); start += properties.getBatchSize()) {
            int end = Math.min(inputs.size(), start + properties.getBatchSize());
            List<Embedding> batch = embedBatch(inputs.subList(start, end), globalIndex);
            embeddings.addAll(batch);
            globalIndex += batch.size();
        }
        return new EmbeddingResponse(embeddings);
    }

    @Override
    public float[] embed(Document document) {
        return embed(getEmbeddingContent(document));
    }

    @Override
    public int dimensions() {
        Integer dimensions = properties.getDimensions();
        return dimensions == null ? super.dimensions() : dimensions;
    }

    private List<Embedding> embedBatch(List<String> inputs, int indexOffset) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", properties.getModel());
        body.put("input", inputs);
        if (properties.getDimensions() != null) {
            body.put("dimensions", properties.getDimensions());
        }

        ZhipuEmbeddingResponse response = restClient.post()
                .uri(properties.getEndpoint())
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.setBearerAuth(properties.getApiKey()))
                .body(body)
                .retrieve()
                .body(ZhipuEmbeddingResponse.class);

        if (response == null || response.data() == null) {
            throw new BizException(ResultCode.INTERNAL_ERROR, "Zhipu embedding response is empty");
        }
        return response.data()
                .stream()
                .sorted(Comparator.comparingInt(ZhipuEmbeddingData::index))
                .map(item -> new Embedding(item.embedding(), indexOffset + item.index()))
                .toList();
    }

    private record ZhipuEmbeddingResponse(List<ZhipuEmbeddingData> data) {
    }

    private record ZhipuEmbeddingData(float[] embedding, int index) {
    }
}
