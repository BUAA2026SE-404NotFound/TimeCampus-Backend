package com.notfound.timecampusserver.mcp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notfound.timecampusserver.service.AdminDashboardService;
import com.notfound.timecampusserver.service.AdminMediaService;
import com.notfound.timecampusserver.service.PoiService;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import org.springaicommunity.mcp.annotation.McpArg;
import org.springaicommunity.mcp.annotation.McpResource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class TimeCampusAdminResources {

    private static final String JSON = "application/json";

    private final PoiService poiService;
    private final AdminMediaService adminMediaService;
    private final AdminDashboardService adminDashboardService;
    private final TimeCampusRagService ragService;
    private final TimeCampusMcpAdminScope adminScope;
    private final ObjectMapper objectMapper;

    public TimeCampusAdminResources(PoiService poiService,
                                    AdminMediaService adminMediaService,
                                    AdminDashboardService adminDashboardService,
                                    TimeCampusRagService ragService,
                                    TimeCampusMcpAdminScope adminScope,
                                    ObjectMapper objectMapper) {
        this.poiService = poiService;
        this.adminMediaService = adminMediaService;
        this.adminDashboardService = adminDashboardService;
        this.ragService = ragService;
        this.adminScope = adminScope;
        this.objectMapper = objectMapper;
    }

    @McpResource(
            uri = "timecampus://pois",
            name = "timecampus-poi-catalog",
            title = "TimeCampus POI Catalog",
            description = "完整 POI 目录，包含上架和下架地点，供 agent 维护前建立全局上下文。",
            mimeType = JSON)
    public ReadResourceResult poiCatalog() {
        return adminScope.call(() -> jsonResource("timecampus://pois",
                Map.of("pois", poiService.list(null, null))));
    }

    @McpResource(
            uri = "timecampus://poi/{poiId}",
            name = "timecampus-poi-detail",
            title = "TimeCampus POI Detail",
            description = "单个 POI 及其影像资料，用于编辑 POI 和关联历史影像前检查上下文。",
            mimeType = JSON)
    public ReadResourceResult poiDetail(
            @McpArg(name = "poiId", description = "POI ID", required = true) Long poiId) {
        return adminScope.call(() -> jsonResource("timecampus://poi/" + poiId,
                Map.of(
                        "poi", poiService.getById(poiId),
                        "media", adminMediaService.list(poiId, null, null, null, null)
                )));
    }

    @McpResource(
            uri = "timecampus://media/{mediaId}",
            name = "timecampus-media-detail",
            title = "TimeCampus Media Detail",
            description = "单条影像资料详情，用于编辑影像说明、年份、路径、审核状态前检查当前值。",
            mimeType = JSON)
    public ReadResourceResult mediaDetail(
            @McpArg(name = "mediaId", description = "影像 ID", required = true) Long mediaId) {
        return adminScope.call(() -> jsonResource("timecampus://media/" + mediaId,
                Map.of("media", adminMediaService.getById(mediaId))));
    }

    @McpResource(
            uri = "timecampus://media/by-poi/{poiId}",
            name = "timecampus-media-by-poi",
            title = "TimeCampus Media By POI",
            description = "某个 POI 下的全部影像资料，适合批量维护一个地点的时间线影像。",
            mimeType = JSON)
    public ReadResourceResult mediaByPoi(
            @McpArg(name = "poiId", description = "POI ID", required = true) Long poiId) {
        return adminScope.call(() -> jsonResource("timecampus://media/by-poi/" + poiId,
                Map.of("media", adminMediaService.list(poiId, null, null, null, null))));
    }

    @McpResource(
            uri = "timecampus://admin/dashboard",
            name = "timecampus-admin-dashboard",
            title = "TimeCampus Admin Dashboard",
            description = "管理端统计概览，用于 agent 判断内容体量、审核分布和近期趋势。",
            mimeType = JSON)
    public ReadResourceResult adminDashboard() {
        return adminScope.call(() -> jsonResource("timecampus://admin/dashboard",
                Map.of("stats", adminDashboardService.stats())));
    }

    @McpResource(
            uri = "timecampus://rag/corpus",
            name = "timecampus-rag-corpus",
            title = "TimeCampus RAG Corpus",
            description = "TimeCampus RAG 语料摘要，说明当前 MCP RAG 会检索哪些数据源。",
            mimeType = JSON)
    public ReadResourceResult ragCorpus() {
        return jsonResource("timecampus://rag/corpus", Map.of("corpus", ragService.corpusSummary()));
    }

    @McpResource(
            uri = "timecampus://content-guidelines",
            name = "timecampus-content-guidelines",
            title = "TimeCampus Content Guidelines",
            description = "POI、影像和网站文案维护规则，供 agent 在写入前参考。",
            mimeType = JSON)
    public ReadResourceResult contentGuidelines() {
        return jsonResource("timecampus://content-guidelines", Map.of(
                "poi", Map.of(
                        "requiredFields", List.of("name", "latitude", "longitude"),
                        "copyFields", List.of("description", "funFact"),
                        "status", "1 means published, 0 means offline",
                        "coordinateSystem", "Tencent Map / GCJ-02 coordinates are expected by the frontend map"),
                "media", Map.of(
                        "officialType", "official",
                        "reviewStatuses", List.of("pending", "approved", "rejected"),
                        "yearRange", "1953 to current year",
                        "copyFields", List.of("description"),
                        "imagePath", "Use a frontend-accessible URL or an existing storage path"),
                "agentWorkflow", List.of(
                        "Read the relevant resource before writing.",
                        "Prefer update_poi_copy or update_media_copy for copy-only edits.",
                        "Use import_official_media for batch official image records.",
                        "Use delete tools only after explicit human confirmation.")
        ));
    }

    private ReadResourceResult jsonResource(String uri, Object body) {
        try {
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(body);
            return new ReadResourceResult(List.of(new TextResourceContents(uri, JSON, json)));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("failed to serialize MCP resource: " + uri, e);
        }
    }
}
