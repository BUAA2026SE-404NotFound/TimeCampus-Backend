package com.notfound.timecampusserver.mcp;

import com.notfound.timecampuspojo.entity.CommentEntity;
import com.notfound.timecampuspojo.vo.MediaVO;
import com.notfound.timecampuspojo.vo.PoiVO;
import com.notfound.timecampusserver.mapper.CommentMapper;
import com.notfound.timecampusserver.service.AdminMediaService;
import com.notfound.timecampusserver.service.PoiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TimeCampusRagServiceTest {

    private PoiService poiService;
    private AdminMediaService adminMediaService;
    private CommentMapper commentMapper;
    private TimeCampusRagService ragService;

    @BeforeEach
    void setUp() {
        poiService = mock(PoiService.class);
        adminMediaService = mock(AdminMediaService.class);
        commentMapper = mock(CommentMapper.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<VectorStore> vectorStoreProvider = mock(ObjectProvider.class);
        when(vectorStoreProvider.getIfAvailable()).thenReturn(null);

        TimeCampusRagProperties ragProperties = new TimeCampusRagProperties();
        ragProperties.setVectorEnabled(true);
        ragProperties.setLexicalFallbackEnabled(true);
        ragProperties.setDefaultTopK(4);
        ragProperties.setMaxTopK(8);

        TimeCampusMcpProperties mcpProperties = new TimeCampusMcpProperties();
        mcpProperties.setAdminId(99L);
        mcpProperties.setAdminRole("admin");

        ragService = new TimeCampusRagService(
                poiService,
                adminMediaService,
                commentMapper,
                new TimeCampusMcpAdminScope(mcpProperties),
                ragProperties,
                vectorStoreProvider
        );
    }

    @Test
    void searchBuildsGroundedCorpusFromMysqlDataWithLexicalFallback() {
        PoiVO poi = poi(1L, "主楼", "北航主楼是校园地标", "夜晚灯光很适合拍照");
        MediaVO media = media(10L, 1L, "approved", "主楼 2001 年雪景旧照");
        CommentEntity comment = comment(20L, 1L, "approved", "学生提到主楼前广场适合作为集合点");
        when(poiService.list(null, null)).thenReturn(List.of(poi));
        when(adminMediaService.list(null, null, "approved", null, null)).thenReturn(List.of(media));
        when(commentMapper.listForAdmin(null, null, "approved")).thenReturn(List.of(comment));

        TimeCampusRagService.TimeCampusRagSearchResult result = ragService.search(
                "主楼 雪景",
                5,
                List.of("poi", "media", "comment"),
                null,
                false
        );

        assertThat(result.usage()).contains("retriever=lexical");
        assertThat(result.corpusSize()).isEqualTo(3);
        assertThat(result.hits()).isNotEmpty();
        assertThat(result.hits().get(0).document().uri()).isEqualTo("timecampus://media/10");
        assertThat(result.hits())
                .extracting(hit -> hit.document().id())
                .contains("poi:1", "media:10", "comment:20");
    }

    @Test
    void collectDocumentsFiltersByPoiTypeAndReviewStatus() {
        PoiVO firstPoi = poi(1L, "主楼", "主楼文案", "主楼故事");
        PoiVO secondPoi = poi(2L, "图书馆", "图书馆文案", "图书馆故事");
        MediaVO approved = media(10L, 1L, "approved", "可检索影像");
        MediaVO pending = media(11L, 1L, "pending", "待审核影像");
        when(poiService.list(null, null)).thenReturn(List.of(firstPoi, secondPoi));
        when(adminMediaService.list(eq(1L), eq(null), eq("approved"), eq(null), eq(null)))
                .thenReturn(List.of(approved));
        when(adminMediaService.list(eq(1L), eq(null), eq(null), eq(null), eq(null)))
                .thenReturn(List.of(approved, pending));
        when(commentMapper.listForAdmin(eq("poi"), eq(1L), eq("approved"))).thenReturn(List.of());
        when(commentMapper.listForAdmin(eq("poi"), eq(1L), eq(null))).thenReturn(List.of());

        List<TimeCampusRagService.TimeCampusRagDocument> approvedOnly = ragService.collectDocuments(
                List.of("poi", "media"),
                1L,
                false
        );
        List<TimeCampusRagService.TimeCampusRagDocument> withPending = ragService.collectDocuments(
                List.of("media"),
                1L,
                true
        );

        assertThat(approvedOnly)
                .extracting(TimeCampusRagService.TimeCampusRagDocument::id)
                .containsExactly("poi:1", "media:10");
        assertThat(withPending)
                .extracting(TimeCampusRagService.TimeCampusRagDocument::id)
                .containsExactly("media:10", "media:11");
    }

    @Test
    void corpusSummaryIncludesGuidelineAndMysqlSources() {
        when(poiService.list(null, null)).thenReturn(List.of(poi(1L, "主楼", "主楼文案", "主楼故事")));
        when(adminMediaService.list(null, null, null, null, null))
                .thenReturn(List.of(media(10L, 1L, "pending", "待审核影像")));
        when(commentMapper.listForAdmin(null, null, null))
                .thenReturn(List.of(comment(20L, 1L, "pending", "待审核评论")));

        TimeCampusRagService.TimeCampusRagCorpusSummary summary = ragService.corpusSummary();

        assertThat(summary.documentCount()).isEqualTo(4);
        assertThat(summary.countsByType())
                .containsEntry("guideline", 1L)
                .containsEntry("poi", 1L)
                .containsEntry("media", 1L)
                .containsEntry("comment", 1L);
    }

    @Test
    void searchFusesSemanticRanksWithoutGenericCampusTermsAndDeduplicatesSources() {
        VectorStore vectorStore = mock(VectorStore.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<VectorStore> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(vectorStore);
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(
                vectorDocument("poi:9042", "北航医院", 0.92),
                vectorDocument("poi:9020", "体育馆", 0.88),
                vectorDocument("poi:9005", "学院路校门", 0.84),
                vectorDocument("poi:9005", "学院路校门 duplicate chunk", 0.80)
        ));
        when(poiService.list(null, null)).thenReturn(List.of(
                poi(9005L, "学院路校门", "学院路一侧的校园入口", ""),
                poi(9042L, "北航医院", "同学们看病开假条的地方", ""),
                poi(9020L, "体育馆", "校内综合性体育场馆", "")
        ));

        TimeCampusRagProperties properties = new TimeCampusRagProperties();
        properties.setVectorEnabled(true);
        properties.setLexicalFallbackEnabled(true);
        properties.setDefaultTopK(5);
        properties.setMaxTopK(20);
        TimeCampusMcpProperties mcpProperties = new TimeCampusMcpProperties();
        mcpProperties.setAdminId(99L);
        mcpProperties.setAdminRole("admin");
        TimeCampusRagService service = new TimeCampusRagService(
                poiService,
                adminMediaService,
                commentMapper,
                new TimeCampusMcpAdminScope(mcpProperties),
                properties,
                provider
        );

        TimeCampusRagService.TimeCampusRagSearchResult result = service.search(
                "校内看病和医疗服务地点", 5, List.of("poi"), null, false);

        assertThat(result.usage()).contains("retriever=hybrid-rrf");
        assertThat(result.hits().get(0).document().id()).isEqualTo("poi:9042");
        assertThat(result.hits().get(0).reason()).contains("lexical rank 1", "qdrant rank 1");
        assertThat(result.hits())
                .extracting(hit -> hit.document().id())
                .doesNotHaveDuplicates();
    }

    private Document vectorDocument(String sourceId, String text, double score) {
        return Document.builder()
                .id(sourceId + "#vector")
                .text(text)
                .metadata("source_id", sourceId)
                .metadata("rag_id", sourceId + "#chunk:0")
                .metadata("rag_type", "poi")
                .metadata("title", text)
                .metadata("uri", "timecampus://" + sourceId.replace(':', '/'))
                .score(score)
                .build();
    }

    private PoiVO poi(Long id, String name, String description, String funFact) {
        PoiVO poi = new PoiVO();
        poi.setId(id);
        poi.setName(name);
        poi.setStatus(1);
        poi.setLatitude(new BigDecimal("39.981"));
        poi.setLongitude(new BigDecimal("116.340"));
        poi.setDescription(description);
        poi.setFunFact(funFact);
        return poi;
    }

    private MediaVO media(Long id, Long poiId, String reviewStatus, String description) {
        MediaVO media = new MediaVO();
        media.setId(id);
        media.setPoiId(poiId);
        media.setType("official");
        media.setYear(2001);
        media.setReviewStatus(reviewStatus);
        media.setDescription(description);
        media.setImagePath("/home/ubuntu/cos/" + id + ".jpg");
        return media;
    }

    private CommentEntity comment(Long id, Long poiId, String reviewStatus, String content) {
        CommentEntity comment = new CommentEntity();
        comment.setId(id);
        comment.setTargetType("poi");
        comment.setTargetId(poiId);
        comment.setReviewStatus(reviewStatus);
        comment.setContent(content);
        return comment;
    }
}
