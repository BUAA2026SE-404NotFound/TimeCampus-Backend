package com.notfound.timecampusserver.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TimeCampusAgentGatewayTest {

    @Test
    void sendsInternalTokenAndReturnsAgentPayload() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        TimeCampusAgentGateway gateway = new TimeCampusAgentGateway(
                builder,
                "http://agent.test",
                "shared-token"
        );
        server.expect(requestTo("http://agent.test/internal/v1/operations/runs"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-TimeCampus-Agent-Token", "shared-token"))
                .andRespond(withSuccess(
                        """
                        {"threadId":"thread-1","status":"approval_required"}
                        """,
                        MediaType.APPLICATION_JSON
                ));

        assertThat(gateway.startOperation("更新主楼").path("status").asText())
                .isEqualTo("approval_required");
        server.verify();
    }

    @Test
    void forwardsSessionStreamWithoutBuffering() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        TimeCampusAgentGateway gateway = new TimeCampusAgentGateway(
                builder,
                "http://agent.test",
                "shared-token"
        );
        server.expect(requestTo(
                        "http://agent.test/internal/v1/operations/sessions/"
                                + "16cf781c-28d9-4f4a-9352-95e33df9067d/messages/stream"
                ))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-TimeCampus-Agent-Token", "shared-token"))
                .andRespond(withSuccess(
                        "event: delta\ndata: {\"content\":\"主楼\"}\n\n",
                        MediaType.TEXT_EVENT_STREAM
                ));
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        gateway.streamSessionMessage(
                "16cf781c-28d9-4f4a-9352-95e33df9067d",
                "查询主楼",
                output
        );

        assertThat(output.toString(StandardCharsets.UTF_8))
                .contains("event: delta", "\"content\":\"主楼\"");
        server.verify();
    }

    @Test
    void forwardsEvalStreamConfigurationAndEvents() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        TimeCampusAgentGateway gateway = new TimeCampusAgentGateway(
                builder,
                "http://agent.test",
                "shared-token"
        );
        server.expect(requestTo("http://agent.test/internal/v1/evals/runs/stream"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-TimeCampus-Agent-Token", "shared-token"))
                .andExpect(content().json("""
                        {
                          "suite":"maintenance",
                          "mode":"live",
                          "min_pass_rate":0.85,
                          "min_overall":80.0,
                          "min_consistency":0.8,
                          "repetitions":3,
                          "case_ids":["maintenance-multi-turn-context"]
                        }
                        """))
                .andRespond(withSuccess(
                        "event: case\ndata: {\"completed\":1,\"total\":3}\n\n",
                        MediaType.TEXT_EVENT_STREAM
                ));
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        gateway.streamEval(
                "maintenance",
                "live",
                0.85,
                80.0,
                0.8,
                3,
                List.of("maintenance-multi-turn-context"),
                output
        );

        assertThat(output.toString(StandardCharsets.UTF_8)).contains("event: case");
        server.verify();
    }
}
