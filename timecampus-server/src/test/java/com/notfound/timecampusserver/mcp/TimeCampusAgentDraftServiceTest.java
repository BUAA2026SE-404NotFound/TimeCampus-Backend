package com.notfound.timecampusserver.mcp;

import com.notfound.timecampusserver.ai.TimeCampusChatGenerator;
import com.notfound.timecampusserver.mcp.TimeCampusAgentDraftService.AgentDraftResult;
import com.notfound.timecampusserver.mcp.TimeCampusRagService.TimeCampusRagContextPack;
import com.notfound.timecampusserver.mcp.TimeCampusRagService.TimeCampusRagDocument;
import com.notfound.timecampusserver.mcp.TimeCampusRagService.TimeCampusRagSearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.LinkedHashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TimeCampusAgentDraftServiceTest {

    private TimeCampusRagService ragService;
    private TimeCampusAgentDraftService draftService;

    @BeforeEach
    void setUp() {
        ragService = mock(TimeCampusRagService.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<TimeCampusChatGenerator> chatGeneratorProvider = mock(ObjectProvider.class);
        when(chatGeneratorProvider.getIfAvailable()).thenReturn(null);
        draftService = new TimeCampusAgentDraftService(ragService, chatGeneratorProvider);
    }

    @Test
    void draftMarksStrongGroundedResultExecutable() {
        String task = "为主楼补充面向游客的简介";
        when(ragService.contextPack(eq(task), eq(6), eq(List.of("poi", "media")), eq(1L), eq(true)))
                .thenReturn(contextPack(task, 4, "主楼资料完整"));

        AgentDraftResult result = draftService.draft(task, 6, List.of("poi", "media"), 1L, true);

        assertThat(result.mode()).isEqualTo("rule");
        assertThat(result.quality().overall()).isGreaterThanOrEqualTo(85);
        assertThat(result.qualityGate().executable()).isTrue();
        assertThat(result.qualityGate().minOverall()).isEqualTo(85);
        assertThat(result.qualityGate().minActionSafety()).isEqualTo(80);
        assertThat(result.gates()).contains("可执行");
    }

    @Test
    void draftBlocksDestructiveOrUncertainTaskBelowExecutionGate() {
        String task = "删除一张版权不明的主楼旧照";
        when(ragService.contextPack(eq(task), eq(6), eq(List.of("media")), eq(1L), eq(true)))
                .thenReturn(contextPack(task, 2, "版权不明，来源待确认"));

        AgentDraftResult result = draftService.draft(task, 6, List.of("media"), 1L, true);

        assertThat(result.quality().actionSafety()).isLessThan(80);
        assertThat(result.qualityGate().executable()).isFalse();
        assertThat(result.qualityGate().reasons()).contains("actionSafety 低于 80");
        assertThat(result.gates()).contains("仅草案");
    }

    private TimeCampusRagContextPack contextPack(String task, int hitCount, String text) {
        List<TimeCampusRagSearchResult.Hit> hits = java.util.stream.IntStream.rangeClosed(1, hitCount)
                .mapToObj(index -> new TimeCampusRagSearchResult.Hit(
                        10,
                        "matched query",
                        new TimeCampusRagDocument(
                                "media:" + index,
                                "media",
                                "主楼影像 " + index,
                                text,
                                "timecampus://media/" + index,
                                new LinkedHashMap<>()
                        )
                ))
                .toList();
        return new TimeCampusRagContextPack(
                task,
                List.of("先检索", "再读取", "生成草案"),
                new TimeCampusRagSearchResult(task, "retriever=lexical", hitCount, hits)
        );
    }
}
