package com.notfound.timecampusserver.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.notfound.timecampuscommon.api.ResultCode;
import com.notfound.timecampuscommon.exception.BizException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TimeCampusAgentGateway {

    private static final String TOKEN_HEADER = "X-TimeCampus-Agent-Token";

    private final RestClient restClient;
    private final String token;

    public TimeCampusAgentGateway(RestClient.Builder builder,
                                  @Value("${timecampus.agent.base-url:http://127.0.0.1:8090}") String baseUrl,
                                  @Value("${timecampus.agent.token:}") String token) {
        this.restClient = builder.baseUrl(baseUrl).build();
        this.token = token == null ? "" : token.trim();
    }

    public JsonNode startOperation(String task) {
        return post("/internal/v1/operations/runs", Map.of("task", task));
    }

    public JsonNode listSessions() {
        return get("/internal/v1/operations/sessions");
    }

    public JsonNode createSession(String title) {
        return post(
                "/internal/v1/operations/sessions",
                title == null || title.isBlank() ? Map.of() : Map.of("title", title.trim())
        );
    }

    public JsonNode getSession(String sessionId) {
        requireToken();
        try {
            return restClient.get()
                    .uri(uri -> uri.pathSegment(
                            "internal", "v1", "operations", "sessions", sessionId
                    ).build())
                    .header(TOKEN_HEADER, token)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException exception) {
            throw unavailable(exception);
        }
    }

    public JsonNode recordSessionMessage(String sessionId, String role, String content) {
        return post(
                uriPath("internal", "v1", "operations", "sessions", sessionId, "messages"),
                Map.of("role", role, "content", content)
        );
    }

    public void streamSessionMessage(String sessionId, String task, OutputStream output) {
        streamPost(
                uriPath("internal", "v1", "operations", "sessions", sessionId, "messages", "stream"),
                Map.of("task", task),
                output
        );
    }

    public void streamDecisions(
            String threadId,
            List<Map<String, Object>> decisions,
            OutputStream output
    ) {
        streamPost(
                uriPath("internal", "v1", "operations", "runs", threadId, "decisions", "stream"),
                Map.of("decisions", decisions),
                output
        );
    }

    public JsonNode resumeOperation(String threadId, List<Map<String, Object>> decisions) {
        requireToken();
        try {
            return restClient.post()
                    .uri(uri -> uri.pathSegment(
                            "internal", "v1", "operations", "runs", threadId, "decisions"
                    ).build())
                    .header(TOKEN_HEADER, token)
                    .body(Map.of("decisions", decisions))
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException exception) {
            throw unavailable(exception);
        }
    }

    public JsonNode evalCases(String suite) {
        requireToken();
        try {
            return restClient.get()
                    .uri(uri -> uri.path("/internal/v1/evals/cases")
                            .queryParam("suite", suite)
                            .build())
                    .header(TOKEN_HEADER, token)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException exception) {
            throw unavailable(exception);
        }
    }

    private JsonNode get(String path) {
        requireToken();
        try {
            return restClient.get()
                    .uri(path)
                    .header(TOKEN_HEADER, token)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException exception) {
            throw unavailable(exception);
        }
    }

    public JsonNode runEval(String suite, String mode, Double minPassRate, Double minOverall) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("suite", suite);
        body.put("mode", mode);
        if (minPassRate != null) {
            body.put("min_pass_rate", minPassRate);
        }
        if (minOverall != null) {
            body.put("min_overall", minOverall);
        }
        return post("/internal/v1/evals/runs", body);
    }

    private JsonNode post(String path, Object body) {
        requireToken();
        try {
            return restClient.post()
                    .uri(path)
                    .header(TOKEN_HEADER, token)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException exception) {
            throw unavailable(exception);
        }
    }

    private void streamPost(String path, Object body, OutputStream output) {
        requireToken();
        try {
            restClient.post()
                    .uri(path)
                    .header(TOKEN_HEADER, token)
                    .body(body)
                    .exchange((request, response) -> {
                        if (!response.getStatusCode().is2xxSuccessful()) {
                            throw new IOException(
                                    "Agent stream returned HTTP " + response.getStatusCode().value()
                            );
                        }
                        response.getBody().transferTo(output);
                        output.flush();
                        return null;
                    });
        } catch (RestClientException exception) {
            throw unavailable(exception);
        }
    }

    private String uriPath(String... segments) {
        return "/" + String.join("/", segments);
    }

    private void requireToken() {
        if (token.isBlank()) {
            throw new BizException(ResultCode.BIZ_ERROR, "TimeCampus Agent service token is not configured");
        }
    }

    private BizException unavailable(Exception exception) {
        return new BizException(ResultCode.BIZ_ERROR,
                "TimeCampus Agent service unavailable: " + exception.getMessage());
    }
}
