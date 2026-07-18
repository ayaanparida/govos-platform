# SRH v1.0.0 — Integration Guide

**Purpose:** How GovOS products consume SRH as a platform service

---

## 1. Integration Architecture

```
┌─────────────────────────────────────────┐
│  CMP (or future product)                 │
│  ComplaintServiceImpl                    │
│       ↓                                  │
│  ComplaintSearchIntegrationImpl          │
│       ↓                                  │
│  SearchApplicationService                │
└─────────────────────────────────────────┘
```

Products implement a search integration interface in the API layer. The integration translates product entities to SRH DTOs and delegates all search operations to `SearchApplicationService`.

---

## 2. Certified Consumer: CMP

| Component | Location |
|-----------|----------|
| Integration interface | `com.govos.api.cmp.search.ComplaintSearchIntegration` |
| Implementation | `ComplaintSearchIntegrationImpl` |
| Tests | `ComplaintSearchIntegrationTest`, `ComplaintSearchIntegrationV2Test` |

CMP indexes complaints synchronously on create/update and queries via `SearchApplicationService.search()`.

---

## 3. Integration Checklist for New Products

1. **Define integration interface** in `com.govos.api.{product}.search`
2. **Implement** using `SearchApplicationService` injection
3. **Register index** via `createIndex()` with product-specific code (e.g., `RTI_APPLICATION`)
4. **Build documents** — product owns field selection; store JSON in `searchText`
5. **Index on write** — call `createDocument()` / `updateDocument()` in product service
6. **Query on read** — call `search()` with `organizationId` filter
7. **Never** import OpenSearch or SRH engine classes
8. **Never** schedule reindex or embedding jobs

---

## 4. Document Model

Products submit `SearchDocumentCreateRequest`:

| Field | Source |
|-------|--------|
| `searchIndexId` | SRH index UUID |
| `organizationId` | Tenant context |
| `entityType` | Product entity name |
| `referenceId` | Product entity UUID |
| `searchText` | Product-built searchable JSON/text |
| `metadata` | Optional facets/filters |

SRH never interprets business semantics — it indexes and queries the payload.

---

## 5. Query Patterns

### Keyword search
```json
POST /api/v1/search/query
{
  "indexCode": "cmp-complaint",
  "organizationId": "...",
  "query": "water supply",
  "page": { "page": 0, "size": 20 }
}
```

### Semantic (when enabled)
```json
POST /api/v1/search/semantic
{
  "indexCode": "cmp-complaint",
  "organizationId": "...",
  "query": "complaints about drainage",
  "page": { "page": 0, "size": 20 }
}
```

---

## 6. Error Handling

Products should catch SRH exceptions at integration boundary:

| Exception | Action |
|-----------|--------|
| `SearchIndexNotFoundException` | Verify index registered |
| `SearchDocumentNotFoundException` | Handle stale reference |
| `SemanticSearchException` | Fall back to keyword search |
| `SearchValidationException` | Fix request payload |

---

## 7. Multi-Tenancy

Always pass `organizationId`. SRH enforces tenant isolation at query layer. Missing organization ID is a validation error.

---

## 8. What Products Must Not Do

| Anti-pattern | Reason |
|--------------|--------|
| Direct OpenSearch client | Violates platform ownership |
| Product-owned cron reindex | SRH-019 owns scheduling |
| Embedding API calls from product | SRH-018 owns AI providers |
| Custom trace/MDC for search | SRH-020 owns observability |

---

## 9. Angular Frontend

Frontend consumes SRH via generated OpenAPI client (`typescript-angular`, Angular 20.3). No SRH-specific frontend library in v1.0 — use platform API client.

---

## 10. Future Products

RTI, Trade License, Water Tax, Birth/Death Registration, Assets, Inventory — all follow the same integration pattern established by CMP-015.
