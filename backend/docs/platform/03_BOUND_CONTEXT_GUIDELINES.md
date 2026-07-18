# GPS-001 — 03 Bounded Context Guidelines

---

## 1. Context Ownership

Each bounded context **owns**:

- Its entities, repositories, domain services
- Its Flyway tables (prefix `{ctx}_`)
- Its REST endpoints under `/api/v1/{context}`
- Its permissions (`{CTX}_*` naming)
- Its module README and sprint documentation

Each context **does not own**:

- Another context's aggregates
- Another context's scheduling (unless platform scheduler module)
- Cross-cutting security infrastructure (govos-security)

---

## 2. Allowed Dependencies

```
govos-api
  → govos-domain (any context)
  → govos-security
  → govos-common

govos-domain/{context}
  → govos-common
  → Third-party libs (JPA, OpenSearch client for adapter)

govos-infrastructure
  → (Flyway scripts only; no domain logic)
```

**Product → Platform (allowed):**

```
com.govos.api.cmp.search.ComplaintSearchIntegrationImpl
  → SearchApplicationService   ✅
```

---

## 3. Forbidden Dependencies

| From | To | Reason |
|------|-----|--------|
| `com.govos.cmp.*` | `com.govos.srh.repository.*` | Context bypass |
| `com.govos.srh.*` | `com.govos.cmp.entity.*` | Context bypass |
| Any domain package | `govos-api` | Inverts layering |
| Entity | Another context's entity (JPA relation) | No cross-FK |

---

## 4. Shared Kernel

**`govos-common`** contains:

- `AuditableEntity` — audit columns, optimistic lock, soft delete
- Shared value types used by 3+ contexts (minimal set)
- No business logic

**Do not expand shared kernel** without architecture review.

---

## 5. Anti-Corruption Layer (ACL)

When integrating across contexts, use ACL in `govos-api`:

| Pattern | Example |
|---------|---------|
| Integration interface | `ComplaintSearchIntegration` |
| Integration impl | Translates CMP model → SRH DTOs |
| Location | `com.govos.api.{product}.integration` or `.search` |

ACL **translates** — it does not expose foreign domain models.

---

## 6. Cross-Context Communication

| Method | When | Example |
|--------|------|---------|
| **Application service call** | Synchronous, same JVM | CMP → `SearchApplicationService` |
| **Domain events** | Async decoupling (future) | Event records in `com.govos.{ctx}.event` |
| **REST** | External or future MS | Not in-process domain calls |
| **Shared DB read** | **Forbidden** | Use service API |

---

## 7. Application Service Usage

- **One application service per bounded context** in `govos-api` (e.g. `SearchApplicationService`, `ComplaintApplicationService`)
- Application services orchestrate domain services
- Products call platform application services, never domain services directly from controllers

---

## 8. Domain Service Usage

- Domain services live in `govos-domain`
- Encapsulate business rules for one context
- Called by application services or other domain services **within same context**
- `@Transactional` on service impl class or method as appropriate

---

## 9. Platform vs Product Classification

| Module | Type | Package prefix |
|--------|------|----------------|
| SRH | Platform | `com.govos.srh` |
| IDM, ORG, MDM, DOC, WRK, NTF, AUD | Platform | `com.govos.{name}` |
| CMP | Product | `com.govos.cmp` |
| Future RTI, License, etc. | Product | `com.govos.{product}` |

---

## 10. Context Diagram

```
┌─────────┐     ACL      ┌─────────┐
│   CMP   │─────────────►│   SRH   │
└─────────┘              └─────────┘
     │                        │
     └──────────┬─────────────┘
                ▼
         ┌─────────────┐
         │ IDM / ORG   │
         │ MDM / AUD   │
         └─────────────┘
```

Products sit above platform services; platform modules do not depend on products.
