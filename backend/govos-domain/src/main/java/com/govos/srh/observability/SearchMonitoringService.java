package com.govos.srh.observability;

public interface SearchMonitoringService {

    SearchMetricsSnapshotDto getMetricsSnapshot();

    SearchLatencySnapshotDto getLatencySnapshot();

    SearchErrorSnapshotDto getErrorSnapshot();
}
