# SRH v1.0.0 — API Reference

**Base URL:** `/api/v1/search`  
**Authentication:** JWT Bearer token (all endpoints)  
**Response wrapper:** `ApiResponse<T>` with `requestId`

---

## Security Permissions

| Permission | Scope |
|------------|-------|
| *(JWT only)* | CRUD, query, semantic endpoints |
| `SRH_MONITOR` | Health, cluster, observability |
| `SRH_ADMIN` | Statistics, dashboard, scheduler |
| `SRH_REINDEX` | Reindex operations |

---

## Index Management

| Method | Path | Permission | Status | Description |
|--------|------|------------|--------|-------------|
| POST | `/indexes` | JWT | 201 | Create index |
| PUT | `/indexes/{id}` | JWT | 200 | Update index |
| GET | `/indexes/{id}` | JWT | 200 | Get by ID |
| GET | `/indexes/code/{code}` | JWT | 200 | Get by code |
| GET | `/indexes` | JWT | 200 | List (paginated) |
| POST | `/indexes/{id}/activate` | JWT | 200 | Activate |
| POST | `/indexes/{id}/archive` | JWT | 200 | Archive |
| DELETE | `/indexes/{id}` | JWT | 204 | Soft delete |
| POST | `/indexes/{id}/restore` | JWT | 200 | Restore |

**Example — Create index:**
```json
POST /api/v1/search/indexes
{
  "code": "cmp-complaint",
  "name": "CMP Complaints",
  "engineType": "OPENSEARCH",
  "organizationId": "550e8400-e29b-41d4-a716-446655440000"
}
```

---

## Document Management

| Method | Path | Permission | Status |
|--------|------|------------|--------|
| POST | `/documents` | JWT | 201 |
| PUT | `/documents/{id}` | JWT | 200 |
| GET | `/documents/{id}` | JWT | 200 |
| GET | `/documents/index/{indexId}` | JWT | 200 |
| GET | `/documents/organization/{orgId}` | JWT | 200 |
| DELETE | `/documents/{id}` | JWT | 204 |
| POST | `/documents/{id}/restore` | JWT | 200 |

---

## Alias Management

| Method | Path | Permission | Status |
|--------|------|------------|--------|
| POST | `/aliases` | JWT | 201 |
| PUT | `/aliases/{id}` | JWT | 200 |
| GET | `/aliases/{aliasName}` | JWT | 200 |
| GET | `/aliases/index/{indexId}` | JWT | 200 |
| POST | `/aliases/{id}/activate` | JWT | 200 |
| DELETE | `/aliases/{id}` | JWT | 204 |
| POST | `/aliases/{id}/restore` | JWT | 200 |

---

## Sync Jobs

| Method | Path | Permission | Status |
|--------|------|------------|--------|
| POST | `/jobs` | JWT | 201 |
| PUT | `/jobs/{id}` | JWT | 200 |
| GET | `/jobs/{id}` | JWT | 200 |
| GET | `/jobs/index/{indexId}` | JWT | 200 |
| POST | `/jobs/{id}/start` | JWT | 200 |
| POST | `/jobs/{id}/complete` | JWT | 200 |
| POST | `/jobs/{id}/fail` | JWT | 200 |
| POST | `/jobs/{id}/cancel` | JWT | 200 |

---

## Query History

| Method | Path | Permission | Status |
|--------|------|------------|--------|
| GET | `/queries/organization/{orgId}` | JWT | 200 |
| GET | `/queries/user/{userId}` | JWT | 200 |

---

## Search & Query

| Method | Path | Permission | Status | Description |
|--------|------|------------|--------|-------------|
| POST | `/query` | JWT | 200 | Full-text search |
| POST | `/autocomplete` | JWT | 200 | Typeahead |
| POST | `/facets` | JWT | 200 | Faceted search |
| POST | `/geo` | JWT | 200 | Geo search |
| GET | `/count` | JWT | 200 | Result count |
| POST | `/semantic` | JWT | 200 | Vector semantic search |
| POST | `/hybrid` | JWT | 200 | Keyword + vector hybrid |

**Example — Search:**
```json
POST /api/v1/search/query
{
  "indexCode": "cmp-complaint",
  "organizationId": "550e8400-e29b-41d4-a716-446655440000",
  "query": "water supply issue",
  "page": { "page": 0, "size": 20, "sort": "createdDate,desc" }
}
```

**Response (200):**
```json
{
  "success": true,
  "data": {
    "totalHits": 42,
    "hits": [...],
    "facets": {},
    "tookMs": 15
  },
  "requestId": "abc-123"
}
```

---

## Administration — Monitor (`SRH_MONITOR`)

| Method | Path | Status |
|--------|------|--------|
| GET | `/admin/health` | 200 |
| GET | `/admin/health/operational` | 200 |
| GET | `/admin/semantic/provider` | 200 |
| GET | `/admin/cluster` | 200 |
| GET | `/admin/observability` | 200 |
| GET | `/admin/traces?limit=50` | 200 |
| GET | `/admin/metrics` | 200 |
| GET | `/admin/latency` | 200 |
| GET | `/admin/errors` | 200 |

---

## Administration — Analytics (`SRH_ADMIN`)

| Method | Path | Status |
|--------|------|--------|
| GET | `/admin/statistics` | 200 |
| GET | `/admin/dashboard` | 200 |
| GET | `/admin/indexes/{id}/statistics` | 200 |
| GET | `/admin/queries/top?limit=10` | 200 |
| GET | `/admin/queries/slow?limit=10` | 200 |
| GET | `/admin/scheduler` | 200 |
| POST | `/admin/scheduler/reindex?full=true` | 202 |
| POST | `/admin/scheduler/embedding` | 202 |
| POST | `/admin/scheduler/cache` | 202 |
| POST | `/admin/scheduler/statistics` | 202 |
| POST | `/admin/scheduler/cleanup` | 202 |
| GET | `/admin/scheduler/history?limit=50` | 200 |

---

## Administration — Reindex (`SRH_REINDEX`)

| Method | Path | Status |
|--------|------|--------|
| POST | `/admin/indexes/{id}/reindex` | 202 |
| POST | `/admin/reindex-all` | 202 |
| POST | `/admin/jobs/{id}/cancel` | 200 |

---

## Common Status Codes

| Code | Meaning |
|------|---------|
| 200 | Success |
| 201 | Created |
| 202 | Accepted (async reindex/scheduler) |
| 204 | No content (delete) |
| 400 | Validation error |
| 401 | Missing/invalid JWT |
| 403 | Missing SRH permission |
| 404 | Resource not found |
| 500 | Internal error |
| 503 | OpenSearch unavailable (graceful degradation) |

OpenAPI spec: `/v3/api-docs` (Swagger UI at `/swagger-ui.html` in non-prod profiles).
