package com.govos.srh.ai;

import com.govos.srh.query.SearchResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class HybridRankingServiceImpl implements HybridRankingService {

    @Override
    public List<SearchResult> rank(
            List<SearchResult> keywordResults,
            List<VectorSearchHit> vectorHits,
            double keywordWeight,
            double semanticWeight) {
        Map<String, Double> keywordScores = normalizeScores(extractKeywordScores(keywordResults));
        Map<String, Double> vectorScores = normalizeScores(extractVectorScores(vectorHits));

        Map<String, SearchResult> keywordById = new HashMap<>();
        if (keywordResults != null) {
            for (SearchResult result : keywordResults) {
                keywordById.putIfAbsent(result.id(), result);
            }
        }

        Map<String, Map<String, Object>> vectorSources = new HashMap<>();
        if (vectorHits != null) {
            for (VectorSearchHit hit : vectorHits) {
                vectorSources.putIfAbsent(hit.id(), hit.metadata());
            }
        }

        LinkedHashMap<String, Double> combinedScores = new LinkedHashMap<>();
        for (String id : unionIds(keywordScores, vectorScores)) {
            double keywordScore = keywordScores.getOrDefault(id, 0D);
            double vectorScore = vectorScores.getOrDefault(id, 0D);
            combinedScores.put(id, (keywordWeight * keywordScore) + (semanticWeight * vectorScore));
        }

        List<Map.Entry<String, Double>> sorted = combinedScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .toList();

        List<SearchResult> ranked = new ArrayList<>(sorted.size());
        for (Map.Entry<String, Double> entry : sorted) {
            String id = entry.getKey();
            SearchResult keywordResult = keywordById.get(id);
            Map<String, Object> source = keywordResult != null
                    ? keywordResult.source()
                    : vectorSources.getOrDefault(id, Map.of());
            ranked.add(new SearchResult(
                    id,
                    entry.getValue(),
                    source,
                    keywordResult != null ? keywordResult.highlights() : null));
        }
        return ranked;
    }

    private static Map<String, Double> extractKeywordScores(List<SearchResult> keywordResults) {
        Map<String, Double> scores = new LinkedHashMap<>();
        if (keywordResults == null) {
            return scores;
        }
        for (SearchResult result : keywordResults) {
            scores.put(result.id(), result.score() != null ? result.score() : 0D);
        }
        return scores;
    }

    private static Map<String, Double> extractVectorScores(List<VectorSearchHit> vectorHits) {
        Map<String, Double> scores = new LinkedHashMap<>();
        if (vectorHits == null) {
            return scores;
        }
        for (VectorSearchHit hit : vectorHits) {
            scores.put(hit.id(), hit.similarityScore());
        }
        return scores;
    }

    private static Map<String, Double> normalizeScores(Map<String, Double> scores) {
        if (scores.isEmpty()) {
            return Map.of();
        }
        double min = scores.values().stream().mapToDouble(Double::doubleValue).min().orElse(0D);
        double max = scores.values().stream().mapToDouble(Double::doubleValue).max().orElse(0D);
        double range = max - min;
        Map<String, Double> normalized = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : scores.entrySet()) {
            if (range == 0D) {
                normalized.put(entry.getKey(), entry.getValue() > 0D ? 1D : 0D);
            } else {
                normalized.put(entry.getKey(), (entry.getValue() - min) / range);
            }
        }
        return normalized;
    }

    private static List<String> unionIds(Map<String, Double> keywordScores, Map<String, Double> vectorScores) {
        LinkedHashMap<String, Boolean> ids = new LinkedHashMap<>();
        keywordScores.keySet().forEach(id -> ids.put(id, Boolean.TRUE));
        vectorScores.keySet().forEach(id -> ids.put(id, Boolean.TRUE));
        return new ArrayList<>(ids.keySet());
    }
}
