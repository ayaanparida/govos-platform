# GPS-001 — 02 Architecture Principles

---

## 1. Domain-Driven Design (DDD)

| Concept | GovOS application |
|---------|-------------------|
| **Bounded context** | One package root per module (`com.govos.srh`, `com.govos.cmp`) |
| **Aggregate** | Entity cluster with single root; one repository per aggregate root |
| **Domain service** | Stateless operations spanning aggregates within one context |
| **Application service** | Orchestration in `govos-api`; no business rules |
| **Ubiquitous language** | Module README defines terms; code matches domain language |

---

## 2. Hexagonal Architecture (Ports & Adapters)

```
         ┌─────────────────────────────────┐
  HTTP ──►│  Adapters (govos-api)          │
         │       ↓ ports (interfaces)      │
         │  Domain (govos-domain)          │
         │       ↓ ports                   │
  DB ────►│  Adapters (JPA, OpenSearch, …) │
         └─────────────────────────────────┘
```

- **Inbound adapters:** REST controllers, schedulers (when context-owned)
- **Outbound adapters:** JPA repositories, engine providers, external APIs
- **Domain core:** No Spring annotations on pure domain logic where possible; entities are JPA exceptions

---

## 3. Clean Architecture Layers

| Layer | Location | Rules |
|-------|----------|-------|
| Presentation | `govos-api` controllers | DTOs only; delegate to application service |
| Application | `govos-api` `*ApplicationService` | Orchestration, transaction demarcation |
| Domain | `govos-domain` services, entities | Business rules, validation |
| Infrastructure | repositories, engine, Flyway | Technical details |

**Dependency rule:** Inner layers never depend on outer layers.

---

## 4. CQRS (Where Applicable)

GovOS uses **pragmatic CQRS**:

| Pattern | When |
|---------|------|
| Separate read models | SRH query service, analytics dashboards |
| Same aggregate write/read | Most CRUD entities |
| Query-only DTOs | Facet search, admin statistics |

Full event-sourced CQRS is **not** required unless documented in module ADR.

---

## 5. SOLID

| Principle | GovOS rule |
|-----------|------------|
| **S** Single responsibility | One service per aggregate operation group |
| **O** Open/closed | Extend via interfaces (e.g. `SearchEngineProvider`) |
| **L** Liskov substitution | Provider implementations interchangeable |
| **I** Interface segregation | Small service interfaces |
| **D** Dependency inversion | Inject interfaces; domain defines ports |

---

## 6. Composition Over Inheritance

- Prefer delegation and interfaces over deep class hierarchies
- Use `AuditableEntity` base for audit columns only — no business logic in base classes
- Records for immutable DTOs and events

---

## 7. Modular Monolith Rules

1. **No cross-context entity references** — UUID columns only, no FK across bounded contexts
2. **Integration via application services** or dedicated integration classes in `govos-api`
3. **Shared kernel minimal** — `govos-common` for audit base, UUID utilities only
4. **One Flyway location** — `govos-infrastructure`
5. **One deployable** — `govos-api` unless explicitly split in future ADR

---

## 8. Future Microservice Migration

When extracting a bounded context:

1. Module must already have clear package boundary
2. Extract `{Context}ApplicationService` + domain package
3. Replace in-process calls with HTTP/events
4. Keep same REST contract (`/api/v1/{context}`)
5. Database per service (future) — start with schema separation in `govos` schema today

**Preparation now:** No direct imports between `com.govos.cmp` and `com.govos.srh` domain packages.

---

## 9. Anti-Patterns (Forbidden)

| Anti-pattern | Why |
|--------------|-----|
| Product → another context's repository | Breaks bounded context |
| Business logic in controllers | Violates layering |
| Shared mutable static state | Testability, threading |
| God service (>500 lines) | Split by aggregate |
| Circular module dependencies | Architecture violation |
