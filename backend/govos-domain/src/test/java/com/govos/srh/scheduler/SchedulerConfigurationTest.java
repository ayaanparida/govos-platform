package com.govos.srh.scheduler;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.EnableScheduling;

import static org.assertj.core.api.Assertions.assertThat;

class SchedulerConfigurationTest {

    @Test
    void shouldEnableScheduling() {
        assertThat(SearchSchedulerConfiguration.class.isAnnotationPresent(EnableScheduling.class)).isTrue();
    }

    @Test
    void shouldExposeSchedulerProperties() {
        SearchSchedulerProperties properties = new SearchSchedulerProperties();

        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getReindexCron()).isEqualTo("0 0 2 * * *");
        assertThat(properties.getMaxRetries()).isEqualTo(3);
    }
}
