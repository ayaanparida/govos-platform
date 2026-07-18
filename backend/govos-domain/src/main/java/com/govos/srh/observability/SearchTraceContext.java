package com.govos.srh.observability;

import java.util.Optional;
import java.util.UUID;
import org.slf4j.MDC;

public final class SearchTraceContext {

    public static final String MDC_TRACE_ID = "traceId";
    public static final String MDC_SPAN_ID = "spanId";
    public static final String MDC_REQUEST_ID = "requestId";
    public static final String MDC_ORGANIZATION_ID = "organizationId";
    public static final String MDC_USER_ID = "userId";

    private static final ThreadLocal<SearchTraceContext> CURRENT = new ThreadLocal<>();

    private final String traceId;
    private final String spanId;
    private final String parentSpanId;
    private final String requestId;
    private final UUID organizationId;
    private final String userId;

    private SearchTraceContext(
            String traceId,
            String spanId,
            String parentSpanId,
            String requestId,
            UUID organizationId,
            String userId) {
        this.traceId = traceId;
        this.spanId = spanId;
        this.parentSpanId = parentSpanId;
        this.requestId = requestId;
        this.organizationId = organizationId;
        this.userId = userId;
    }

    public static SearchTraceContext current() {
        SearchTraceContext context = CURRENT.get();
        if (context != null) {
            return context;
        }
        return fromMdc();
    }

    public static Optional<SearchTraceContext> currentOptional() {
        SearchTraceContext context = CURRENT.get();
        return context != null ? Optional.of(context) : Optional.empty();
    }

    public static SearchTraceContext startSpan(String traceId, String spanId, String parentSpanId) {
        SearchTraceContext parent = current();
        SearchTraceContext child = new SearchTraceContext(
                traceId != null ? traceId : parent.traceId(),
                spanId,
                parentSpanId,
                parent.requestId(),
                parent.organizationId(),
                parent.userId());
        child.install();
        return child;
    }

    public static SearchTraceContext bootstrap(String traceId, String spanId, String requestId) {
        SearchTraceContext context = new SearchTraceContext(
                traceId,
                spanId,
                null,
                requestId != null ? requestId : MDC.get(MDC_REQUEST_ID),
                parseUuid(MDC.get(MDC_ORGANIZATION_ID)),
                MDC.get(MDC_USER_ID));
        context.install();
        return context;
    }

    public static void enrich(UUID organizationId, String userId) {
        if (organizationId != null) {
            MDC.put(MDC_ORGANIZATION_ID, organizationId.toString());
        }
        if (userId != null && !userId.isBlank()) {
            MDC.put(MDC_USER_ID, userId);
        }
        SearchTraceContext context = CURRENT.get();
        if (context != null) {
            CURRENT.set(new SearchTraceContext(
                    context.traceId(),
                    context.spanId(),
                    context.parentSpanId(),
                    context.requestId(),
                    organizationId != null ? organizationId : context.organizationId(),
                    userId != null ? userId : context.userId()));
        }
    }

    public void install() {
        CURRENT.set(this);
        if (traceId != null) {
            MDC.put(MDC_TRACE_ID, traceId);
        }
        if (spanId != null) {
            MDC.put(MDC_SPAN_ID, spanId);
        }
        if (requestId != null) {
            MDC.put(MDC_REQUEST_ID, requestId);
        }
        if (organizationId != null) {
            MDC.put(MDC_ORGANIZATION_ID, organizationId.toString());
        }
        if (userId != null) {
            MDC.put(MDC_USER_ID, userId);
        }
    }

    public void close() {
        CURRENT.remove();
        MDC.remove(MDC_SPAN_ID);
        if (parentSpanId == null) {
            MDC.remove(MDC_TRACE_ID);
        } else {
            MDC.put(MDC_SPAN_ID, parentSpanId);
        }
    }

    public String traceId() {
        return traceId;
    }

    public String spanId() {
        return spanId;
    }

    public String parentSpanId() {
        return parentSpanId;
    }

    public String requestId() {
        return requestId;
    }

    public UUID organizationId() {
        return organizationId;
    }

    public String userId() {
        return userId;
    }

    private static SearchTraceContext fromMdc() {
        return new SearchTraceContext(
                MDC.get(MDC_TRACE_ID),
                MDC.get(MDC_SPAN_ID),
                null,
                MDC.get(MDC_REQUEST_ID),
                parseUuid(MDC.get(MDC_ORGANIZATION_ID)),
                MDC.get(MDC_USER_ID));
    }

    private static UUID parseUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
