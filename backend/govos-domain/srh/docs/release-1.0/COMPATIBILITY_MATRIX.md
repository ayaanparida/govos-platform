# SRH v1.0.0 — Compatibility Matrix

**Certified:** 2026-07-18  
**SRH Version:** 1.0.0

---

## Runtime & Framework

| Component | Certified Version | Minimum | Maximum Tested | Notes |
|-----------|-------------------|---------|--------------|-------|
| Java | 21 | 21 | 21 | LTS required; enforced by Maven enforcer |
| Spring Boot | 3.5.16 | 3.5.x | 3.5.16 | Parent BOM |
| Spring Framework | 6.2.x (via Boot) | 6.2+ | — | Managed by Boot |
| Jakarta EE | 10 | 10 | — | Servlet, Validation, Persistence |

---

## Data & Search

| Component | Certified Version | Notes |
|-----------|-------------------|-------|
| PostgreSQL | 15+ | Metadata store; Flyway migrations |
| OpenSearch | 2.x | Target cluster; kNN for semantic vectors |
| OpenSearch Java Client | 2.18.0 | `opensearch-java`, `opensearch-rest-client` |
| Flyway | 11.7.2 | Spring Boot managed; migration `V2_1_0__search.sql` |

---

## Observability

| Component | Certified Version | Notes |
|-----------|-------------------|-------|
| Micrometer | 1.15.12 | Via Spring Boot 3.5 BOM |
| Prometheus registry | 1.15.12 | Prod actuator exposure |
| OpenTelemetry API | Boot-managed | OTLP gRPC exporter |
| Micrometer Observation | Boot-managed | Vendor-neutral instrumentation |

---

## Build & Mapping

| Component | Certified Version | Notes |
|-----------|-------------------|-------|
| MapStruct | 1.6.3 | Domain DTO mappers |
| Maven | 3.9+ | Multi-module build |
| JaCoCo | 0.8.13 | SRH coverage gates ≥85% overall |

---

## Frontend (Consumer)

| Component | Certified Version | Notes |
|-----------|-------------------|-------|
| Angular | 20.3.x | GovOS frontend; consumes API via OpenAPI |
| TypeScript | 5.x | Via Angular CLI 20.3 |
| OpenAPI Generator | typescript-angular | `govos-api.yaml` client generation |

SRH does not ship frontend components in v1.0. Products consume SRH via REST only.

---

## Container & Orchestration

| Component | Certified | Notes |
|-----------|-----------|-------|
| Docker | ✅ Compatible | Standard JRE 21 base image recommended |
| Kubernetes | ✅ Compatible | Stateless API pods; external OpenSearch + PostgreSQL |
| Helm | ✅ Compatible | No official chart in v1.0; deploy via platform chart |

No Dockerfile ships with SRH v1.0 — deployment uses platform `govos-api` artifact.

---

## Semantic AI Providers

| Provider | Config value | Vector dimension | Status |
|----------|--------------|------------------|--------|
| Mock | `mock` | 384 | Default dev/test |
| OpenAI | `openai` | 1536 | Production-ready |
| Azure OpenAI | `azure-openai` | 1536 | Production-ready |
| Ollama | `ollama` | 768 | Self-hosted option |

---

## Profile Compatibility

| Setting | Default / local | Production |
|---------|-----------------|------------|
| `semantic.enabled` | `false` | `true` recommended |
| `semantic.vector-store` | `memory` | `opensearch` |
| `cache.warm-on-startup` | `false` | `true` |
| `pool.max-connections` | 50 | 100 |
| Actuator | health, info | + prometheus, metrics |
| Swagger UI | enabled | disabled |

---

## API Compatibility

| Dimension | Value |
|-----------|-------|
| API version | v1 |
| Base path | `/api/v1/search` |
| Auth | JWT Bearer (all endpoints) |
| Admin permissions | `SRH_MONITOR`, `SRH_ADMIN`, `SRH_REINDEX` |
| Breaking changes | None since v1.0.0 baseline |

---

## Unsupported Combinations

| Combination | Reason |
|-------------|--------|
| OpenSearch 1.x | Client 2.18 requires OpenSearch 2.x API |
| Java 17 or below | Platform requires Java 21 |
| Direct product → OpenSearch | Architecture violation; use `SearchApplicationService` |
| Product-owned scheduling | SRH-019 owns all search scheduling |
