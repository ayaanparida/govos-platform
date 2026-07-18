# GPS-001 — 01 Platform Overview

---

## 1. What is GovOS?

GovOS (Government Operating System) is an **enterprise modular monolith** platform for digital government services. It provides shared platform capabilities (identity, documents, workflow, search, audit, notifications) consumed by product bounded contexts (complaints, RTI, licensing, etc.).

---

## 2. Platform vs Product

| Layer | Examples | Owns |
|-------|----------|------|
| **Platform modules** | IDM, ORG, MDM, DOC, WRK, NTF, AUD, SRH | Cross-cutting capabilities |
| **Product modules** | CMP (Complaints), future RTI, Trade License | Business domain logic |
| **Infrastructure** | Flyway, PostgreSQL, security filters | Persistence, cross-cutting tech |
| **API** | `govos-api` | REST, orchestration, integration |

Products **consume** platform services. Products **never** bypass platform abstractions (e.g. no direct OpenSearch from CMP).

---

## 3. Module Map

```
govos-platform/backend/
├── govos-common          Shared base types (AuditableEntity, etc.)
├── govos-shared          Cross-cutting utilities
├── govos-domain          Business logic by bounded context
│   ├── com.govos.cmp     Complaints (product)
│   ├── com.govos.srh     Search (platform)
│   ├── com.govos.idm     Identity (platform)
│   ├── com.govos.doc     Documents (platform)
│   ├── com.govos.wrk     Workflow (platform)
│   ├── com.govos.audit   Audit (platform)
│   ├── com.govos.mdm     Master data (platform)
│   └── …
├── govos-infrastructure  Flyway, DB config
├── govos-security        JWT, filters, method security
└── govos-api             REST controllers, application services
```

---

## 4. Modular Monolith

GovOS deploys as a **single deployable** (`govos-api` JAR) with **logical module boundaries** enforced by package structure and dependency rules. This enables:

- Simple operations (one artifact, one database)
- Clear DDD boundaries
- Future extraction to microservices without rewrite

See [02_ARCHITECTURE_PRINCIPLES.md](./02_ARCHITECTURE_PRINCIPLES.md) for migration guidelines.

---

## 5. Technology Stack (Baseline)

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Runtime |
| Spring Boot | 3.5.x | Framework |
| PostgreSQL | 15+ | Primary datastore |
| Flyway | 11.x | Schema migrations |
| MapStruct | 1.6.x | DTO mapping |
| JUnit 5 + Mockito | — | Unit testing |
| JaCoCo | 0.8.x | Coverage gates |
| JWT | — | Authentication |
| Micrometer + OpenTelemetry | — | Observability |

---

## 6. API Surface

- **Base path:** `/api/v1`
- **Versioning:** URI version prefix (`v1`)
- **Documentation:** OpenAPI 3 (`/v3/api-docs`, Swagger UI in non-prod)
- **Auth:** JWT Bearer on all protected endpoints

---

## 7. Multi-Tenancy

Organization-scoped data uses `organizationId` (UUID) on entities and queries. Platform modules enforce tenant isolation at service and query layers.

---

## 8. GPS-001 Scope

GPS-001 defines **how** every module is built. It does not define product business rules. Each bounded context maintains its own README and sprint documentation (e.g. SRH-001, CMP-015) aligned with GPS-001.
