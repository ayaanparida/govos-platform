package com.govos.srh.observability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SearchTraceLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger("com.govos.srh.trace");

    private final SearchObservationProperties properties;

    public SearchTraceLogger(SearchObservationProperties properties) {
        this.properties = properties;
    }

    public void logOperation(SearchTraceLogContext context) {
        if (!properties.isEnabled()) {
            return;
        }
        LOGGER.info(
                "search_trace operation={} status={} durationMs={} traceId={} spanId={} organizationId={} documentCount={} provider={} engine={}",
                safe(context.operation()),
                safe(context.status()),
                context.durationMs(),
                safe(context.traceId()),
                safe(context.spanId()),
                safeUuid(context.organizationId()),
                context.documentCount(),
                safe(context.provider()),
                safe(context.engine()));
    }

    public void logSpan(String spanName, String status, long durationMs, String traceId, String spanId) {
        if (!properties.isEnabled() || !properties.isLogSpans()) {
            return;
        }
        LOGGER.debug(
                "search_span span={} status={} durationMs={} traceId={} spanId={}",
                safe(spanName),
                safe(status),
                durationMs,
                safe(traceId),
                safe(spanId));
    }

    public void logEvent(SearchObservationEventType type, String operation, String traceId) {
        if (!properties.isEnabled() || !properties.isLogEvents()) {
            return;
        }
        LOGGER.debug("search_event type={} operation={} traceId={}", type, safe(operation), safe(traceId));
    }

    private static String safe(String value) {
        return value != null ? value : "-";
    }

    private static String safeUuid(UUID value) {
        return value != null ? value.toString() : "-";
    }
}
