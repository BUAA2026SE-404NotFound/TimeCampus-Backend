package com.notfound.timecampusserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notfound.timecampusserver.controller.admin.AdminAgentController;
import com.notfound.timecampusserver.mcp.TimeCampusAgentDraftService;
import com.notfound.timecampusserver.mcp.TimeCampusAgentDraftService.AgentDraftResult;
import com.notfound.timecampusserver.mcp.TimeCampusAgentDraftService.AgentQualityGate;
import com.notfound.timecampusserver.mcp.TimeCampusAgentDraftService.AgentQualityScore;
import com.notfound.timecampusserver.mcp.TimeCampusRagService;
import com.notfound.timecampusserver.mcp.TimeCampusRagService.TimeCampusRagContextPack;
import com.notfound.timecampusserver.mcp.TimeCampusRagService.TimeCampusRagDocument;
import com.notfound.timecampusserver.mcp.TimeCampusRagService.TimeCampusRagSearchResult;
import com.notfound.timecampusserver.mcp.TimeCampusRagVectorIndexService;
import com.notfound.timecampusserver.mcp.TimeCampusRagVectorIndexService.VectorIndexResult;
import com.notfound.timecampusserver.service.TimeCampusAgentGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminAgentControllerWebMvcTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;
    private TimeCampusRagService ragService;
    private TimeCampusRagVectorIndexService vectorIndexService;
    private TimeCampusAgentDraftService draftService;
    private TimeCampusAgentGateway agentGateway;

    @BeforeEach
    void setUp() {
        ragService = mock(TimeCampusRagService.class);
        vectorIndexService = mock(TimeCampusRagVectorIndexService.class);
        draftService = mock(TimeCampusAgentDraftService.class);
        agentGateway = mock(TimeCampusAgentGateway.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AdminAgentController(
                        ragService,
                        vectorIndexService,
                        draftService,
                        agentGateway,
                        objectMapper
                ))
                .build();
    }

    @Test
    void contextPackUsesVersionedAgentApi() throws Exception {
        TimeCampusRagContextPack contextPack = contextPack("维护主楼文案");
        when(ragService.contextPack(eq("维护主楼文案"), eq(6), any(), eq(1L), eq(true)))
                .thenReturn(contextPack);

        mockMvc.perform(post("/api/v1/admin/agent/rag/context-pack")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "task", "维护主楼文案",
                                "limit", 6,
                                "types", List.of("poi", "media"),
                                "poiId", 1,
                                "includePending", true
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.task").value("维护主楼文案"))
                .andExpect(jsonPath("$.data.retrieval.hits[0].document.uri").value("timecampus://poi/1"));
    }

    @Test
    void draftReturnsModeQualityAndGroundedContext() throws Exception {
        AgentDraftResult result = new AgentDraftResult(
                "维护主楼文案",
                "model",
                "摘要：建议更新主楼文案。",
                contextPack("维护主楼文案"),
                new AgentQualityScore(96, 100, 90, 55, 90),
                new AgentQualityGate(true, 85, 80, List.of("达到执行线")),
                List.of("可执行")
        );
        when(draftService.draft(eq("维护主楼文案"), eq(6), any(), eq(null), eq(true))).thenReturn(result);

        mockMvc.perform(post("/api/v1/admin/agent/draft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "task", "维护主楼文案",
                                "limit", 6,
                                "includePending", true
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.mode").value("model"))
                .andExpect(jsonPath("$.data.quality.overall").value(90))
                .andExpect(jsonPath("$.data.qualityGate.executable").value(true))
                .andExpect(jsonPath("$.data.qualityGate.minOverall").value(85))
                .andExpect(jsonPath("$.data.contextPack.retrieval.hits[0].document.type").value("poi"));
    }

    @Test
    void rebuildIndexReturnsVectorIndexStatus() throws Exception {
        when(vectorIndexService.rebuild(any(), eq(null), eq(true), eq(true)))
                .thenReturn(new VectorIndexResult("indexed", 3, 5, "ok"));

        mockMvc.perform(post("/api/v1/admin/agent/rag/rebuild-index")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "types", List.of("poi"),
                                "includePending", true,
                                "deleteExisting", true
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("indexed"))
                .andExpect(jsonPath("$.data.vectorDocumentCount").value(5));
    }

    @Test
    void operationStopsWhenQualityGateBlocksExecution() throws Exception {
        AgentDraftResult blocked = new AgentDraftResult(
                "删除主楼",
                "rule",
                "仅生成草案",
                contextPack("删除主楼"),
                new AgentQualityScore(20, 60, 45, 20, 38),
                new AgentQualityGate(false, 85, 80, List.of("overall 低于 85")),
                List.of("仅草案")
        );
        when(draftService.draft(eq("删除主楼"), eq(null), eq(null), eq(null), eq(null)))
                .thenReturn(blocked);

        mockMvc.perform(post("/api/v1/admin/agent/operations/runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("task", "删除主楼"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("blocked"))
                .andExpect(jsonPath("$.data.execution").doesNotExist());

        verifyNoInteractions(agentGateway);
    }

    @Test
    void operationStartsAgentAfterQualityGatePasses() throws Exception {
        AgentDraftResult executable = new AgentDraftResult(
                "更新主楼简介",
                "rule",
                "建议更新",
                contextPack("更新主楼简介"),
                new AgentQualityScore(96, 100, 90, 55, 90),
                new AgentQualityGate(true, 85, 80, List.of("达到执行线")),
                List.of("可执行")
        );
        when(draftService.draft(eq("更新主楼简介"), eq(null), eq(null), eq(null), eq(null)))
                .thenReturn(executable);
        when(agentGateway.startOperation("更新主楼简介"))
                .thenReturn(objectMapper.readTree("""
                        {"threadId":"thread-1","status":"approval_required","pendingActions":[]}
                        """));

        mockMvc.perform(post("/api/v1/admin/agent/operations/runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("task", "更新主楼简介"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("approval_required"))
                .andExpect(jsonPath("$.data.execution.threadId").value("thread-1"));
    }

    @Test
    void operationRejectsTaskOverTwentyThousandCharacters() throws Exception {
        mockMvc.perform(post("/api/v1/admin/agent/operations/runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "task", "校".repeat(20_001)
                        ))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(draftService, agentGateway);
    }

    @Test
    void sessionRejectsTitleOverSixtyCharacters() throws Exception {
        mockMvc.perform(post("/api/v1/admin/agent/operations/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "校".repeat(61)
                        ))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(agentGateway);
    }

    @Test
    void evalRunForwardsRepeatAndCaseSelection() throws Exception {
        when(agentGateway.runEval(
                eq("maintenance"),
                eq("live"),
                eq(0.85),
                eq(80.0),
                eq(0.8),
                eq(3),
                eq(List.of("maintenance-multi-turn-context"))
        )).thenReturn(objectMapper.readTree("""
                {"runId":"run-1","gatePassed":true,"total":3}
                """));

        mockMvc.perform(post("/api/v1/admin/agent/evals/runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "suite", "maintenance",
                                "mode", "live",
                                "repetitions", 3,
                                "caseIds", List.of("maintenance-multi-turn-context")
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.gatePassed").value(true))
                .andExpect(jsonPath("$.data.total").value(3));
    }

    @Test
    void evalStreamDisablesProxyBuffering() throws Exception {
        mockMvc.perform(post("/api/v1/admin/agent/evals/runs/stream")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "suite", "maintenance",
                                "mode", "live",
                                "repetitions", 1
                        ))))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-cache"))
                .andExpect(header().string("X-Accel-Buffering", "no"));
    }

    @Test
    void evalHistoryAndBadCaseLifecycleAreProxied() throws Exception {
        when(agentGateway.listEvalRuns(10)).thenReturn(objectMapper.readTree("""
                {"runs":[{"runId":"run-1","gatePassed":false}]}
                """));
        when(agentGateway.updateBadCase("bad-1", "resolved", "已处理"))
                .thenReturn(objectMapper.readTree("""
                        {"id":"bad-1","status":"resolved","resolution":"已处理"}
                        """));

        mockMvc.perform(get("/api/v1/admin/agent/evals/runs").param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.runs[0].runId").value("run-1"));

        mockMvc.perform(patch("/api/v1/admin/agent/evals/bad-cases/bad-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "status", "resolved",
                                "resolution", "已处理"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("resolved"));

        verify(agentGateway).updateBadCase("bad-1", "resolved", "已处理");
    }

    private TimeCampusRagContextPack contextPack(String task) {
        TimeCampusRagDocument document = new TimeCampusRagDocument(
                "poi:1",
                "poi",
                "主楼",
                "主楼 description",
                "timecampus://poi/1",
                new LinkedHashMap<>()
        );
        TimeCampusRagSearchResult searchResult = new TimeCampusRagSearchResult(
                task,
                "retriever=lexical",
                1,
                List.of(new TimeCampusRagSearchResult.Hit(12, "matched query", document))
        );
        return new TimeCampusRagContextPack(task, List.of("先检索", "再读取"), searchResult);
    }
}
