# SRH v1.0.0 — Developer Guide

**Audience:** Backend developers integrating products with SRH

---

## 1. Prerequisites

- Java 21, Maven 3.9+
- PostgreSQL 15+ (metadata)
- OpenSearch 2.x (search engine)
- GovOS Platform backend (`govos-api` module)

---

## 2. Local Setup

```bash
cd govos-platform/backend
mvn -pl govos-api -am spring-boot:run
```

Configure OpenSearch in `application.yml`:

```yaml
govos.search:
  host: localhost
  port: 9200
  semantic:
    enabled: false   # start with keyword search only
```

---

## 3. Integration Pattern (Required)

Products integrate via a dedicated integration class in `govos-api`:

```
com.govos.api.{product}.search.{Product}SearchIntegrationImpl
  → SearchApplicationService
```

**Reference implementation:** `ComplaintSearchIntegrationImpl` (CMP-015 / SRH-013)

### Do

- Call `SearchApplicationService` for index, query, delete operations
- Pass `organizationId` on every request
- Build search documents in product layer; SRH stores payload
- Index synchronously within product transaction

### Do Not

- Import `com.govos.srh.engine.*` from products
- Call OpenSearch directly
- Schedule search operations from products
- Modify SRH domain entities from products

---

## 4. Key Service Entry Points

| Operation | Application method |
|-----------|-------------------|
| Index document | `createDocument()` / `updateDocument()` |
| Search | `search()` |
| Semantic search | `semanticSearch()` / `hybridSearch()` |
| Autocomplete | `autocomplete()` |
| Facets | `facetSearch()` |
| Geo | `geoSearch()` |
| Reindex | `reindexIndex()` (requires SRH_REINDEX) |

---

## 5. Module Dependencies

Products in `govos-api` depend on `govos-domain` (SRH packages). No circular dependencies.

```
govos-api → govos-domain (srh) → govos-common
```

---

## 6. Testing

```bash
mvn -pl govos-api -am test
```

SRH-specific tests live in:
- `govos-domain/src/test/java/com/govos/srh/`
- `govos-api/src/test/java/com/govos/api/srh/`

Coverage gates (SRH-009): service/validator/mapper ≥90%; overall ≥85%.

---

## 7. Extension Points (Post v1.0)

| Extension | Location | v1.0 status |
|-----------|----------|-------------|
| New product consumer | `govos-api` integration class | Allowed |
| New embedding provider | `com.govos.srh.ai.provider` | Requires new sprint |
| New engine backend | `SearchEngineProvider` impl | Not in v1.0 scope |
| Domain event publishing | `com.govos.srh.event` | Contracts only |

---

## 8. Code Conventions

- Package prefix: `com.govos.srh`
- Table prefix: `srh_`
- DTOs in `com.govos.srh.dto`
- Exceptions in `com.govos.srh.exception`
- MapStruct mappers in `com.govos.srh.mapper`

See [INTEGRATION_GUIDE.md](./INTEGRATION_GUIDE.md) for step-by-step product wiring.
