# SRH v1.0.0 — Operations Guide

**Audience:** Platform operations and SRE teams

---

## 1. Service Overview

SRH runs as part of the `govos-api` Spring Boot application. It is stateless at the API layer; state lives in PostgreSQL (metadata) and OpenSearch (documents/vectors).

---

## 2. Health Checks

| Endpoint | Permission | Purpose |
|----------|------------|---------|
| `GET /actuator/health` | Public (platform) | Spring Boot liveness |
| `GET /api/v1/search/admin/health` | SRH_MONITOR | OpenSearch cluster health |
| `GET /api/v1/search/admin/health/operational` | SRH_MONITOR | Production readiness composite |

**Operational health includes:** engine status, node count, shard state, semantic provider health, cache health.

---

## 3. Monitoring Stack

| Source | Path / Metric prefix | Tool |
|--------|---------------------|------|
| Micrometer | `search.*`, `semantic.*`, `scheduler.*` | Prometheus |
| Actuator | `/actuator/prometheus` (prod) | Grafana |
| Observability API | `/admin/metrics`, `/admin/latency` | Custom dashboards |
| Traces | OTLP → collector | Jaeger/Tempo |
| Logs | `com.govos.srh.operation`, `com.govos.srh.trace` | ELK/Loki |

---

## 4. Key Metrics to Alert On

| Metric | Threshold suggestion |
|--------|---------------------|
| `search.errors` | > 1% of `search.requests` |
| `cluster.health` status=DOWN | Any occurrence |
| `scheduler.failures` | > 0 in 1 hour |
| `search.trace.failed` | Sustained increase |
| `embedding.errors` | > 5 in 15 min |
| OpenSearch JVM heap | > 85% |

---

## 5. Scheduled Operations

All jobs run via Spring `@Scheduled` when `govos.search.scheduler.enabled=true`:

| Job | Default schedule |
|-----|------------------|
| Daily full reindex | 02:00 |
| Incremental reindex | Every 6 hours |
| Embedding generation | 03:30 |
| Cleanup (history, vectors, cache) | 04:00 |
| Health verification | Every 15 min |
| Statistics + cache warm | Hourly :05 |

Manual triggers available via `/admin/scheduler/*` (SRH_ADMIN).

---

## 6. Log Fields (Safe)

Structured logs include: `operation`, `status`, `durationMs`, `organizationId`, `requestId`, `traceId`, `spanId`, `documentCount`, `provider`, `engine`.

**Never logged:** search text, document JSON, embeddings, API keys, PII.

---

## 7. Permission Assignment

| Role | Permissions |
|------|-------------|
| Operations viewer | `SRH_MONITOR` |
| Search administrator | `SRH_MONITOR`, `SRH_ADMIN` |
| Reindex operator | `SRH_REINDEX` (+ MONITOR recommended) |

IDM seed data for SRH permissions is external to v1.0 — assign via platform IDM module.

---

## 8. Daily Operations Checklist

- [ ] Verify `/admin/health/operational` returns healthy
- [ ] Check scheduler history for failed jobs
- [ ] Review slow queries (`/admin/queries/slow`)
- [ ] Confirm OpenSearch cluster green/yellow only
- [ ] Verify Prometheus scrape successful
- [ ] Check embedding provider health if semantic enabled

---

## 9. Incident Escalation

1. Check [OPERATIONAL_RUNBOOKS.md](./OPERATIONAL_RUNBOOKS.md)
2. Review `/admin/errors` and `/admin/traces`
3. Check OpenSearch cluster state via `/admin/cluster`
4. Engage platform architecture if product bypass suspected

See [TROUBLESHOOTING_GUIDE.md](./TROUBLESHOOTING_GUIDE.md) for diagnostic procedures.
