package com.govos.srh.production;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.govos.srh.config.SearchProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

@Component
public class SearchReadCache {

    private final SearchCacheProperties properties;
    private final Cache<String, Object> cache;
    private final AtomicBoolean healthy = new AtomicBoolean(true);

    public SearchReadCache(SearchProperties searchProperties) {
        this.properties = searchProperties.getCache();
        this.cache = Caffeine.newBuilder()
                .maximumSize(Math.max(1L, properties.getMaxEntries()))
                .expireAfterWrite(Duration.ofSeconds(Math.max(1L, properties.getTtlSeconds())))
                .build();
    }

    @SuppressWarnings("unchecked")
    public <T> T getOrLoad(String key, Supplier<T> loader) {
        if (!properties.isEnabled()) {
            return loader.get();
        }
        try {
            return (T) cache.get(key, ignored -> loader.get());
        } catch (RuntimeException ex) {
            healthy.set(false);
            return loader.get();
        }
    }

    public void put(String key, Object value) {
        if (properties.isEnabled() && value != null) {
            cache.put(key, value);
            healthy.set(true);
        }
    }

    public void evict(String key) {
        cache.invalidate(key);
    }

    public void evictAll() {
        cache.invalidateAll();
        healthy.set(true);
    }

    public long size() {
        return cache.estimatedSize();
    }

    public String healthStatus() {
        return healthy.get() ? "UP" : "DEGRADED";
    }

    public boolean isEnabled() {
        return properties.isEnabled();
    }
}
