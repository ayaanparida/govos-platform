# SRH v1.0.0 — Operational Runbooks

---

## RB-01: OpenSearch Cluster Down

**Symptoms:** `/admin/health` returns DOWN; queries fail; `search.errors` spike.

**Steps:**
1. Verify OpenSearch pods/nodes: `GET /_cluster/health`
2. Check network from `govos-api` to OpenSearch host/port
3. Verify credentials: `GOVOS_SEARCH_USERNAME`, `GOVOS_SEARCH_PASSWORD`
4. Confirm TLS setting matches cluster: `govos.search.ssl`
5. If graceful degradation enabled, keyword search may return cached/stale results
6. Restore OpenSearch cluster; SRH auto-recovers on reconnect
7. Trigger health verification: `POST /admin/scheduler/cache` or wait for health cron

**Recovery validation:** `GET /admin/health/operational` → engine UP

---

## RB-02: OpenSearch Node Failure

**Symptoms:** Cluster YELLOW; `relocatingShards` > 0; elevated latency.

**Steps:**
1. `GET /admin/cluster` — identify failed node
2. Replace/restart failed node in OpenSearch cluster
3. Wait for shard reallocation
4. Monitor until cluster GREEN or acceptable YELLOW
5. No SRH restart required unless connection pool exhausted

---

## RB-03: Index Corruption

**Symptoms:** Query errors on specific index; mapping conflicts; missing documents.

**Steps:**
1. Identify affected index: `GET /admin/indexes/{id}/statistics`
2. Archive corrupted physical index in OpenSearch (ops team)
3. Trigger reindex: `POST /admin/indexes/{id}/reindex` (SRH_REINDEX)
4. Monitor sync job: `GET /jobs/{id}`
5. Validate document count matches PostgreSQL metadata
6. If alias involved, verify alias points to new physical index

---

## RB-04: Alias Switch Failure

**Symptoms:** Reindex completes but alias not switched; dual-write issues.

**Steps:**
1. Check job status and error message in sync job record
2. Verify new physical index exists in OpenSearch
3. Manually switch alias in OpenSearch if needed (ops)
4. Re-run: `POST /admin/indexes/{id}/reindex`
5. Confirm via `GET /aliases/{aliasName}`

---

## RB-05: Full Platform Reindex

**Symptoms:** Planned maintenance; schema migration; disaster recovery.

**Steps:**
1. Schedule maintenance window
2. `POST /admin/reindex-all` (SRH_REINDEX) OR `POST /admin/scheduler/reindex?full=true` (SRH_ADMIN)
3. Monitor running jobs: dashboard or scheduler history
4. Validate each index document count
5. Run smoke queries per product index

**Duration estimate:** ~1 min per 10K documents (see PERFORMANCE_REPORT.md)

---

## RB-06: Embedding Migration

**Symptoms:** Model change; `embedding-version` increment; vector dimension change.

**Steps:**
1. Update provider config and `embedding-version`
2. Set `semantic.vector-store=opensearch`
3. Trigger: `POST /admin/scheduler/embedding` (SRH_ADMIN)
4. Monitor embedding job via scheduler history
5. Validate vector count: `/admin/semantic/provider`
6. Run hybrid search smoke tests

---

## RB-07: Cache Corruption / Stale Results

**Symptoms:** Outdated search results; cache health DEGRADED.

**Steps:**
1. Check operational health cache status
2. Trigger cache eviction: `POST /admin/scheduler/cache` (SRH_ADMIN)
3. Optionally restart API pods to clear in-memory Caffeine
4. Verify fresh results with known test query
5. Review `cache.ttl-seconds` if staleness recurring

---

## RB-08: Embedding Provider Failure

**Symptoms:** `embedding.errors` spike; semantic search fails; provider health DOWN.

**Steps:**
1. `GET /admin/semantic/provider` — check active provider
2. Verify API key/endpoint env vars
3. Test provider connectivity externally
4. Fallback: set `semantic.provider=mock` temporarily (keyword-only)
5. Fix provider credentials; trigger `POST /admin/scheduler/embedding`
6. Re-enable production provider

---

## RB-09: Semantic Provider Switch

**Symptoms:** Migrating OpenAI → Azure or Ollama.

**Steps:**
1. Configure new provider block in yaml
2. Update `semantic.provider` value
3. Increment `embedding-version`
4. Restart API with new config
5. Run embedding generation job
6. Validate hybrid search quality
7. Decommission old provider credentials

---

## RB-10: Scheduler Failure

**Symptoms:** Jobs not running; scheduler history shows FAILED/RETRYING.

**Steps:**
1. `GET /admin/scheduler` — verify enabled and cron expressions
2. `GET /admin/scheduler/history` — inspect error messages
3. Check API pod logs for scheduler exceptions
4. Verify `govos.search.scheduler.enabled=true`
5. Manual retry via appropriate `POST /admin/scheduler/*` endpoint
6. If persistent, restart API pods (scheduler is in-process Spring)

**Note:** Scheduler state is in-memory for history; pod restart clears history but not cron schedule.

---

## Escalation Matrix

| Severity | Condition | Escalate to |
|----------|-----------|-------------|
| P1 | All search down | Platform SRE + OpenSearch ops |
| P2 | Single index failure | SRH admin + product owner |
| P3 | Semantic degraded | AI provider ops |
| P4 | Scheduler missed run | Platform ops (next window) |
