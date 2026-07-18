# GPS-001 — 17 REST API Standard

---

## 1. Base Path & Versioning

```
/api/v1/{context}/{resource}
```

- Version in URI path (`v1`) — not header
- Context: singular noun matching module (`search`, `complaints`, `auth`)
- Constant: `ApiConstants.BASE_PATH = "/api/v1"`

---

## 2. URI Naming

| Rule | Good | Bad |
|------|------|-----|
| Plural resources | `/indexes` | `/index` |
| kebab-case | `/admin/health/operational` | `/admin/healthOperational` |
| UUID path params | `/indexes/{id}` | `/indexes/{code}` for mutations |
| Actions as sub-paths | `/indexes/{id}/activate` | `/activateIndex` |
| Admin prefix | `/admin/...` | Mixed admin paths |

---

## 3. HTTP Methods

| Method | Usage |
|--------|-------|
| GET | Read, list |
| POST | Create, actions, search queries |
| PUT | Full update |
| PATCH | Partial update (when supported) |
| DELETE | Soft delete |

Search queries use **POST** (complex body) even for read semantics.

---

## 4. Status Codes

| Code | Usage |
|------|-------|
| 200 | Success with body |
| 201 | Created |
| 202 | Accepted (async reindex, scheduler trigger) |
| 204 | Success no body (delete) |
| 400 | Validation error |
| 401 | Missing/invalid JWT |
| 403 | Missing permission |
| 404 | Resource not found |
| 409 | Conflict |
| 500 | Internal error |
| 503 | Dependency unavailable (graceful degradation) |

---

## 5. Pagination

Query params: `page`, `size`, `sort`

```json
GET /api/v1/search/indexes?page=0&size=20&sort=createdDate,desc
```

Response wrapper includes page metadata via `PageResponse<T>`.

Defaults and max page size from configuration.

---

## 6. Sorting

- Format: `field,direction` (e.g. `name,asc`)
- Allowlist sort fields in service layer
- Reject unknown sort fields with 400

---

## 7. Error Response

```json
{
  "success": false,
  "error": {
    "code": "NOT_FOUND",
    "message": "Search index not found"
  },
  "requestId": "550e8400-e29b-41d4-a716-446655440000"
}
```

Always include `requestId` from `X-Request-ID` / correlation filter.

---

## 8. OpenAPI Conventions

- `@Tag(name = "Search")` on controller
- `@Operation` summary + description on each endpoint
- `@SecurityRequirement(name = "bearerAuth")` on secured controllers
- Document permission in description: `"Requires SRH_ADMIN permission"`
- Generate spec: `/v3/api-docs`
- Swagger UI: disabled in prod profile

---

## 9. Request/Response Headers

| Header | Direction | Purpose |
|--------|-----------|---------|
| `Authorization: Bearer {jwt}` | Request | Authentication |
| `X-Request-ID` | Request/Response | Correlation |
| `X-Trace-ID` | Request/Response | Distributed tracing |
| `Content-Type: application/json` | Request | JSON payloads |

---

## 10. Prohibited

- Breaking changes to v1 without version bump
- Exposing internal entity structure
- GET with request body
- Non-standard status codes for common cases
