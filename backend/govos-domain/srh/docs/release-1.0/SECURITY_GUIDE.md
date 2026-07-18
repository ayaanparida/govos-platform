# SRH v1.0.0 — Security Guide

---

## 1. Authentication

All `/api/v1/search/**` endpoints require JWT Bearer authentication via GovOS platform security (`govos-security` module).

```
Authorization: Bearer <jwt-token>
```

Unauthenticated requests receive **401 Unauthorized**.

---

## 2. Authorization Permissions

| Permission | Endpoints | Purpose |
|------------|-----------|---------|
| `SRH_MONITOR` | Health, cluster, observability (9 endpoints) | Read-only operations monitoring |
| `SRH_ADMIN` | Statistics, dashboard, scheduler (12 endpoints) | Search administration |
| `SRH_REINDEX` | Reindex, cancel jobs (3 endpoints) | Destructive index operations |

Non-admin endpoints (CRUD, query) require valid JWT only — fine-grained product permissions enforced at product layer.

Method security: `@PreAuthorize("hasAuthority('SRH_*')")` on admin methods.

---

## 3. Secret Management

| Secret | Storage | Never |
|--------|---------|-------|
| `GOVOS_SEARCH_PASSWORD` | Secret manager / K8s Secret | In git, logs |
| `GOVOS_SEARCH_OPENAI_API_KEY` | Secret manager | In application.yml committed |
| `GOVOS_SEARCH_AZURE_API_KEY` | Secret manager | In logs |
| JWT signing key | Platform security config | Exposed to clients |

**Production rule:** All credentials via environment variables or secret manager injection.

---

## 4. TLS

| Connection | Requirement |
|------------|-------------|
| Client → API | HTTPS (platform ingress) |
| API → OpenSearch | `govos.search.ssl=true` in production |
| API → Embedding provider | HTTPS (provider default URLs) |
| OTLP export | TLS recommended for collector |

---

## 5. Logging & PII

SRH structured logging **never includes:**

- Search query text
- Document JSON / content
- Embedding vectors
- API keys or tokens
- User PII beyond opaque user ID in MDC

Safe log fields: `operation`, `status`, `durationMs`, `organizationId`, `requestId`, `traceId`, `documentCount`, `provider`.

---

## 6. Multi-Tenancy Isolation

- Every query filtered by `organizationId`
- Products must not query cross-tenant
- Admin endpoints return platform-wide aggregates (restricted to SRH_ADMIN/MONITOR)

---

## 7. Input Validation

- Jakarta Validation on all request DTOs
- Pagination guards prevent deep pagination abuse
- Query timeout limits (`query-timeout-ms`)
- Max page size enforced (`max-page-size`)

---

## 8. API Key Handling (Embedding Providers)

- Keys loaded at startup from environment
- Factory logs provider name only — never key value
- Mock provider used when production provider misconfigured (warn logged)

---

## 9. Audit

- Query history stored optionally (`srh_search_query_history`) — org-scoped
- Scheduler execution history — in-memory (no document content)
- Platform AUD module separate from SRH — products audit business actions

---

## 10. Security Checklist (Production)

- [ ] TLS enabled for OpenSearch
- [ ] All secrets in secret manager
- [ ] SRH permissions assigned via IDM
- [ ] Swagger disabled in prod profile
- [ ] Actuator endpoints restricted
- [ ] Log aggregation excludes sensitive fields
- [ ] Network policy: API → OpenSearch only (no product direct access)

See [SECURITY_REVIEW.md](./SECURITY_REVIEW.md) for certification review.
