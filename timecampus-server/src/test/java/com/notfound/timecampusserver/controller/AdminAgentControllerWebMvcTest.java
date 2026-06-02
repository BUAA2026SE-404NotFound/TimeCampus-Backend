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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminAgentControllerWebMvcTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;
    private TimeCampusRagService ragService;
    private TimeCampusRagVectorIndexService vectorIndexService;
    private TimeCampusAgentDraftService draftService;

    @BeforeEach
    void setUp() {
        ragService = mock(TimeCampusRagService.class);
        vectorIndexService = mock(TimeCampusRagVectorIndexService.class);
        draftService = mock(TimeCampusAgentDraftService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AdminAgentController(ragService, vectorIndexService, draftService))
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
