package com.notfound.timecampusserver.mcp;

import com.notfound.timecampusserver.ai.TimeCampusChatGenerator;
import com.notfound.timecampusserver.mcp.TimeCampusRagService.TimeCampusRagContextPack;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TimeCampusAgentDraftService {

    private final TimeCampusRagService ragService;
    private final ObjectProvider<TimeCampusChatGenerator> chatGeneratorProvider;

    public TimeCampusAgentDraftService(TimeCampusRagService ragService,
                                       ObjectProvider<TimeCampusChatGenerator> chatGeneratorProvider) {
        this.ragService = ragService;
        this.chatGeneratorProvider = chatGeneratorProvider;
    }

    public AgentDraftResult draft(String task,
                                  Integer limit,
                                  List<String> types,
                                  Long poiId,
                                  Boolean includePending) {
        TimeCampusRagContextPack contextPack = ragService.contextPack(task, limit, types, poiId, includePending);
        AgentQualityScore quality = score(contextPack);
        TimeCampusChatGenerator chatGenerator = chatGeneratorProvider.getIfAvailable();
        String mode = chatGenerator == null ? "rule" : "model";
        String draft = chatGenerator == null
                ? ruleDraft(task, contextPack)
                : chatGenerator.generate(systemPrompt(), userPrompt(task, contextPack));
        return new AgentDraftResult(task, mode, draft, contextPack, quality, gates(quality));
    }

    private String ruleDraft(String task, TimeCampusRagContextPack contextPack) {
        StringBuilder draft = new StringBuilder();
        draft.append("任务：").append(task).append('\n');
        draft.append("处理建议：先读取命中的 MCP resource，再执行最小范围写操作。").append('\n');
        draft.append("引用：").append('\n');
        contextPack.retrieval().hits().forEach(hit ->
                draft.append("- ")
                        .append(hit.document().uri())
                        .append(" / ")
                        .append(hit.document().title())
                        .append('\n'));
        draft.append("风险：删除、版权不明、年份/坐标不确定时停止并请求人工确认。");
        return draft.toString();
    }

    private String systemPrompt() {
        return """
                You are the TimeCampus backend maintenance agent.
                Use only the provided RAG context. Do not invent dates, coordinates, sources, people, copyright status or operational data.
                Return concise Chinese output with sections: 摘要, 引用, 草案, 风险, 建议工具.
                Destructive operations and uncertain media rights must ask for human confirmation.
                """;
    }

    private String userPrompt(String task, TimeCampusRagContextPack contextPack) {
        return "任务：\n" + task + "\n\nRAG context pack：\n" + contextPack;
    }

    private AgentQualityScore score(TimeCampusRagContextPack contextPack) {
        int citedItems = contextPack.retrieval().hits().size();
        int plannedActions = Math.max(1, contextPack.workflow().size());
        int grounding = clamp(citedItems * 24);
        int actionSafety = 100;
        int completeness = citedItems > 0 ? 90 : 45;
        int citationDensity = clamp((int) Math.round((double) citedItems / plannedActions * 55));
        int overall = clamp((int) Math.round(
                grounding * 0.3 + actionSafety * 0.3 + completeness * 0.25 + citationDensity * 0.15));
        return new AgentQualityScore(grounding, actionSafety, completeness, citationDensity, overall);
    }

    private List<String> gates(AgentQualityScore quality) {
        if (quality.overall() >= 85 && quality.actionSafety() >= 80) {
            return List.of("可执行", "写入前仍需展示变更摘要");
        }
        return List.of("仅草案", "补充引用或人工复核后再写入");
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    public record AgentDraftResult(String task,
                                   String mode,
                                   String draft,
                                   TimeCampusRagContextPack contextPack,
                                   AgentQualityScore quality,
                                   List<String> gates) {
    }

    public record AgentQualityScore(int grounding,
                                    int actionSafety,
                                    int completeness,
                                    int citationDensity,
                                    int overall) {
    }
}
