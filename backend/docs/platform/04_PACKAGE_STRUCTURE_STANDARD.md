# GPS-001 — 04 Package Structure Standard

---

## 1. Bounded Context Root

Every bounded context uses:

```
com.govos.{context}/
```

Examples: `com.govos.srh`, `com.govos.cmp`, `com.govos.idm`

---

## 2. Standard Domain Packages

| Package | Required | Purpose |
|---------|----------|---------|
| `config` | Optional | `@Configuration`, `@ConfigurationProperties` |
| `entity` | Yes* | JPA entities (*if persistent) |
| `repository` | Yes* | Spring Data repositories |
| `dto` | Yes | Request/response DTOs |
| `mapper` | Yes | MapStruct mappers |
| `validator` | Yes | Business validation |
| `exception` | Yes | Typed exceptions |
| `event` | Optional | Domain event records |
| `service` | Yes | Service interfaces |
| `service.impl` | Yes | Service implementations |
| `enums` | Optional | Domain enums |
| `valueobject` | Optional | Value objects |

---

## 3. Extended Packages (When Applicable)

| Package | Context | Purpose |
|---------|---------|---------|
| `query` | SRH, read-heavy | Query service, request/response types |
| `engine` | SRH | External engine adapter |
| `admin` | Platform modules | Administration, dashboards |
| `scheduler` | SRH, batch contexts | Scheduled jobs |
| `observability` | Platform modules | Tracing, metrics |
| `production` | SRH | Resilience, cache, guards |
| `ai` | SRH | Semantic search, embeddings |
| `ai.provider` | SRH | AI provider implementations |

---

## 4. API Layer Packages (`govos-api`)

```
com.govos.api.{context}/
├── controller/          REST controllers
├── application/         Application services
├── mapper/              API-level mappers (optional)
├── config/              Security, module config
└── integration/         Cross-context ACL (products)
```

---

## 5. Module Documentation

Each context maintains:

```
govos-domain/{context}/README.md
```

Optional release docs:

```
govos-domain/{context}/docs/release-{version}/
```

---

## 6. Package Naming Rules

- Lowercase, singular nouns: `entity`, not `entities`
- `service.impl` not `serviceimpl`
- No abbreviated package names (`svc`, `ctrl`)
- Max nesting depth: 3 levels below context root (e.g. `com.govos.srh.ai.provider`)

---

## 7. Class Placement Matrix

| Class type | Package |
|------------|---------|
| `SearchIndex` entity | `entity` |
| `SearchIndexRepository` | `repository` |
| `SearchIndexService` | `service` |
| `SearchIndexServiceImpl` | `service.impl` |
| `SearchIndexValidator` | `validator` |
| `SearchIndexMapper` | `mapper` |
| `SearchIndexNotFoundException` | `exception` |
| `SearchController` | `govos-api...controller` |
| `SearchApplicationService` | `govos-api...application` |

---

## 8. Forbidden Placements

- Controllers in `govos-domain`
- Entities in `govos-api`
- Repositories outside `repository` package
- Business logic in `mapper` or `dto` packages
