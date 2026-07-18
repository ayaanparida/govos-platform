package com.govos.srh.scheduler;

import com.govos.srh.config.SearchProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SearchScheduledJobsTest {

    @Test
    void shouldRegisterScheduledTaskComponent() {
        SearchSchedulerService schedulerService = mock(SearchSchedulerService.class);
        SearchScheduledTasks tasks = new SearchScheduledTasks(schedulerService);

        assertThat(tasks).isNotNull();
    }

    @Test
    void shouldDefineAllJobNames() {
        assertThat(SearchScheduledJobNames.DAILY_FULL_REINDEX).isEqualTo("daily-full-reindex");
        assertThat(SearchScheduledJobNames.STATISTICS_AGGREGATION).isEqualTo("statistics-aggregation");
    }
}
