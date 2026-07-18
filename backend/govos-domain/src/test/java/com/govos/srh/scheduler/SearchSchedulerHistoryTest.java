package com.govos.srh.scheduler;

import com.govos.srh.config.SearchProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SearchSchedulerHistoryTest {

    private SearchSchedulerHistoryStore historyStore;

    @BeforeEach
    void setUp() {
        SearchProperties properties = new SearchProperties();
        properties.getScheduler().setHistoryMaxEntries(10);
        historyStore = new SearchSchedulerHistoryStore(properties);
    }

    @Test
    void shouldStoreAndListExecutionHistory() {
        historyStore.add(new SearchScheduledJobRecord(java.util.UUID.randomUUID(), "cache-warmup"));
        historyStore.add(new SearchScheduledJobRecord(java.util.UUID.randomUUID(), "statistics-aggregation"));

        assertThat(historyStore.list(5)).hasSize(2);
        assertThat(historyStore.totalExecutions()).isEqualTo(2);
    }

    @Test
    void shouldFindLatestFailedJob() {
        SearchScheduledJobRecord failed = new SearchScheduledJobRecord(
                java.util.UUID.randomUUID(), SearchScheduledJobNames.EMBEDDING_GENERATION);
        failed.setStatus(SearchScheduledJobStatus.FAILED);
        historyStore.add(failed);

        assertThat(historyStore.findLatestFailed(SearchScheduledJobNames.EMBEDDING_GENERATION)).isNotNull();
    }
}
