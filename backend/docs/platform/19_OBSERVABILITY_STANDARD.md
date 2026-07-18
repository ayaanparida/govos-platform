# GPS-001 — 19 Observability Standard

---

## 1. Three Pillars

| Pillar | GovOS implementation |
|--------|---------------------|
| **Metrics** | Micrometer → Prometheus |
| **Tracing** | OpenTelemetry OTLP + MDC correlation |
| **Logging** | SLF4J structured logs |

---

## 2. Micrometer Metrics

- Register via `MeterRegistry` — use wrapper recorders per context (e.g. `SearchMetricsRecorder`)
- Enable gate: `{context}.metrics.enabled` configuration
- Naming: `{domain}.{noun}` or `{domain}.{noun}.{verb}`

Examples: `search.requests`, `scheduler.executions`, `embedding.duration`

**Tags:** lowercase; common tags: `operation`, `provider`, `job`, `status`

---

## 3. OpenTelemetry

- API + SDK with OTLP gRPC exporter
- Configuration-driven endpoint: `govos.{context}.observation.otlp-endpoint`
- No-op fallback when disabled
- Sample rate configurable for high-traffic

---

## 4. Correlation IDs

| ID | MDC key | Header |
|----|---------|--------|
| Request ID | `requestId` | `X-Request-ID` |
| Trace ID | `traceId` | `X-Trace-ID` |
| Span ID | `spanId` | `X-Span-ID` |

Set by `CorrelationIdFilter` and context-specific propagation filters.

---

## 5. Structured Logging

Log format (key=value):

```
operation={} status={} durationMs={} organizationId={} requestId={} traceId={}
```

Dedicated loggers per concern:
- `com.govos.{context}.operation` — business operations
- `com.govos.{context}.trace` — tracing metadata

---

## 6. Span Naming

Pattern: `{context}.{operation}` dot-separated

Examples: `search.query`, `search.semantic`, `search.scheduler.execution`

Create spans via AOP or tracer wrapper — **not** inline in business logic.

---

## 7. Observability Package

Platform modules with operational jobs or external engines should include:

```
com.govos.{context}.observability
```

Components: tracer, metrics wrapper, trace context, optional admin snapshot endpoints.

---

## 8. Admin Observability Endpoints

Read-only endpoints under `/admin/` requiring `{CTX}_MONITOR` permission:

- Observability snapshot
- Traces, metrics, latency, errors

---

## 9. Prohibited

- Logging PII, secrets, query text, embeddings
- Custom metrics without naming convention review
- Product modules implementing own tracing bypassing platform MDC

---

## 10. Reference Implementation

SRH SRH-020 — full observability stack certified in Release-1.0.
