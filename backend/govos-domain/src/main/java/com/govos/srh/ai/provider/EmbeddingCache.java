package com.govos.srh.ai.provider;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.govos.srh.ai.EmbeddingProvider;
import com.govos.srh.config.SearchProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class EmbeddingCache {

    private final com.govos.srh.config.EmbeddingCacheProperties properties;
    private final Cache<String, float[]> cache;

    public EmbeddingCache(SearchProperties searchProperties) {
        this.properties = searchProperties.getSemantic().getEmbeddingCache();
        this.cache = Caffeine.newBuilder()
                .maximumSize(Math.max(1L, properties.getMaxEntries()))
                .expireAfterWrite(Duration.ofSeconds(Math.max(1L, properties.getTtlSeconds())))
                .build();
    }

    public float[] get(String provider, String model, String text) {
        if (!properties.isEnabled() || text == null) {
            return null;
        }
        return cache.getIfPresent(cacheKey(provider, model, text));
    }

    public void put(String provider, String model, String text, float[] embedding) {
        if (!properties.isEnabled() || text == null || embedding == null) {
            return;
        }
        cache.put(cacheKey(provider, model, text), embedding);
    }

    public List<float[]> getBatch(String provider, String model, List<String> texts) {
        if (!properties.isEnabled() || texts == null || texts.isEmpty()) {
            return null;
        }
        List<float[]> cached = new ArrayList<>(texts.size());
        boolean complete = true;
        for (String text : texts) {
            float[] vector = cache.getIfPresent(cacheKey(provider, model, text));
            if (vector == null) {
                complete = false;
                break;
            }
            cached.add(vector);
        }
        return complete ? cached : null;
    }

    public void putBatch(String provider, String model, List<String> texts, List<float[]> embeddings) {
        if (!properties.isEnabled() || texts == null || embeddings == null) {
            return;
        }
        int limit = Math.min(texts.size(), embeddings.size());
        for (int i = 0; i < limit; i++) {
            put(provider, model, texts.get(i), embeddings.get(i));
        }
    }

    public void evictAll() {
        cache.invalidateAll();
    }

    public long size() {
        return cache.estimatedSize();
    }

    public boolean isEnabled() {
        return properties.isEnabled();
    }

    static String cacheKey(String provider, String model, String text) {
        return provider + ":" + model + ":" + sha256(text);
    }

    private static String sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte value : hash) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }
}
