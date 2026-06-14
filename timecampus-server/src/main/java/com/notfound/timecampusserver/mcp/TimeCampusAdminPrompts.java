package com.notfound.timecampusserver.mcp;

import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.springaicommunity.mcp.annotation.McpArg;
import org.springaicommunity.mcp.annotation.McpPrompt;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TimeCampusAdminPrompts {

    @McpPrompt(
            name = "timecampus_poi_editor",
            title = "TimeCampus POI Editor",
            description = "生成新增或编辑 POI 的 agent 工作提示，强调先查重、确认坐标和再写入。")
    public GetPromptResult poiEditor(
            @McpArg(name = "task", description = "用户要完成的 POI 维护任务", required = true) String task,
            @McpArg(name = "sourceNotes", description = "地点来源、史料、坐标来源或待整理素材", required = false) String sourceNotes) {
        return prompt("POI editor workflow", """
                你是时光航迹 TimeCampus 后端管理 agent，正在维护 POI 数据。

                用户任务：
                %s

                参考素材：
                %s

                工作流程：
                1. 先读取 timecampus://content-guidelines，理解 POI 字段和坐标要求。
                2. 用 timecampus_search_pois 按名称关键字查重，必要时读取 timecampus://pois。
                3. 新增 POI 前确认 name、latitude、longitude；坐标应与前端地图坐标系一致。
                4. 编辑已有 POI 前调用 timecampus_get_poi 或读取 timecampus://poi/{poiId}，不要覆盖未知字段。
                5. 只润色网站/小程序文案时优先使用 timecampus_update_poi_copy。
                6. 写入后再次读取 POI，向用户汇报变更字段和仍需人工核验的来源。
                """.formatted(task, blankToPlaceholder(sourceNotes)));
    }

    @McpPrompt(
            name = "timecampus_media_importer",
            title = "TimeCampus Media Importer",
            description = "生成新增或编辑影像资料的 agent 工作提示，适合官方历史影像入库和说明维护。")
    public GetPromptResult mediaImporter(
            @McpArg(name = "task", description = "用户要完成的影像维护任务", required = true) String task,
            @McpArg(name = "sourceMaterials", description = "图片 URL、年份、地点、说明等素材", required = false) String sourceMaterials,
            @McpArg(name = "defaultReviewStatus", description = "默认审核状态，建议 official 使用 approved", required = false) String defaultReviewStatus) {
        return prompt("Media importer workflow", """
                你是时光航迹 TimeCampus 影像资料维护 agent。

                用户任务：
                %s

                原始素材：
                %s

                默认审核状态：
                %s

                工作流程：
                1. 先读取 timecampus://content-guidelines，确认 year、imagePath、reviewStatus 约束。
                2. 对每张影像先确认 poiId；不确定地点时用 timecampus_search_pois 查找候选。
                3. 批量新增官方影像使用 timecampus_import_official_media；单条文案修订使用 timecampus_update_media_copy。
                4. 修正年份、图片路径、关联 POI 或审核状态时使用 timecampus_update_media_metadata。
                5. 写入后用 timecampus_list_media 或 timecampus://media/by-poi/{poiId} 检查时间线是否完整。
                6. 对来源不明、年份不确定、版权不清晰的图片，不要直接入库为 approved，应向用户列出待确认项。
                """.formatted(task, blankToPlaceholder(sourceMaterials), blankTo(defaultReviewStatus, "approved")));
    }

    @McpPrompt(
            name = "timecampus_copy_editor",
            title = "TimeCampus Copy Editor",
            description = "生成网站/小程序文案快速编辑提示，适合 POI 简介、冷知识和影像说明润色。")
    public GetPromptResult copyEditor(
            @McpArg(name = "target", description = "文案目标，例如 POI 简介、冷知识、影像说明", required = true) String target,
            @McpArg(name = "tone", description = "语气，例如克制、亲切、学术、面向新生", required = false) String tone,
            @McpArg(name = "maxLength", description = "建议最大字数", required = false) Integer maxLength,
            @McpArg(name = "draft", description = "待润色草稿或素材", required = false) String draft) {
        return prompt("Copy editor workflow", """
                你是时光航迹 TimeCampus 文案编辑 agent。

                编辑目标：%s
                语气：%s
                最大字数：%s
                草稿/素材：
                %s

                编辑原则：
                1. 保持史料表达克制可信，不编造年份、人物、事件和建筑用途。
                2. 文案面向地图和时间线浏览场景，优先短句、可扫读、有地点感。
                3. POI description 适合 1-3 句地点简介；funFact 适合一个轻量故事或冷知识。
                4. 影像 description 应说明画面、年份和地点关系，避免长段落。
                5. 写入前先读取目标当前值；只改文案时使用 timecampus_update_poi_copy 或 timecampus_update_media_copy。
                6. 如果素材不足，先给用户候选文案和待确认问题，不要直接写入事实性断言。
                """.formatted(
                target,
                blankTo(tone, "克制、清晰、适合校园历史导览"),
                maxLength == null ? "按字段场景控制" : maxLength,
                blankToPlaceholder(draft)));
    }

    @McpPrompt(
            name = "timecampus_content_auditor",
            title = "TimeCampus Content Auditor",
            description = "生成内容巡检提示，用于发现 POI 文案缺失、影像缺口、审核积压和时间线问题。")
    public GetPromptResult contentAuditor(
            @McpArg(name = "focus", description = "巡检重点，例如缺失文案、影像年份断层、待审核内容", required = false) String focus,
            @McpArg(name = "includeOffline", description = "是否包含下架 POI", required = false) Boolean includeOffline) {
        return prompt("Content audit workflow", """
                你是时光航迹 TimeCampus 内容巡检 agent。

                巡检重点：
                %s

                是否包含下架 POI：
                %s

                工作流程：
                1. 读取 timecampus://admin/dashboard，判断审核分布和内容增长趋势。
                2. 用 timecampus_search_pois 拉取 POI；includeOffline 按用户要求设置。
                3. 对重点 POI 读取 timecampus://poi/{poiId} 或 timecampus://media/by-poi/{poiId}。
                4. 输出问题清单：缺少简介、缺少冷知识、无官方影像、年份明显断层、pending 影像积压、疑似重复影像。
                5. 对每个问题给出建议动作和可调用工具，但未经用户确认不要执行删除。
                6. 对可安全执行的文案修订，先列出 diff 摘要再调用写工具。
                """.formatted(blankTo(focus, "全面巡检"), Boolean.TRUE.equals(includeOffline)));
    }

    @McpPrompt(
            name = "timecampus_rag_grounded_maintenance",
            title = "TimeCampus RAG Grounded Maintenance",
            description = "生成基于 RAG 的维护提示，要求 agent 先检索再编辑，避免编造地点、年份和来源。")
    public GetPromptResult ragGroundedMaintenance(
            @McpArg(name = "task", description = "要完成的维护任务", required = true) String task,
            @McpArg(name = "poiId", description = "如任务限定某个 POI，可提供 POI ID", required = false) Long poiId,
            @McpArg(name = "includePending", description = "是否允许检索待审核/驳回内容", required = false) Boolean includePending) {
        return prompt("RAG grounded maintenance workflow", """
                你是时光航迹 TimeCampus 的 RAG-grounded 管理 agent。

                任务：
                %s

                限定 POI：
                %s

                是否检索待审核/驳回内容：
                %s

                工作流程：
                1. 先读取 timecampus://rag/corpus，确认 RAG 当前覆盖 POI、影像、评论和维护规范。
                2. 调用 timecampus_rag_context_pack，task 使用用户任务原文；如果限定 POI，传入 poiId。
                3. 对 RAG 命中的每条候选，检查 document.uri；准备写入前必须再调用对应 read tool/resource 获取当前值。
                4. 只允许根据 RAG 命中文档和用户提供素材做事实性陈述，不要编造地点、年份、人物、来源或版权状态。
                5. 文案类修改优先调用 timecampus_update_poi_copy 或 timecampus_update_media_copy。
                6. 结构化字段修改使用 update_poi 或 update_media_metadata，并在回复中列出字段级变更。
                7. RAG 结果不足或互相矛盾时，停止写入并向用户列出需要确认的问题。
                """.formatted(task, poiId == null ? "（未限定）" : poiId, Boolean.TRUE.equals(includePending)));
    }

    private GetPromptResult prompt(String description, String text) {
        return new GetPromptResult(description, List.of(new PromptMessage(Role.USER, new TextContent(text))));
    }

    private String blankToPlaceholder(String value) {
        return blankTo(value, "（未提供）");
    }

    private String blankTo(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }
}
