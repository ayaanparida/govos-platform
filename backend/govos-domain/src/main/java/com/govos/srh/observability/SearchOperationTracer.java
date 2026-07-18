package com.govos.srh.observability;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

@Component
public class SearchOperationTracer {

    private final SearchObservationProperties properties;
    private final Tracer tracer;
    private final ObservationRegistry observationRegistry;
    private final SearchObservationMetrics metrics;
    private final SearchObservationService observationService;
    private final SearchTraceLogger traceLogger;

    public SearchOperationTracer(
            SearchObservationProperties properties,
            Tracer tracer,
            ObjectProvider<ObservationRegistry> observationRegistryProvider,
            SearchObservationMetrics metrics,
            SearchObservationService observationService,
            SearchTraceLogger traceLogger) {
        this.properties = properties;
        this.tracer = tracer;
        this.observationRegistry = observationRegistryProvider.getIfAvailable(ObservationRegistry::create);
        this.metrics = metrics;
        this.observationService = observationService;
        this.traceLogger = traceLogger;
    }

    public <T> T trace(String spanName, TraceContextAttributes attributes, Supplier<T> action) {
        if (!properties.isEnabled() || !shouldSample()) {
            return action.get();
        }

        long started = System.currentTimeMillis();
        Instant startedAt = Instant.now();
        metrics.recordTraceCreated(spanName);

        Span span = tracer.spanBuilder(spanName)
                .setSpanKind(SpanKind.INTERNAL)
                .startSpan();
        String traceId = span.getSpanContext().getTraceId();
        String spanId = span.getSpanContext().getSpanId();
        SearchTraceContext traceContext = SearchTraceContext.bootstrap(traceId, spanId, SearchTraceContext.current().requestId());
        if (attributes.organizationId() != null) {
            SearchTraceContext.enrich(attributes.organizationId(), traceContext.userId());
        }

        Observation observation = Observation.createNotStarted(spanName, observationRegistry)
                .contextualName(spanName)
                .start();

        publishStartEvent(attributes);
        try (Scope scope = span.makeCurrent()) {
            T result = action.get();
            complete(spanName, span, attributes, started, startedAt, "SUCCESS", attributes.documentCount());
            observation.stop();
            return result;
        } catch (RuntimeException ex) {
            fail(spanName, span, attributes, started, startedAt, ex);
            observation.error(ex);
            observation.stop();
            throw ex;
        } catch (Throwable ex) {
            fail(spanName, span, attributes, started, startedAt, ex);
            observation.error(ex);
            observation.stop();
            throw new SearchObservationException("Observation failed for " + spanName, ex);
        } finally {
            span.end();
            traceContext.close();
        }
    }

