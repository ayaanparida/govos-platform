# SRH v1.0.0 — Performance Report

**Certification date:** 2026-07-18  
**Methodology:** Architecture analysis, configuration review, test suite validation  
**Note:** Figures are engineering estimates for production planning — not load test certificates.

---

## 1. Test Environment Baseline

| Component | Specification |
|-----------|---------------|
| Java | 21 |
| Spring Boot | 3.5.16 |
| OpenSearch | 2.x (3-node cluster assumed for prod estimates) |
| Test suite | 640+ unit/integration tests — BUILD SUCCESS |

---

## 2. Document Scale Estimates

### 100K Documents

| Metric | Estimate |
|--------|----------|
| Index storage (OpenSearch) | ~2–5 GB (depends on payload size) |
| Vector storage (semantic) | ~600 MB (384-dim) / ~2.4 GB (1536-dim) |
| PostgreSQL metadata | ~50 MB |
| Full reindex duration | 10–15 minutes |
| Bulk throughput | ~200 docs/sec |
| Query P95 (keyword) | 50–80 ms |

**Infrastructure:** 3 OpenSearch nodes (4 GB heap), 2 API pods (1 CPU, 1 GB)

### 1M Documents

| Metric | Estimate |
|--------|----------|
| Index storage | ~20–50 GB |
| Vector storage | ~6 GB / ~24 GB |
| PostgreSQL metadata | ~500 MB |
| Full reindex duration | 1.5–2.5 hours |
| Bulk throughput | ~150 docs/sec (cluster dependent) |
| Query P95 (keyword) | 80–120 ms |

**Infrastructure:** 3 OpenSearch nodes (8 GB heap), 3 API pods (2 CPU, 2 GB)

### 10M Documents

| Metric | Estimate |
|--------|----------|
| Index storage | ~200–500 GB |
| Vector storage | ~60 GB / ~240 GB |
| PostgreSQL metadata | ~5 GB |
| Full reindex duration | 15–24 hours (off-peak recommended) |
| Bulk throughput | ~100 docs/sec |
| Query P95 (keyword) | 100–200 ms |

**Infrastructure:** 5+ OpenSearch nodes (16 GB heap), 5+ API pods (2 CPU, 2 GB), index sharding required

---

## 3. Concurrent User Estimates

| Concurrent users | Sustained QPS | API pods | Notes |
|------------------|---------------|----------|-------|
| 50 | ~25 QPS | 2 | Default config sufficient |
| 200 | ~100 QPS | 3 | Enable cache warm; pool=100 |
| 500 | ~250 QPS | 5 | Reduce observation sample-rate |
| 1000 | ~500 QPS | 8+ | OpenSearch read replicas recommended |

---

## 4. Query Latency Breakdown (P95 targets)

| Stage | Keyword | Semantic |
|-------|---------|----------|
| API + validation | 5 ms | 5 ms |
| Cache lookup | 1 ms | 1 ms |
| Embedding (query) | — | 100–200 ms |
| OpenSearch query | 30–80 ms | 50–150 ms |
| Response mapping | 5 ms | 5 ms |
| **Total** | **50–100 ms** | **200–350 ms** |

---

## 5. Memory Budget (per API pod)

| Component | 100K scale | 1M scale |
|-----------|------------|----------|
| JVM heap | 1 GB | 2 GB |
| Caffeine read cache | ~50 MB | ~200 MB |
| Embedding cache | ~100 MB | ~300 MB |
| HTTP connection pool | ~20 MB | ~40 MB |
| **Pod limit** | **2 Gi** | **3 Gi** |

---

## 6. CPU Budget

| Workload mix | CPU per pod |
|--------------|-------------|
| 80% read / 20% write | 0.5–1 core |
| 50% read / 50% write | 1–2 cores |
| Reindex window | Burst to 4 cores |
| Embedding batch job | Burst to 4 cores |

---

## 7. Scheduler Impact

| Job | Resource impact | Recommended window |
|-----|-----------------|-------------------|
| Daily full reindex | High CPU/IO | 02:00 off-peak |
| Embedding generation | High network (provider API) | 03:30 |
| Cache eviction | Low | 04:00 |
| Health verification | Low | Every 15 min |

---

## 8. Recommendations

1. Use prod profile with `cache.warm-on-startup=true`
2. Set `pool.max-connections=100` for production
3. Schedule reindex/embedding off-peak
4. Monitor `search.duration` P95 in Prometheus
5. Scale OpenSearch before API for document-heavy workloads
6. Use `observation.sample-rate=0.25` above 250 QPS

---

## 9. Certification Statement

Performance characteristics documented above are suitable for GovOS platform planning. Formal load testing recommended per deployment environment before go-live.
