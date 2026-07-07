package com.notfound.timecampusserver.mcp;

import com.notfound.timecampusserver.mcp.TimeCampusRagService.TimeCampusRagContextPack;
import com.notfound.timecampusserver.mcp.TimeCampusRagService.TimeCampusRagCorpusSummary;
import com.notfound.timecampusserver.mcp.TimeCampusRagService.TimeCampusRagSearchResult;
import com.notfound.timecampusserver.mcp.TimeCampusRagVectorIndexService.VectorIndexResult;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TimeCampusRagTools {

    private final TimeCampusRagService ragService;
    private final TimeCampusRagVectorIndexService vectorIndexService;

    public TimeCampusRagTools(TimeCampusRagService ragService,
                              TimeCampusRagVectorIndexService vectorIndexService) {
        this.ragService = ragService;
        this.vectorIndexService = vectorIndexService;
    }

    @McpTool(
            name = "timecampus_rag_search",
            description = "在 TimeCampus 管理知识库中检索 POI、影像、评论、维护规范和知识文档，供 agent 写入前做 grounded context。",
            annotations = @McpTool.McpAnnotations(
                    title = "Search TimeCampus RAG",
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false))
    public TimeCampusRagSearchResult ragSearch(
            @McpToolParam(description = "检索问题或维护任务，例如“主楼 2000 年旧照说明怎么写”", required = true)
            String query,
            @McpToolParam(description = "返回条数，默认 8，最大 20", required = false)
            Integer limit,
            @McpToolParam(description = "限定文档类型，可传 poi/media/comment/guideline/knowledge，支持逗号分隔", required = false)
            List<String> types,
            @McpToolParam(description = "限定某个 POI ID 的上下文，可为空", required = false)
            Long poiId,
            @McpToolParam(description = "是否包含 pending/rejected 内容；默认 false，仅检索 approved 影像和评论", required = false)
            Boolean includePending) {
        return ragService.search(query, limit, types, poiId, includePending);
    }

    @McpTool(
            name = "timecampus_rag_context_pack",
            description = "为一个维护任务生成 RAG 上下文包，包含检索命中文档和 agent 后续调用建议。",
            annotations = @McpTool.McpAnnotations(
                    title = "Build TimeCampus RAG Context Pack",
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false))
    public TimeCampusRagContextPack ragContextPack(
            @McpToolParam(description = "agent 要完成的维护任务", required = true)
            String task,
            @McpToolParam(description = "返回条数，默认 8，最大 20", required = false)
            Integer limit,
            @McpToolParam(description = "限定文档类型，可传 poi/media/comment/guideline/knowledge，支持逗号分隔", required = false)
            List<String> types,
            @McpToolParam(description = "限定某个 POI ID 的上下文，可为空", required = false)
            Long poiId,
            @McpToolParam(description = "是否包含 pending/rejected 内容；默认 false", required = false)
            Boolean includePending) {
        return ragService.contextPack(task, limit, types, poiId, includePending);
    }

    @McpTool(
            name = "timecampus_rag_corpus_summary",
            description = "查看 TimeCampus RAG 语料来源和文档数量，帮助 agent 判断检索覆盖范围。",
            annotations = @McpTool.McpAnnotations(
                    title = "TimeCampus RAG Corpus Summary",
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false))
    public TimeCampusRagCorpusSummary ragCorpusSummary() {
        return ragService.corpusSummary();
    }

    @McpTool(
            name = "timecampus_rag_rebuild_vector_index",
            description = "抽取 POI、影像、评论、维护规范和知识文档，切块后写入 Qdrant 向量库。需要配置 Qdrant 与 EmbeddingModel。",
            annotations = @McpTool.McpAnnotations(
                    title = "Rebuild TimeCampus RAG Vector Index",
                    readOnlyHint = false,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false))
    public VectorIndexResult rebuildVectorIndex(
            @McpToolParam(description = "限定文档类型，可传 poi/media/comment/guideline/knowledge，支持逗号分隔；为空则全部", required = false)
            List<String> types,
            @McpToolParam(description = "限定某个 POI ID 的上下文，可为空", required = false)
            Long poiId,
            @McpToolParam(description = "是否包含 pending/rejected 影像和评论；默认 false", required = false)
            Boolean includePending,
            @McpToolParam(description = "是否先删除同 source_id 的旧向量 chunk；建议 true", required = false)
            Boolean deleteExisting) {
        return vectorIndexService.rebuild(types, poiId, includePending, deleteExisting == null || deleteExisting);
    }
}
