package com.govos.srh.observability;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class SearchMonitoringServiceImpl implements SearchMonitoringService {

    private final SearchObservationEventStore eventStore;
    private final MeterRegistry meterRegistry;
    private final boolean enabled;

    public SearchMonitoringServiceImpl(
            SearchObservationEventStore eventStore,
            ObjectProvider<MeterRegistry> meterRegistryProvider,
            SearchObservationProperties properties) {
        this.eventStore = eventStore;
        this.meterRegistry = meterRegistryProvider.getIfAvailable();
        this.enabled = properties.isEnabled() && meterRegistry != null;
    }

    @Override
    public SearchMetricsSnapshotDto getMetricsSnapshot() {
        List<SearchTraceRecord> traces = eventStore.recentTraces(500);
        long completed = traces.stream().filter(t -> "SUCCESS".equals(t.status())).count();
        long failed = traces.stream().filter(t -> "ERROR".equals(t.status())).count();
        long total = traces.size();
        double errorRate = total == 0 ? 0.0 : (double) failed / total;
        double throughput = total == 0 ? 0.0 : total / 60.0;

        return new SearchMetricsSnapshotDto(
                total,
                completed,
                failed,
                eventStore.eventCount(),
                errorRate,
                throughput);
    }

    @Override
    public SearchLatencySnapshotDto getLatencySnapshot() {
        return new SearchLatencySnapshotDto(
                timerMean("search.span.duration", "search.query"),
                timerMean("search.span.duration", "search.semantic"),
                timerMean("embedding.duration", null),
                timerMean("search.span.duration", "search.bulk.index"),
                timerMean("scheduler.duration", null),
                timerMean("embedding.duration", null));
    }

    @Override
    public SearchErrorSnapshotDto getErrorSnapshot() {
        List<SearchTraceRecord> traces = eventStore.recentTraces(500);
        long failed = traces.stream().filter(t -> "ERROR".equals(t.status())).count();
        long total = traces.size();
        double errorRate = total == 0 ? 0.0 : (double) failed / total;

        Map<String, Long> failuresByOperation = traces.stream()
                .filter(t -> "ERROR".equals(t.status()))
                .collect(Collectors.groupingBy(SearchTraceRecord::operation, Collectors.counting()));

        List<String> topFailed = failuresByOperation.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .map(e -> e.getKey() + ":" + e.getValue())
                .toList();

        return new SearchErrorSnapshotDto(failed, errorRate, topFailed);
    }

    private double timerMean(String metricName, String operationTag) {
        if (!enabled) {
            return 0.0;
        }
        return meterRegistry.find(metricName)
                .timers()
                .stream()
                .filter(timer -> operationTag == null || operationTag.equals(timer.getId().getTag("operation")))
                .mapToDouble(timer -> timer.mean(TimeUnit.MILLISECONDS))
                .average()
                .orElse(0.0);
    }
}
