# SRH v1.0.0 — Performance Guide

**Purpose:** Production sizing guidance for operators and architects

See [PERFORMANCE_REPORT.md](./PERFORMANCE_REPORT.md) for detailed benchmarks and capacity tables.

---

## 1. Latency Expectations (P95)

| Operation | Target P95 | Notes |
|-----------|------------|-------|
| Keyword search | < 100 ms | Warm cache, < 1M docs per index |
| Autocomplete | < 50 ms | Prefix queries |
| Facet search | < 150 ms | Depends on facet cardinality |
| Geo search | < 120 ms | Radius queries |
| Semantic search | < 300 ms | Includes embedding generation for query |
| Hybrid search | < 350 ms | Keyword + vector |
| Bulk index (500 docs) | < 2 s | Batch size default |
| Reindex (10K docs) | < 2 min | Single index |
| Admin health check | < 500 ms | Cluster state query |

*Targets assume healthy 3-node OpenSearch cluster, prod profile, cache warm.*

---

## 2. Document Scale Guidance

| Scale | OpenSearch | API pods | PostgreSQL |
|-------|------------|----------|------------|
| 100K docs | 3 nodes, 4 GB heap | 2 × 1 CPU, 1 GB | Standard |
| 1M docs | 3 nodes, 8 GB heap | 3 × 2 CPU, 2 GB | Standard + read replica |
| 10M docs | 5+ nodes, 16 GB heap | 5+ × 2 CPU, 2 GB | Dedicated instance |

---

## 3. Concurrent Users

| Users | API replicas | Pool connections | Cache entries |
|-------|--------------|------------------|---------------|
| 50 | 2 | 50 | 1000 |
| 200 | 3 | 100 | 5000 |
| 1000 | 5+ | 100 per pod | 10000 |

---

## 4. Memory Recommendations (govos-api pod)

| Profile | Heap (-Xmx) | Native/overhead | Total pod limit |
|---------|-------------|-----------------|-----------------|
| Dev | 512 MB | 256 MB | 1 Gi |
| Prod (100K) | 1 GB | 512 MB | 2 Gi |
| Prod (1M+) | 2 GB | 512 MB | 3 Gi |

Caffeine cache and embedding cache consume heap proportional to `max-entries`.

---

## 5. CPU Recommendations

| Workload | CPU request | CPU limit |
|----------|-------------|-----------|
| Query-heavy | 500m | 2 |
| Index-heavy (reindex window) | 1 | 4 |
| Semantic enabled | 1 | 4 |

Embedding generation is CPU/network bound — schedule off-peak via scheduler cron.

---

## 6. Tuning Parameters

| Parameter | Tuning direction |
|-----------|------------------|
| `cache.ttl-seconds` | Increase for read-heavy, decrease for freshness |
| `cache.max-entries` | Increase with memory budget |
| `pool.max-connections` | Match concurrent query load |
| `bulk-batch-size` | Increase for reindex throughput (max ~1000) |
| `query-timeout-ms` | Increase for complex geo/facet queries |
| `observation.sample-rate` | Decrease in high-traffic (0.1–0.25) |

---

## 7. Anti-Patterns

- Deep pagination beyond 10K offset
- Full reindex during peak hours
- Semantic enabled without OpenSearch vector store in prod
- Single-node OpenSearch for production
- sample-rate 1.0 at >500 QPS without collector capacity

---

## 8. Monitoring Performance

- `/admin/latency` — SRH latency snapshot
- `/actuator/prometheus` — `search.duration`, `semantic.duration`
- OpenSearch `_nodes/stats` — JVM, query cache
- Grafana dashboards recommended for trend analysis
