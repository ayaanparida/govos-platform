# GPS-001 — 05 Naming Conventions

---

## 1. Java Classes

| Type | Pattern | Example |
|------|---------|---------|
| Entity | `{Aggregate}` | `SearchIndex`, `Complaint` |
| Repository | `{Aggregate}Repository` | `SearchIndexRepository` |
| Service interface | `{Aggregate}Service` | `SearchIndexService` |
| Service impl | `{Aggregate}ServiceImpl` | `SearchIndexServiceImpl` |
| Validator | `{Aggregate}Validator` | `SearchIndexValidator` |
| Mapper | `{Aggregate}Mapper` | `SearchIndexMapper` |
| Exception | `{Aggregate}NotFoundException` | `SearchIndexNotFoundException` |
| DTO | `{Aggregate}Dto` | `SearchIndexDto` |
| Create request | `{Aggregate}CreateRequest` | `SearchIndexCreateRequest` |
| Update request | `{Aggregate}UpdateRequest` | `SearchIndexUpdateRequest` |
| Controller | `{Context}Controller` | `SearchController` |
| Application service | `{Context}ApplicationService` | `SearchApplicationService` |
| Configuration | `{Context}Configuration` | `SearchConfiguration` |
| Properties | `{Context}Properties` | `SearchProperties` |

---

## 2. Database

| Element | Convention | Example |
|---------|------------|---------|
| Schema | `govos` | Fixed |
| Table | `{prefix}_{snake_case}` | `srh_search_index` |
| Column | `snake_case` | `created_date`, `organization_id` |
| Primary key | `id` | UUID |
| FK column | `{referenced}_id` | UUID only, no cross-context FK |
| Index | `idx_{table}_{columns}` | `idx_srh_search_index_code` |
| Unique | `uk_{table}_{columns}` | `uk_srh_search_index_code` |

---

## 3. Table Prefixes

| Context | Prefix |
|---------|--------|
| Master Data | `mdm_` |
| Identity | `idm_` |
| Organization | `org_` |
| Document | `doc_` |
| Workflow | `wrk_` |
| Notification | `ntf_` |
| Audit | `aud_` |
| Search | `srh_` |
| Complaint | `cmp_` |

---

## 4. REST API

| Element | Convention | Example |
|---------|------------|---------|
| Base path | `/api/v1/{context}` | `/api/v1/search` |
| Resource | plural kebab-case | `/indexes`, `/documents` |
| Path param | `{id}` UUID | `/indexes/{id}` |
| Action | verb sub-path | `/indexes/{id}/activate` |
| Admin | `/admin/...` | `/admin/health` |

---

## 5. Permissions

Pattern: `{CONTEXT}_{ACTION}` in UPPER_SNAKE_CASE

| Example | Meaning |
|---------|---------|
| `SRH_MONITOR` | Read-only search ops monitoring |
| `SRH_ADMIN` | Search administration |
| `SRH_REINDEX` | Reindex operations |
| `CMP_READ` | Read complaints |
| `CMP_WRITE` | Write complaints |

---

## 6. Configuration Properties

Pattern: `govos.{context}.{property}` in kebab-case YAML

```yaml
govos.search.host: localhost
govos.search.semantic.enabled: false
```

Environment: `GOVOS_{CONTEXT}_{PROPERTY}` in UPPER_SNAKE

---

## 7. Metrics

Pattern: `{domain}.{noun}.{verb}` or `{context}.{operation}`

Examples: `search.requests`, `scheduler.executions`, `search.trace.created`

Tags: lowercase, snake_case keys: `operation`, `provider`, `job`

---

## 8. Flyway Migrations

Pattern: `V{major}_{minor}_{patch}__{description}.sql`

Example: `V2_1_0__search.sql`

---

## 9. Tests

| Type | Pattern |
|------|---------|
| Unit test class | `{ClassUnderTest}Test` |
| Integration test | `{Feature}IntegrationTest` |
| Test method | `should{ExpectedBehavior}When{Condition}` |

Example: `shouldReturnIndexWhenIdExists`

---

## 10. Sprint / Document IDs

| Type | Pattern | Example |
|------|---------|---------|
| Platform standard | `GPS-{nnn}` | GPS-001 |
| Bounded context | `{CTX}-{nnn}` | SRH-019, CMP-015 |
| ADR | `ADR-{nnn}` | ADR-005 |
