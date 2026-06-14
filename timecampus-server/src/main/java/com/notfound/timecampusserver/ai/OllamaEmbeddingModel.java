package com.notfound.timecampusserver.ai;

import com.notfound.timecampuscommon.api.ResultCode;
import com.notfound.timecampuscommon.exception.BizException;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.AbstractEmbeddingModel;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OllamaEmbeddingModel extends AbstractEmbeddingModel {

    private final OllamaEmbeddingProperties properties;
    private final RestClient restClient;

    public OllamaEmbeddingModel(OllamaEmbeddingProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder
                .baseUrl(properties.getBaseUrl())
                .build();
    }

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
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

        OllamaEmbeddingResponse response = restClient.post()
                .uri("/api/embed")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(OllamaEmbeddingResponse.class);

        if (response == null || response.embeddings() == null) {
            throw new BizException(ResultCode.INTERNAL_ERROR, "Ollama embedding response is empty");
        }
        if (response.embeddings().isEmpty()) {
            throw new BizException(ResultCode.INTERNAL_ERROR, "Ollama embedding response has no embeddings");
        }
        if (response.embeddings().size() != inputs.size()) {
            throw new BizException(ResultCode.INTERNAL_ERROR,
                    "Ollama embedding response size does not match request size");
        }
        List<Embedding> embeddings = new ArrayList<>();
        for (int i = 0; i < response.embeddings().size(); i++) {
            embeddings.add(new Embedding(response.embeddings().get(i), indexOffset + i));
        }
        return embeddings;
    }

    private record OllamaEmbeddingResponse(String model, List<float[]> embeddings) {
    }
}
