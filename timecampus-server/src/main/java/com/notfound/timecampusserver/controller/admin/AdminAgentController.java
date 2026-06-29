package com.notfound.timecampusserver.controller.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notfound.timecampuscommon.api.ApiResponse;
import com.notfound.timecampusserver.mcp.TimeCampusAgentDraftService;
import com.notfound.timecampusserver.mcp.TimeCampusAgentDraftService.AgentDraftResult;
import com.notfound.timecampusserver.mcp.TimeCampusRagService;
import com.notfound.timecampusserver.mcp.TimeCampusRagService.TimeCampusRagContextPack;
import com.notfound.timecampusserver.mcp.TimeCampusRagService.TimeCampusRagSearchResult;
import com.notfound.timecampusserver.mcp.TimeCampusRagVectorIndexService;
import com.notfound.timecampusserver.mcp.TimeCampusRagVectorIndexService.VectorIndexResult;
import com.notfound.timecampusserver.service.TimeCampusAgentGateway;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Tag(name = "Admin-Agent", description = "管理员：AI Agent RAG 封装")
@RestController
@RequestMapping("/api/v1/admin/agent")
public class AdminAgentController {

    private final TimeCampusRagService ragService;
    private final TimeCampusRagVectorIndexService vectorIndexService;
    private final TimeCampusAgentDraftService draftService;
    private final TimeCampusAgentGateway agentGateway;
    private final ObjectMapper objectMapper;

