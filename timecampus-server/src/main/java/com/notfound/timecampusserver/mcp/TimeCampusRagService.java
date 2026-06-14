package com.notfound.timecampusserver.mcp;

import com.notfound.timecampuspojo.entity.CommentEntity;
import com.notfound.timecampuspojo.vo.MediaVO;
import com.notfound.timecampuspojo.vo.PoiVO;
import com.notfound.timecampusserver.mapper.CommentMapper;
import com.notfound.timecampusserver.service.AdminMediaService;
import com.notfound.timecampusserver.service.PoiService;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TimeCampusRagService {

    private final PoiService poiService;
    private final AdminMediaService adminMediaService;
    private final CommentMapper commentMapper;
    private final TimeCampusMcpAdminScope adminScope;
    private final TimeCampusRagProperties ragProperties;
    private final ObjectProvider<VectorStore> vectorStoreProvider;

    public TimeCampusRagService(PoiService poiService,
                                AdminMediaService adminMediaService,
                                CommentMapper commentMapper,
                                TimeCampusMcpAdminScope adminScope,
                                TimeCampusRagProperties ragProperties,
                                ObjectProvider<VectorStore> vectorStoreProvider) {
        this.poiService = poiService;
        this.adminMediaService = adminMediaService;
        this.commentMapper = commentMapper;
        this.adminScope = adminScope;
        this.ragProperties = ragProperties;
        this.vectorStoreProvider = vectorStoreProvider;
    }

    public TimeCampusRagSearchResult search(String query,
                                            Integer limit,
                                            List<String> types,
                                            Long poiId,
                                            Boolean includePending) {
        return adminScope.call(() -> {
            List<TimeCampusRagDocument> corpus = buildCorpus(types, poiId, includePending);
            int normalizedLimit = normalizeLimit(limit);
            List<TimeCampusRagSearchResult.Hit> hits = vectorSearch(query, normalizedLimit, types, poiId, includePending);
            String retriever = "qdrant";
            if (hits.isEmpty() && ragProperties.isLexicalFallbackEnabled()) {
                hits = lexicalSearch(query, normalizedLimit, corpus);
                retriever = "lexical";
            }
            return new TimeCampusRagSearchResult(
                    query,
                    "retriever=" + retriever + ". Use the returned documents as grounding context. Read the referenced MCP resource/tool before writing, and do not invent dates, locations, people, sources or copyright status.",
                    corpus.size(),
                    hits
            );
        });
    }

    public TimeCampusRagContextPack contextPack(String task,
                                                Integer limit,
                                                List<String> types,
                                                Long poiId,
                                                Boolean includePending) {
        TimeCampusRagSearchResult searchResult = search(task, limit, types, poiId, includePending);
        return new TimeCampusRagContextPack(
                task,
                List.of(
                        "Start from these retrieved documents, then call the concrete read tool/resource for any record that will be changed.",
                        "Use copy-only tools for copy edits and metadata tools only when structured fields must change.",
                        "Ask for human confirmation before delete operations or when source year/location/copyright is uncertain."
                ),
                searchResult
        );
    }

    public TimeCampusRagCorpusSummary corpusSummary() {
        return adminScope.call(() -> {
            List<TimeCampusRagDocument> corpus = collectDocuments(null, null, true);
            Map<String, Long> countsByType = corpus.stream()
                    .collect(Collectors.groupingBy(TimeCampusRagDocument::type, LinkedHashMap::new, Collectors.counting()));
            return new TimeCampusRagCorpusSummary(corpus.size(), countsByType, List.of(
                    "poi",
                    "media",
                    "comment",
                    "guideline"
            ));
        });
    }

    public List<TimeCampusRagDocument> collectDocuments(List<String> types, Long poiId, Boolean includePending) {
        return adminScope.call(() -> buildCorpus(types, poiId, includePending));
    }

    public List<String> collectDocumentIds(List<String> types, Long poiId, Boolean includePending) {
        return collectDocuments(types, poiId, includePending).stream()
                .map(TimeCampusRagDocument::id)
                .toList();
    }

    private List<TimeCampusRagDocument> buildCorpus(List<String> types, Long poiId, Boolean includePending) {
        Set<String> typeSet = normalizeTypes(types);
        boolean withPending = Boolean.TRUE.equals(includePending);
        List<TimeCampusRagDocument> documents = new ArrayList<>();

        if (matchesType(typeSet, "guideline")) {
            documents.add(guidelineDocument());
        }

        List<PoiVO> pois = poiService.list(null, null);
        Map<Long, PoiVO> poisById = pois.stream()
                .collect(Collectors.toMap(PoiVO::getId, poi -> poi, (left, right) -> left, LinkedHashMap::new));

        if (matchesType(typeSet, "poi")) {
            pois.stream()
                    .filter(poi -> poiId == null || poiId.equals(poi.getId()))
                    .map(this::poiDocument)
                    .forEach(documents::add);
        }

        if (matchesType(typeSet, "media")) {
            String reviewStatus = withPending ? null : "approved";
            adminMediaService.list(poiId, null, reviewStatus, null, null)
                    .stream()
                    .map(media -> mediaDocument(media, poisById.get(media.getPoiId())))
                    .forEach(documents::add);
        }

        if (matchesType(typeSet, "comment")) {
            String reviewStatus = withPending ? null : "approved";
            String targetType = poiId == null ? null : "poi";
            commentMapper.listForAdmin(targetType, poiId, reviewStatus)
                    .stream()
                    .map(comment -> commentDocument(comment, poisById))
                    .forEach(documents::add);
        }

        return documents;
    }

    private List<TimeCampusRagSearchResult.Hit> vectorSearch(String query,
                                                            int limit,
                                                            List<String> types,
                                                            Long poiId,
                                                            Boolean includePending) {
        VectorStore vectorStore = vectorStoreProvider.getIfAvailable();
        if (!ragProperties.isVectorEnabled() || vectorStore == null || query == null || query.isBlank()) {
            return List.of();
        }
        try {
            SearchRequest.Builder builder = SearchRequest.builder()
                    .query(query)
                    .topK(limit);
            if (ragProperties.getSimilarityThreshold() > 0) {
                builder.similarityThreshold(ragProperties.getSimilarityThreshold());
            }
            String filterExpression = filterExpression(types, poiId, includePending);
            if (!filterExpression.isBlank()) {
                builder.filterExpression(filterExpression);
            }
            return vectorStore.similaritySearch(builder.build())
                    .stream()
                    .map(this::documentHit)
                    .toList();
        } catch (RuntimeException e) {
            if (!ragProperties.isLexicalFallbackEnabled()) {
                throw e;
            }
            return List.of();
        }
    }

    private List<TimeCampusRagSearchResult.Hit> lexicalSearch(String query,
                                                             int limit,
                                                             List<TimeCampusRagDocument> corpus) {
        List<String> queryTerms = terms(query);
        return corpus.stream()
                .map(document -> score(document, query, queryTerms))
                .filter(hit -> hit.score() > 0)
                .sorted(Comparator.comparingDouble(TimeCampusRagSearchResult.Hit::score).reversed()
                        .thenComparing(hit -> hit.document().id()))
                .limit(limit)
                .toList();
    }

    private TimeCampusRagSearchResult.Hit documentHit(Document document) {
        TimeCampusRagDocument ragDocument = new TimeCampusRagDocument(
                stringMeta(document, "rag_id", document.getId()),
                stringMeta(document, "rag_type", "unknown"),
                stringMeta(document, "title", ""),
                document.getText(),
                stringMeta(document, "uri", ""),
                new LinkedHashMap<>(document.getMetadata())
        );
        double score = document.getScore() == null ? 0.0 : document.getScore();
        return new TimeCampusRagSearchResult.Hit(score, "qdrant similarity", ragDocument);
    }

    private String stringMeta(Document document, String key, String fallback) {
        Object value = document.getMetadata().get(key);
        return value == null ? fallback : String.valueOf(value);
    }

    private String filterExpression(List<String> types, Long poiId, Boolean includePending) {
        List<String> expressions = new ArrayList<>();
        Set<String> typeSet = normalizeTypes(types);
        if (!typeSet.isEmpty()) {
            String joinedTypes = typeSet.stream()
                    .map(type -> "'" + type + "'")
                    .collect(Collectors.joining(", "));
            expressions.add("rag_type in [" + joinedTypes + "]");
        }
        if (poiId != null) {
            expressions.add("poiId == " + poiId);
        }
        if (!Boolean.TRUE.equals(includePending)) {
            expressions.add("(reviewStatus == 'approved' || reviewStatus == '')");
        }
        return String.join(" && ", expressions);
    }

    private TimeCampusRagSearchResult.Hit score(TimeCampusRagDocument document, String query, List<String> queryTerms) {
        if (query == null || query.isBlank()) {
            return new TimeCampusRagSearchResult.Hit(0.1, "empty query fallback", document);
        }
        String normalizedQuery = normalize(query);
        String title = normalize(document.title());
        String text = normalize(document.text());
        double score = 0;
        List<String> reasons = new ArrayList<>();

        if (title.contains(normalizedQuery)) {
            score += 12;
            reasons.add("title contains query");
        }
        if (text.contains(normalizedQuery)) {
            score += 8;
            reasons.add("text contains query");
        }

        for (String term : queryTerms) {
            if (term.length() <= 1 && !isHan(term.charAt(0))) {
                continue;
            }
            if (title.contains(term)) {
                score += 4;
            }
            if (text.contains(term)) {
                score += 1;
            }
        }

        if (score > 0 && reasons.isEmpty()) {
            reasons.add("matched query terms");
        }
        return new TimeCampusRagSearchResult.Hit(score, String.join(", ", reasons), document);
    }

    private TimeCampusRagDocument guidelineDocument() {
        return new TimeCampusRagDocument(
                "guideline:content-maintenance",
                "guideline",
                "TimeCampus content maintenance guidelines",
                "POI required fields: name, latitude, longitude. POI copy fields: description and funFact. "
                        + "Media fields: poiId, imagePath, year, description, reviewStatus. "
                        + "Official media should normally be approved only after source, year, location and usage rights are clear. "
                        + "Agents must read before writing, prefer copy-only tools for copy edits, and require explicit confirmation before deletes.",
                "timecampus://content-guidelines",
                metadata("scope", "global", "reviewStatus", "", "source", "guideline")
        );
    }

    private TimeCampusRagDocument poiDocument(PoiVO poi) {
        return new TimeCampusRagDocument(
                "poi:" + poi.getId(),
                "poi",
                safe(poi.getName()),
                joinLines(
                        "POI " + poi.getId() + ": " + safe(poi.getName()),
                        "status: " + poi.getStatus(),
                        "latitude: " + decimal(poi.getLatitude()),
                        "longitude: " + decimal(poi.getLongitude()),
                        "description: " + safe(poi.getDescription()),
                        "funFact: " + safe(poi.getFunFact())
                ),
                "timecampus://poi/" + poi.getId(),
                metadata(
                        "poiId", poi.getId(),
                        "status", poi.getStatus() == null ? "" : poi.getStatus(),
                        "reviewStatus", "",
                        "source", "mysql"
                )
        );
    }

    private TimeCampusRagDocument mediaDocument(MediaVO media, PoiVO poi) {
        return new TimeCampusRagDocument(
                "media:" + media.getId(),
                "media",
                "Media " + media.getId() + " / " + safe(poi == null ? null : poi.getName()) + " / " + media.getYear(),
                joinLines(
                        "Media " + media.getId(),
                        "poiId: " + media.getPoiId(),
                        "poiName: " + safe(poi == null ? null : poi.getName()),
                        "type: " + safe(media.getType()),
                        "year: " + media.getYear(),
                        "reviewStatus: " + safe(media.getReviewStatus()),
                        "description: " + safe(media.getDescription()),
                        "imagePath: " + safe(media.getImagePath()),
                        "rejectReason: " + safe(media.getRejectReason())
                ),
                "timecampus://media/" + media.getId(),
                metadata(
                        "mediaId", media.getId(),
                        "poiId", media.getPoiId(),
                        "type", safe(media.getType()),
                        "reviewStatus", safe(media.getReviewStatus()),
                        "source", "mysql"
                )
        );
    }

    private TimeCampusRagDocument commentDocument(CommentEntity comment, Map<Long, PoiVO> poisById) {
        String poiName = "";
        if ("poi".equals(comment.getTargetType())) {
            PoiVO poi = poisById.get(comment.getTargetId());
            poiName = poi == null ? "" : safe(poi.getName());
        }
        return new TimeCampusRagDocument(
                "comment:" + comment.getId(),
                "comment",
                "Comment " + comment.getId() + " / " + safe(comment.getTargetType()) + " " + comment.getTargetId(),
                joinLines(
                        "Comment " + comment.getId(),
                        "targetType: " + safe(comment.getTargetType()),
                        "targetId: " + comment.getTargetId(),
                        "poiName: " + poiName,
                        "reviewStatus: " + safe(comment.getReviewStatus()),
                        "content: " + safe(comment.getContent()),
                        "rejectReason: " + safe(comment.getRejectReason())
                ),
                "timecampus://comment/" + comment.getId(),
                metadata(
                        "commentId", comment.getId(),
                        "targetType", safe(comment.getTargetType()),
                        "targetId", comment.getTargetId(),
                        "reviewStatus", safe(comment.getReviewStatus()),
                        "source", "mysql"
                )
        );
    }

    private Set<String> normalizeTypes(List<String> types) {
        if (types == null || types.isEmpty()) {
            return Set.of();
        }
        return types.stream()
                .filter(type -> type != null && !type.isBlank())
                .flatMap(type -> List.of(type.split(",")).stream())
                .map(type -> type.trim().toLowerCase(Locale.ROOT))
                .collect(Collectors.toCollection(HashSet::new));
    }

    private boolean matchesType(Set<String> types, String type) {
        return types == null || types.isEmpty() || types.contains(type);
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return ragProperties.getDefaultTopK();
        }
        return Math.min(limit, ragProperties.getMaxTopK());
    }

    private List<String> terms(String query) {
        String normalized = normalize(query);
        if (normalized.isBlank()) {
            return List.of();
        }
        Set<String> result = new HashSet<>();
        for (String part : normalized.split("[^\\p{IsAlphabetic}\\p{IsDigit}\\p{IsHan}]+")) {
            if (!part.isBlank()) {
                result.add(part);
            }
        }
        for (int i = 0; i < normalized.length(); i++) {
            char current = normalized.charAt(i);
            if (isHan(current)) {
                result.add(String.valueOf(current));
                if (i + 1 < normalized.length() && isHan(normalized.charAt(i + 1))) {
                    result.add(normalized.substring(i, i + 2));
                }
            }
        }
        return new ArrayList<>(result);
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }

    private boolean isHan(char value) {
        return Character.UnicodeScript.of(value) == Character.UnicodeScript.HAN;
    }

    private String decimal(BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String joinLines(String... lines) {
        return String.join("\n", lines);
    }

    private Map<String, Object> metadata(Object... keysAndValues) {
        Map<String, Object> values = new LinkedHashMap<>();
        for (int i = 0; i + 1 < keysAndValues.length; i += 2) {
            Object key = keysAndValues[i];
            Object value = keysAndValues[i + 1];
            if (key != null && value != null) {
                values.put(String.valueOf(key), value);
            }
        }
        return values;
    }

    public record TimeCampusRagDocument(String id,
                                        String type,
                                        String title,
                                        String text,
                                        String uri,
                                        Map<String, Object> metadata) {
    }

    public record TimeCampusRagSearchResult(String query,
                                            String usage,
                                            int corpusSize,
                                            List<Hit> hits) {
        public record Hit(double score, String reason, TimeCampusRagDocument document) {
        }
    }

    public record TimeCampusRagContextPack(String task,
                                           List<String> workflow,
                                           TimeCampusRagSearchResult retrieval) {
    }

    public record TimeCampusRagCorpusSummary(int documentCount,
                                             Map<String, Long> countsByType,
                                             List<String> supportedTypes) {
    }
}
