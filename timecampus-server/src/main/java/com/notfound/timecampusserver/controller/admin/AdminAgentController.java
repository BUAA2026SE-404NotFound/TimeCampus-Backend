package com.notfound.timecampusserver.controller.admin;

import com.notfound.timecampuscommon.api.ApiResponse;
import com.notfound.timecampusserver.mcp.TimeCampusAgentDraftService;
import com.notfound.timecampusserver.mcp.TimeCampusAgentDraftService.AgentDraftResult;
import com.notfound.timecampusserver.mcp.TimeCampusRagService;
import com.notfound.timecampusserver.mcp.TimeCampusRagService.TimeCampusRagContextPack;
import com.notfound.timecampusserver.mcp.TimeCampusRagService.TimeCampusRagSearchResult;
import com.notfound.timecampusserver.mcp.TimeCampusRagVectorIndexService;
import com.notfound.timecampusserver.mcp.TimeCampusRagVectorIndexService.VectorIndexResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Admin-Agent", description = "管理员：AI Agent RAG 封装")
@RestController
@RequestMapping("/api/v1/admin/agent")
public class AdminAgentController {

    private final TimeCampusRagService ragService;
    private final TimeCampusRagVectorIndexService vectorIndexService;
    private final TimeCampusAgentDraftService draftService;

    public AdminAgentController(TimeCampusRagService ragService,
                                TimeCampusRagVectorIndexService vectorIndexService,
                                TimeCampusAgentDraftService draftService) {
        this.ragService = ragService;
        this.vectorIndexService = vectorIndexService;
        this.draftService = draftService;
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

    public record RagIndexRequest(List<String> types,
                                  Long poiId,
                                  Boolean includePending,
                                  Boolean deleteExisting) {
    }
}
