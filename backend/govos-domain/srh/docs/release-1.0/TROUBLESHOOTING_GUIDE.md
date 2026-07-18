# SRH v1.0.0 — Troubleshooting Guide

---

## Quick Diagnostics

| Symptom | First check |
|---------|-------------|
| 401 on all endpoints | JWT token validity |
| 403 on admin | SRH_* permission assignment |
| Empty search results | Index exists; documents indexed; org ID correct |
| Slow queries | `/admin/queries/slow`; OpenSearch cluster load |
| Semantic not working | `semantic.enabled=true`; provider configured |
| Scheduler not running | `scheduler.enabled=true`; check pod logs |

---

## Connection Issues

### Cannot connect to OpenSearch

```
SearchEngineHealthStatus: DOWN
```

1. Ping host:port from API container
2. Check firewall/security groups
3. Verify SSL: `govos.search.ssl` matches cluster
4. Test credentials with curl to OpenSearch
5. Review `pool.connection-timeout-ms`

### Connection pool exhausted

```
java.util.concurrent.TimeoutException
```

1. Increase `pool.max-connections` (prod: 100)
2. Check for connection leaks (long-running queries)
3. Reduce concurrent reindex operations

---

## Query Issues

### No results but documents exist

- Verify `organizationId` in query matches document
- Check index code spelling
- Confirm documents not soft-deleted
- Check OpenSearch index directly (ops only)

### Pagination errors

```
Deep pagination not allowed
```

- Offset exceeds `guard.max-deep-pagination-offset` (10000)
- Use cursor/search-after pattern (post v1.0 enhancement)

### Semantic returns empty

- `semantic.enabled` must be `true`
- Embeddings must be generated (`POST /admin/scheduler/embedding`)
- Vector store must be `opensearch` in prod
- Provider must be healthy

---

## Indexing Issues

### Bulk index failures

1. Check `bulk.failures` metric
2. Review batch size: `bulk-batch-size`
3. Verify document payload size limits
4. Check OpenSearch mapping conflicts

### Reindex stuck

1. `GET /jobs/{id}` — check status
2. `POST /admin/jobs/{id}/cancel` then retry
3. Check scheduler history for errors

---

## Scheduler Issues

### Jobs not executing

- Confirm single scheduler owner (one API instance runs cron by default in K8s — use leader election post v1.0 if needed)
- Verify `govos.search.scheduler.enabled=true`
- Check Spring `@Scheduled` not disabled

### Job failures with retries

- Review error in `/admin/scheduler/history`
- Check exponential backoff settings
- Manual retry via admin trigger endpoint

---

## Observability Issues

### No traces in collector

1. Verify `observation.enabled=true`
2. Check `otlp-endpoint` reachable
3. Review `sample-rate` (not 0)
4. Fallback: `/admin/traces` in-memory history

### High error rate in `/admin/errors`

1. Identify top failed operations
2. Cross-reference with OpenSearch logs
3. Check provider health for embedding failures

---

## Log Analysis

| Logger | Content |
|--------|---------|
| `com.govos.srh.operation` | Search operations (no query text) |
| `com.govos.srh.trace` | Distributed trace metadata |
| `com.govos.srh.ai.provider` | Provider selection (no API keys) |

---

## Support Escalation

Include in ticket:
- `requestId` from API response
- `traceId` from response headers
- Timestamp and endpoint
- Organization ID (not document content)
- Output of `/admin/health/operational`
