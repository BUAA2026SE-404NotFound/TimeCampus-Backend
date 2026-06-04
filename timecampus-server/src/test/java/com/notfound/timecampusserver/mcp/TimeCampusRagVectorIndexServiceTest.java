package com.notfound.timecampusserver.mcp;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.beans.factory.ObjectProvider;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TimeCampusRagVectorIndexServiceTest {

    @Test
    void rebuildChunksSourceDocumentsAndPreservesRagMetadata() {
        TimeCampusRagService ragService = mock(TimeCampusRagService.class);
        VectorStore vectorStore = mock(VectorStore.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<VectorStore> vectorStoreProvider = mock(ObjectProvider.class);
        when(vectorStoreProvider.getIfAvailable()).thenReturn(vectorStore);
        when(ragService.collectDocuments(List.of("media"), 1L, true)).thenReturn(List.of(
                new TimeCampusRagService.TimeCampusRagDocument(
                        "media:10",
                        "media",
                        "Media 10 / 主楼 / 2001",
                        "主楼 2001 年雪景旧照，来源已核验，适合用于游客导览。",
                        "timecampus://media/10",
                        new LinkedHashMap<>(Map.of(
                                "mediaId", 10L,
                                "poiId", 1L,
                                "reviewStatus", "approved",
                                "source", "mysql"
                        ))
                )
        ));

        TimeCampusRagProperties properties = new TimeCampusRagProperties();
        properties.setVectorEnabled(true);
        properties.setChunkMaxChars(12);
        properties.setChunkOverlapChars(3);
        TimeCampusRagVectorIndexService service = new TimeCampusRagVectorIndexService(
                ragService,
                properties,
                vectorStoreProvider
        );

        TimeCampusRagVectorIndexService.VectorIndexResult result = service.rebuild(
                List.of("media"),
                1L,
                true,
                true
        );

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Document>> documentsCaptor = ArgumentCaptor.forClass(List.class);
        verify(vectorStore).delete(any(Filter.Expression.class));
        verify(vectorStore).add(documentsCaptor.capture());
        List<Document> vectorDocuments = documentsCaptor.getValue();

        assertThat(result.status()).isEqualTo("indexed");
        assertThat(result.sourceDocumentCount()).isEqualTo(1);
        assertThat(result.vectorDocumentCount()).isEqualTo(vectorDocuments.size());
        assertThat(vectorDocuments).hasSizeGreaterThan(1);
        assertThat(UUID.fromString(vectorDocuments.get(0).getId())).isNotNull();
        assertThat(vectorDocuments.get(0).getMetadata())
                .containsEntry("rag_id", "media:10#chunk:0")
                .containsEntry("source_id", "media:10")
                .containsEntry("rag_type", "media")
                .containsEntry("uri", "timecampus://media/10")
                .containsEntry("chunk_index", 0)
                .containsEntry("chunk_count", vectorDocuments.size())
                .containsEntry("poiId", 1L)
                .containsEntry("reviewStatus", "approved")
                .containsEntry("source", "mysql");
    }

    @Test
    void rebuildContinuesWhenDeletingMissingCollection() {
        TimeCampusRagService ragService = mock(TimeCampusRagService.class);
        VectorStore vectorStore = mock(VectorStore.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<VectorStore> vectorStoreProvider = mock(ObjectProvider.class);
        when(vectorStoreProvider.getIfAvailable()).thenReturn(vectorStore);
        when(ragService.collectDocuments(List.of("guideline"), null, false)).thenReturn(List.of(
                new TimeCampusRagService.TimeCampusRagDocument(
                        "guideline:content-maintenance",
                        "guideline",
                        "TimeCampus content maintenance guidelines",
                        "Agents must read before writing.",
                        "timecampus://content-guidelines",
                        new LinkedHashMap<>()
                )
        ));
        doThrow(new IllegalStateException("NOT_FOUND: Collection `timecampus_rag` doesn't exist!"))
                .when(vectorStore)
                .delete(any(Filter.Expression.class));
        TimeCampusRagVectorIndexService service = new TimeCampusRagVectorIndexService(
                ragService,
                vectorEnabledProperties(),
                vectorStoreProvider
        );

        TimeCampusRagVectorIndexService.VectorIndexResult result = service.rebuild(
                List.of("guideline"),
                null,
                false,
                true
        );

        assertThat(result.status()).isEqualTo("indexed");
        assertThat(result.sourceDocumentCount()).isEqualTo(1);
        assertThat(result.vectorDocumentCount()).isEqualTo(1);
        verify(vectorStore).add(any());
    }

    @Test
    void rebuildReportsUnavailableWhenVectorStoreIsMissing() {
        TimeCampusRagService ragService = mock(TimeCampusRagService.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<VectorStore> vectorStoreProvider = mock(ObjectProvider.class);
        when(vectorStoreProvider.getIfAvailable()).thenReturn(null);
        TimeCampusRagVectorIndexService service = new TimeCampusRagVectorIndexService(
                ragService,
                vectorEnabledProperties(),
                vectorStoreProvider
        );

        TimeCampusRagVectorIndexService.VectorIndexResult result = service.rebuild(null, null, false, true);

        assertThat(result.status()).isEqualTo("unavailable");
        assertThat(result.sourceDocumentCount()).isZero();
        assertThat(result.vectorDocumentCount()).isZero();
        assertThat(result.message()).contains("No VectorStore bean");
    }

    @Test
    void rebuildReportsDisabledWhenVectorRagIsOff() {
        TimeCampusRagService ragService = mock(TimeCampusRagService.class);
        VectorStore vectorStore = mock(VectorStore.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<VectorStore> vectorStoreProvider = mock(ObjectProvider.class);
        when(vectorStoreProvider.getIfAvailable()).thenReturn(vectorStore);
        TimeCampusRagVectorIndexService service = new TimeCampusRagVectorIndexService(
                ragService,
                new TimeCampusRagProperties(),
                vectorStoreProvider
        );

        TimeCampusRagVectorIndexService.VectorIndexResult result = service.rebuild(null, null, false, true);

        assertThat(result.status()).isEqualTo("disabled");
        assertThat(result.sourceDocumentCount()).isZero();
        assertThat(result.vectorDocumentCount()).isZero();
        assertThat(result.message()).contains("Vector RAG is disabled");
    }

    private TimeCampusRagProperties vectorEnabledProperties() {
        TimeCampusRagProperties properties = new TimeCampusRagProperties();
        properties.setVectorEnabled(true);
        return properties;
    }
}
