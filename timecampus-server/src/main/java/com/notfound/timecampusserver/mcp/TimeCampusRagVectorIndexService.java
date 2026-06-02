package com.notfound.timecampusserver.mcp;

import com.notfound.timecampusserver.mcp.TimeCampusRagService.TimeCampusRagDocument;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TimeCampusRagVectorIndexService {

    private final TimeCampusRagService ragService;
    private final TimeCampusRagProperties ragProperties;
    private final ObjectProvider<VectorStore> vectorStoreProvider;

    public TimeCampusRagVectorIndexService(TimeCampusRagService ragService,
                                           TimeCampusRagProperties ragProperties,
                                           ObjectProvider<VectorStore> vectorStoreProvider) {
        this.ragService = ragService;
        this.ragProperties = ragProperties;
        this.vectorStoreProvider = vectorStoreProvider;
    }

    public VectorIndexResult rebuild(List<String> types,
                                     Long poiId,
                                     Boolean includePending,
                                     Boolean deleteExisting) {
        VectorStore vectorStore = vectorStoreProvider.getIfAvailable();
        if (vectorStore == null) {
            return new VectorIndexResult("unavailable", 0, 0,
                    "No VectorStore bean is available. Configure Qdrant plus an EmbeddingModel bean first.");
        }

        List<TimeCampusRagDocument> sourceDocuments = ragService.collectDocuments(types, poiId, includePending);
        if (Boolean.TRUE.equals(deleteExisting) && !sourceDocuments.isEmpty()) {
            deleteSources(vectorStore, sourceDocuments);
        }

        List<Document> vectorDocuments = sourceDocuments.stream()
                .flatMap(source -> toVectorDocuments(source).stream())
                .toList();
        if (!vectorDocuments.isEmpty()) {
            vectorStore.add(vectorDocuments);
        }
        return new VectorIndexResult("indexed", sourceDocuments.size(), vectorDocuments.size(),
                "Indexed TimeCampus RAG documents into Qdrant at " + LocalDateTime.now());
    }

    private void deleteSources(VectorStore vectorStore, List<TimeCampusRagDocument> documents) {
        List<Object> ids = documents.stream()
                .map(TimeCampusRagDocument::id)
                .map(Object.class::cast)
                .toList();
        FilterExpressionBuilder builder = new FilterExpressionBuilder();
        vectorStore.delete(builder.in("source_id", ids).build());
    }

    private List<Document> toVectorDocuments(TimeCampusRagDocument source) {
        List<String> chunks = chunks(source.text());
        List<Document> documents = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            String chunkId = source.id() + "#chunk:" + i;
            Map<String, Object> metadata = new LinkedHashMap<>(source.metadata());
            metadata.put("rag_id", chunkId);
            metadata.put("source_id", source.id());
            metadata.put("rag_type", source.type());
            metadata.put("title", source.title());
            metadata.put("uri", source.uri());
            metadata.put("chunk_index", i);
            metadata.put("chunk_count", chunks.size());
            metadata.putIfAbsent("reviewStatus", "");
            metadata.putIfAbsent("source", "mysql");
            documents.add(new Document(chunkId, chunks.get(i), metadata));
        }
        return documents;
    }

    private List<String> chunks(String text) {
        if (text == null || text.isBlank()) {
            return List.of("");
        }
        int max = ragProperties.getChunkMaxChars();
        int overlap = Math.min(ragProperties.getChunkOverlapChars(), Math.max(0, max - 1));
        if (text.length() <= max) {
            return List.of(text);
        }
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(text.length(), start + max);
            chunks.add(text.substring(start, end));
            if (end == text.length()) {
                break;
            }
            start = Math.max(0, end - overlap);
        }
        return chunks;
    }

    public record VectorIndexResult(String status, int sourceDocumentCount, int vectorDocumentCount, String message) {
    }
}