    public AdminAgentController(TimeCampusRagService ragService,
                                TimeCampusRagVectorIndexService vectorIndexService,
                                TimeCampusAgentDraftService draftService,
                                TimeCampusAgentGateway agentGateway,
                                ObjectMapper objectMapper) {
        this.ragService = ragService;
        this.vectorIndexService = vectorIndexService;
        this.draftService = draftService;
        this.agentGateway = agentGateway;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/rag/search")
    @Operation(summary = "Agent RAG 检索")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<TimeCampusRagSearchResult> search(@Valid @RequestBody RagSearchRequest request) {
        return ApiResponse.success(ragService.search(
                request.query(),
                request.limit(),
                request.types(),
                request.poiId(),
                request.includePending()
        ));
    }

    @PostMapping("/rag/context-pack")
    @Operation(summary = "Agent RAG 上下文包")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<TimeCampusRagContextPack> contextPack(@Valid @RequestBody RagContextPackRequest request) {
        return ApiResponse.success(ragService.contextPack(
                request.task(),
                request.limit(),
                request.types(),
                request.poiId(),
                request.includePending()
        ));
    }

    @PostMapping("/draft")
    @Operation(summary = "生成 Agent 维护草案")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<AgentDraftResult> draft(@Valid @RequestBody AgentDraftRequest request) {
        return ApiResponse.success(draftService.draft(
                request.task(),
                request.limit(),
                request.types(),
                request.poiId(),
                request.includePending()
        ));
    }

    @PostMapping("/rag/rebuild-index")
    @Operation(summary = "重建 Agent 向量索引")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<VectorIndexResult> rebuildIndex(@RequestBody(required = false) RagIndexRequest request) {
        RagIndexRequest normalized = request == null
                ? new RagIndexRequest(null, null, true, true)
                : request;
        return ApiResponse.success(vectorIndexService.rebuild(
                normalized.types(),
                normalized.poiId(),
                normalized.includePending(),
                normalized.deleteExisting()
        ));
    }

    @PostMapping("/operations/runs")
    @Operation(summary = "运行运营智能体", description = "先执行 RAG 质量门禁，再进入 MCP 工具审批流程。")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<AgentOperationRunResult> runOperation(
            @Valid @RequestBody AgentOperationRunRequest request) {
        AgentDraftResult preflight = draftService.draft(
                request.task(),
                request.limit(),
                request.types(),
                request.poiId(),
                request.includePending()
        );
        if (!preflight.qualityGate().executable()) {
            return ApiResponse.success(new AgentOperationRunResult("blocked", preflight, null));
        }
        JsonNode execution = agentGateway.startOperation(request.task());
        return ApiResponse.success(new AgentOperationRunResult(
                execution.path("status").asText("completed"),
                preflight,
                execution
        ));
    }

    @GetMapping("/operations/sessions")
    @Operation(summary = "查询运营智能体会话")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<JsonNode> listOperationSessions() {
        return ApiResponse.success(agentGateway.listSessions());
    }

    @PostMapping("/operations/sessions")
    @Operation(summary = "创建运营智能体会话")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<JsonNode> createOperationSession(
            @RequestBody(required = false) AgentSessionCreateRequest request) {
        return ApiResponse.success(agentGateway.createSession(request == null ? null : request.title()));
    }

    @GetMapping("/operations/sessions/{sessionId}")
    @Operation(summary = "查询运营智能体会话消息")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<JsonNode> getOperationSession(@PathVariable String sessionId) {
        return ApiResponse.success(agentGateway.getSession(sessionId));
    }

    @PostMapping(
            value = "/operations/sessions/{sessionId}/messages/stream",
            produces = "text/event-stream"
    )
    @Operation(summary = "流式运行运营智能体")
    @SecurityRequirement(name = "bearerAuth")
    public StreamingResponseBody streamOperation(
            @PathVariable String sessionId,
            @Valid @RequestBody AgentOperationRunRequest request) {
        return output -> {
            try {
                writeEvent(output, "status", Map.of(
                        "stage", "preflight",
                        "message", "正在执行 RAG 检索与质量门禁"
                ));
                AgentDraftResult preflight = draftService.draft(
                        request.task(),
                        request.limit(),
                        request.types(),
                        request.poiId(),
                        request.includePending()
                );
                String status = preflight.qualityGate().executable() ? "running" : "blocked";
                writeEvent(output, "preflight", Map.of(
                        "status", status,
                        "preflight", preflight
                ));
                if (!preflight.qualityGate().executable()) {
                    agentGateway.recordSessionMessage(sessionId, "user", request.task());
                    agentGateway.recordSessionMessage(sessionId, "assistant", preflight.draft());
                    writeEvent(output, "done", Map.of("status", "blocked"));
                    return;
                }
                agentGateway.streamSessionMessage(sessionId, request.task(), output);
            } catch (Exception exception) {
                writeEvent(output, "error", Map.of(
                        "message", exception.getMessage() == null
                                ? "运营智能体流式请求失败"
                                : exception.getMessage()
                ));
            }
        };
    }

    @PostMapping("/operations/runs/{threadId}/decisions")
    @Operation(summary = "审批并恢复运营智能体")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<JsonNode> resumeOperation(
            @PathVariable String threadId,
            @Valid @RequestBody AgentOperationDecisionRequest request) {
        return ApiResponse.success(agentGateway.resumeOperation(threadId, request.decisions()));
    }

    @PostMapping(
            value = "/operations/runs/{threadId}/decisions/stream",
            produces = "text/event-stream"
    )
    @Operation(summary = "流式审批并恢复运营智能体")
    @SecurityRequirement(name = "bearerAuth")
    public StreamingResponseBody streamResumeOperation(
            @PathVariable String threadId,
            @Valid @RequestBody AgentOperationDecisionRequest request) {
        return output -> agentGateway.streamDecisions(threadId, request.decisions(), output);
    }

    @GetMapping("/evals/cases")
    @Operation(summary = "查询 Agent Eval 用例")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<JsonNode> evalCases(
            @RequestParam(defaultValue = "all")
            @Pattern(regexp = "all|maintenance|guide") String suite) {
        return ApiResponse.success(agentGateway.evalCases(suite));
    }

    @PostMapping("/evals/runs")
    @Operation(summary = "运行 Agent Eval")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<JsonNode> runEval(@Valid @RequestBody AgentEvalRunRequest request) {
        return ApiResponse.success(agentGateway.runEval(
                request.suite(),
                request.mode(),
                request.minPassRate(),
                request.minOverall()
        ));
    }

    public record RagSearchRequest(@NotBlank String query,
                                   Integer limit,
                                   List<String> types,
                                   Long poiId,
                                   Boolean includePending) {
    }

    public record RagContextPackRequest(@NotBlank String task,
                                        Integer limit,
                                        List<String> types,
                                        Long poiId,
                                        Boolean includePending) {
    }

    public record AgentDraftRequest(@NotBlank String task,
                                    Integer limit,
                                    List<String> types,
                                    Long poiId,
                                    Boolean includePending) {
    }

    public record AgentOperationRunRequest(@NotBlank String task,
                                           Integer limit,
                                           List<String> types,
                                           Long poiId,
                                           Boolean includePending) {
    }

    public record AgentSessionCreateRequest(String title) {
    }

    public record AgentOperationDecisionRequest(@NotEmpty List<Map<String, Object>> decisions) {
    }

    public record AgentEvalRunRequest(
            @Pattern(regexp = "all|maintenance|guide") String suite,
            @Pattern(regexp = "fixture|live") String mode,
            @DecimalMin("0.0") @DecimalMax("1.0") Double minPassRate,
            @DecimalMin("0.0") @DecimalMax("100.0") Double minOverall) {
        public AgentEvalRunRequest {
            suite = suite == null ? "all" : suite;
            mode = mode == null ? "fixture" : mode;
        }
    }

    public record AgentOperationRunResult(String status,
                                          AgentDraftResult preflight,
                                          JsonNode execution) {
    }

    public record RagIndexRequest(List<String> types,
                                  Long poiId,
                                  Boolean includePending,
                                  Boolean deleteExisting) {
    }

    private void writeEvent(OutputStream output, String event, Object data) throws IOException {
        String payload = objectMapper.writeValueAsString(data);
        output.write(
                ("event: " + event + "\ndata: " + payload + "\n\n")
                        .getBytes(StandardCharsets.UTF_8)
        );
        output.flush();
    }
}