    public long traceCount(String spanName, TraceContextAttributes attributes, DocumentCountAction action) {
        if (!properties.isEnabled() || !shouldSample()) {
            try {
                return action.execute();
            } catch (Exception ex) {
                if (ex instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                throw new SearchObservationException("Observation failed for " + spanName, ex);
            }
        }

        long started = System.currentTimeMillis();
        Instant startedAt = Instant.now();
        metrics.recordTraceCreated(spanName);

        Span span = tracer.spanBuilder(spanName)
                .setSpanKind(SpanKind.INTERNAL)
                .startSpan();
        String traceId = span.getSpanContext().getTraceId();
        String spanId = span.getSpanContext().getSpanId();
        SearchTraceContext traceContext = SearchTraceContext.bootstrap(traceId, spanId, SearchTraceContext.current().requestId());
        if (attributes.organizationId() != null) {
            SearchTraceContext.enrich(attributes.organizationId(), traceContext.userId());
        }

        Observation observation = Observation.createNotStarted(spanName, observationRegistry)
                .contextualName(spanName)
                .start();

        publishStartEvent(attributes);
        try (Scope scope = span.makeCurrent()) {
            long documentCount = action.execute();
            complete(spanName, span, attributes, started, startedAt, "SUCCESS", documentCount);
            observation.stop();
            return documentCount;
        } catch (RuntimeException ex) {
            fail(spanName, span, attributes, started, startedAt, ex);
            observation.error(ex);
            observation.stop();
            throw ex;
        } catch (Exception ex) {
            fail(spanName, span, attributes, started, startedAt, ex);
            observation.error(ex);
            observation.stop();
            if (ex instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new SearchObservationException("Observation failed for " + spanName, ex);
        } finally {
            span.end();
            traceContext.close();
        }
    }

    private void complete(
            String spanName,
            Span span,
            TraceContextAttributes attributes,
            long started,
            Instant startedAt,
            String status,
            long documentCount) {
        long durationMs = System.currentTimeMillis() - started;
        span.setStatus(StatusCode.OK);
        metrics.recordTraceCompleted(spanName);
        metrics.recordSpanDuration(spanName, durationMs);
        recordCategoryTrace(spanName, attributes.provider());
        traceLogger.logOperation(new SearchTraceLogContext(
                spanName,
                status,
                durationMs,
                span.getSpanContext().getTraceId(),
                span.getSpanContext().getSpanId(),
                attributes.organizationId(),
                documentCount,
                attributes.provider(),
                attributes.engine()));
        traceLogger.logSpan(spanName, status, durationMs, span.getSpanContext().getTraceId(), span.getSpanContext().getSpanId());
        observationService.recordTrace(new SearchTraceRecord(
                UUID.randomUUID(),
                span.getSpanContext().getTraceId(),
                span.getSpanContext().getSpanId(),
                null,
                spanName,
                status,
                durationMs,
                attributes.organizationId(),
                SearchTraceContext.current().userId(),
                SearchTraceContext.current().requestId(),
                documentCount,
                attributes.provider(),
                attributes.engine(),
                Instant.ofEpochMilli(startedAt.toEpochMilli()),
                Instant.now()));
        publishCompletionEvent(attributes, status, durationMs, documentCount);
    }

    private void fail(
            String spanName,
            Span span,
            TraceContextAttributes attributes,
            long started,
            Instant startedAt,
            Throwable ex) {
        long durationMs = System.currentTimeMillis() - started;
        span.setStatus(StatusCode.ERROR, ex.getMessage());
        span.recordException(ex);
        metrics.recordTraceFailed(spanName);
        metrics.recordSpanDuration(spanName, durationMs);
        traceLogger.logOperation(new SearchTraceLogContext(
                spanName,
                "ERROR",
                durationMs,
                span.getSpanContext().getTraceId(),
                span.getSpanContext().getSpanId(),
                attributes.organizationId(),
                attributes.documentCount(),
                attributes.provider(),
                attributes.engine()));
        observationService.recordTrace(new SearchTraceRecord(
                UUID.randomUUID(),
                span.getSpanContext().getTraceId(),
                span.getSpanContext().getSpanId(),
                null,
                spanName,
                "ERROR",
                durationMs,
                attributes.organizationId(),
                SearchTraceContext.current().userId(),
                SearchTraceContext.current().requestId(),
                attributes.documentCount(),
                attributes.provider(),
                attributes.engine(),
                Instant.ofEpochMilli(startedAt.toEpochMilli()),
                Instant.now()));
        if (attributes.failEvent() != null) {
            observationService.publishEvent(
                    attributes.failEvent(),
                    spanName,
                    "ERROR",
                    durationMs,
                    attributes.organizationId(),
                    attributes.documentCount(),
                    attributes.provider(),
                    attributes.engine());
        }
    }

    private void publishStartEvent(TraceContextAttributes attributes) {
        if (attributes.startEvent() != null) {
            observationService.publishEvent(
                    attributes.startEvent(),
                    attributes.operation(),
                    "STARTED",
                    0L,
                    attributes.organizationId(),
                    attributes.documentCount(),
                    attributes.provider(),
                    attributes.engine());
        }
    }

    private void publishCompletionEvent(
            TraceContextAttributes attributes,
            String status,
            long durationMs,
            long documentCount) {
        if (attributes.completeEvent() != null) {
            observationService.publishEvent(
                    attributes.completeEvent(),
                    attributes.operation(),
                    status,
                    durationMs,
                    attributes.organizationId(),
                    documentCount,
                    attributes.provider(),
                    attributes.engine());
        }
    }

    private void recordCategoryTrace(String spanName, String provider) {
        if (spanName.contains("scheduler")) {
            metrics.recordSchedulerTrace(spanName);
        } else if (spanName.contains("embedding")) {
            metrics.recordEmbeddingTrace(provider != null ? provider : "unknown");
        } else if (spanName.contains("vector")) {
            metrics.recordVectorTrace(spanName);
        } else if (spanName.contains("provider")) {
            metrics.recordProviderTrace(provider != null ? provider : "unknown");
        }
    }

    private boolean shouldSample() {
        double sampleRate = properties.getSampleRate();
        if (sampleRate >= 1.0) {
            return true;
        }
        if (sampleRate <= 0.0) {
            return false;
        }
        return ThreadLocalRandom.current().nextDouble() <= sampleRate;
    }

    public record TraceContextAttributes(
            String operation,
            UUID organizationId,
            long documentCount,
            String provider,
            String engine,
            SearchObservationEventType startEvent,
            SearchObservationEventType completeEvent,
            SearchObservationEventType failEvent) {
    }

    @FunctionalInterface
    public interface DocumentCountAction {
        long execute() throws Exception;
    }
}
