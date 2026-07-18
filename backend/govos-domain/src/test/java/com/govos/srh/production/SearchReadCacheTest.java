package com.govos.srh.production;

import com.govos.srh.config.SearchProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SearchReadCacheTest {

    @Test
    void shouldCacheLoadedValues() {
        SearchProperties properties = new SearchProperties();
        properties.getCache().setEnabled(true);
        SearchReadCache cache = new SearchReadCache(properties);

        String first = cache.getOrLoad("key", () -> "value");
        String second = cache.getOrLoad("key", () -> "other");

        assertThat(first).isEqualTo("value");
        assertThat(second).isEqualTo("value");
        assertThat(cache.size()).isEqualTo(1);
    }

    @Test
    void shouldBypassCacheWhenDisabled() {
        SearchProperties properties = new SearchProperties();
        properties.getCache().setEnabled(false);
        SearchReadCache cache = new SearchReadCache(properties);

        assertThat(cache.getOrLoad("key", () -> "one")).isEqualTo("one");
        assertThat(cache.getOrLoad("key", () -> "two")).isEqualTo("two");
        assertThat(cache.size()).isZero();
    }

    @Test
    void shouldEvictEntries() {
        SearchProperties properties = new SearchProperties();
        SearchReadCache cache = new SearchReadCache(properties);
        cache.getOrLoad("key", () -> "value");
        cache.evict("key");
        assertThat(cache.size()).isZero();
    }
}
