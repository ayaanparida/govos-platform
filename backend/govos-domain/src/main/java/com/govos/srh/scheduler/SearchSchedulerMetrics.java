package com.govos.srh.scheduler;

import com.govos.srh.config.SearchProperties;
import com.govos.srh.production.SearchMetricsRecorder;
import org.springframework.stereotype.Component;

@Component
public class SearchSchedulerMetrics {

    private final SearchMetricsRecorder metricsRecorder;
    private final SearchSchedulerProperties properties;

    public SearchSchedulerMetrics(SearchProperties searchProperties, SearchMetricsRecorder metricsRecorder) {
        this.properties = searchProperties.getScheduler();
        this.metricsRecorder = metricsRecorder;
    }

    public void recordExecution(String jobName) {
        metricsRecorder.recordSchedulerExecution(jobName);
    }

    public void recordDuration(String jobName, long durationMs) {
        metricsRecorder.recordSchedulerDuration(durationMs, jobName);
    }

    public void recordFailure(String jobName) {
        metricsRecorder.recordSchedulerFailure(jobName);
    }

    public void recordRetry(String jobName) {
        metricsRecorder.recordSchedulerRetry(jobName);
    }

    public void recordSkipped(String jobName) {
        metricsRecorder.recordSchedulerSkipped(jobName);
    }

    public SearchSchedulerProperties properties() {
        return properties;
    }
}
