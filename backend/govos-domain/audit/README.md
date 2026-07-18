# GovOS Audit (AUD)

Sprint 0 — Audit bounded context for the GovOS Enterprise Government Platform.

## Purpose

The AUD module provides a **reusable audit trail foundation** for recording who did what, when, and on which entity — across every GovOS bounded context. Complaints, workflows, documents, and identity changes all emit audit records through this module.

This module provides the **domain layer only** — no REST controllers, automatic change tracking, AOP, entity listeners, Kafka, or search integration in this sprint.

## Architecture

```
Business Action (future)
        ↓
   AuditEvent  ←── AuditActor (who)
        ↓              AuditSession (when/where)
   AuditChange[]   (field-level diffs)
        ↓
   AuditEntity     (entity registry)
        ↓
   AuditExport     (compliance export requests)
```

## Module Location

| Artifact | Package | Responsibility |
|----------|---------|----------------|
| `govos-domain` | `com.govos.audit` | AUD entities, services, repositories, DTOs, mappers, validators |
| `govos-infrastructure` | `db/migration` | Flyway `V1_7_0__audit.sql` |

## Package Structure

```
com.govos.audit
├── config          # Bounded-context configuration
├── controller      # Reserved for future REST layer (empty)
├── dto             # Request/response records
├── entity          # JPA entities and enums
├── event           # Domain events (records)
├── exception       # Domain exceptions
├── mapper          # MapStruct entity ↔ DTO mapping
├── repository      # Spring Data JPA repositories
├── service         # Service interfaces and implementations
└── validator       # Business validation rules
```

## Entity Relationship

```
AuditSession ──────────────┐
                           │
AuditActor ────┐           │
 (User FK)     │           │
               ▼           ▼
           AuditEvent ── AuditChange
               │
               │  entityType + entityId
               ▼
           AuditEntity (registry)

AuditExport ── requestedBy → idm.User
```

| Entity | Table | Description |
|--------|-------|-------------|
| `AuditEvent` | `aud_event` | Core audit log entry (append-only) |
| `AuditActor` | `aud_actor` | Actor snapshot (user, display name, org context) |
| `AuditEntity` | `aud_entity` | Registered business entity reference |
| `AuditChange` | `aud_change` | Field-level before/after values (append-only) |
| `AuditSession` | `aud_session` | User session context (login/logout, device) |
| `AuditExport` | `aud_export` | Compliance export request metadata |

### Enums

| Enum | Values |
|------|--------|
| `AuditEventType` | `ENTITY_CREATED`, `ENTITY_UPDATED`, `ENTITY_DELETED`, `ENTITY_VIEWED`, `USER_LOGIN`, `USER_LOGOUT`, `PERMISSION_CHANGE`, `EXPORT`, `SYSTEM`, `OTHER` |
| `AuditAction` | `CREATE`, `UPDATE`, `DELETE`, `READ`, `LOGIN`, `LOGOUT`, `EXPORT`, `ASSIGN`, `TRANSITION`, `OTHER` |
| `AuditEventStatus` | `PENDING`, `RECORDED`, `FAILED` |
| `AuditExportType` | `CSV`, `JSON`, `XML`, `PDF` |
| `AuditExportStatus` | `PENDING`, `IN_PROGRESS`, `COMPLETED`, `FAILED`, `CANCELLED` |

### Cross-Domain References

- `AuditActor.user` → `com.govos.idm.entity.User`
- `AuditExport.requestedBy` → `com.govos.idm.entity.User`

## Database

| Migration | Version | Purpose |
|-----------|---------|---------|
| `V1_7_0__audit.sql` | 1.7.0 | All AUD tables, FKs, indexes |

Key indexes:

- Unique `event_code` (active, non-deleted)
- Composite index on `(entity_type, entity_id)` for event lookup
- Index on `event_timestamp DESC` for timeline queries
- Indexes on `actor_id`, `session_id`, export `status`

## Service API (Internal)

| Service | Key Operations |
|---------|----------------|
| `AuditEventService` | Create, get, list by entity/actor/session/status (append-only) |
| `AuditActorService` | CRUD, list by user, list all |
| `AuditEntityService` | CRUD, lookup by entity type/id |
| `AuditChangeService` | Create, get, list by audit event (append-only) |
| `AuditSessionService` | CRUD, get by sessionId, `endSession` |
| `AuditExportService` | Create, update, soft-delete (blocked when completed) |

## Business Rules

- **Duplicate event codes rejected** — `DuplicateAuditEventException`
- **Append-only audit events** — no update or soft-delete on `AuditEvent`
- **Append-only change records** — no update on `AuditChange`; must reference a valid `AuditEvent`
- **Session logout validation** — `logoutTime` must be after `loginTime`
- **Immutable completed exports** — no update or soft-delete when status is `COMPLETED`
- **Optimistic locking** via `version` on mutable entities
- **Soft-delete** sets `deleted = true`, `active = false`

## Domain Events

| Event | Purpose |
|-------|---------|
| `AuditEventCreatedEvent` | Audit event recorded |
| `AuditSessionStartedEvent` | User session opened |
| `AuditSessionEndedEvent` | User session closed |
| `AuditExportCompletedEvent` | Export request completed |

Events are plain records; Spring event publishing is deferred.

## Future Integration

- Workflow engine emits `AuditEvent` on step transitions
- Complaint module records entity changes via `AuditChange`
- Search (SRH) indexes audit events for dashboard queries
- Security module attaches actor/session context automatically
- Scheduled export job processes `AuditExport` requests
- Spring ApplicationEventPublisher for cross-module audit propagation

## Out of Scope

- REST controllers and HTTP APIs
- Spring Security integration
- AOP / Hibernate entity listeners
- Automatic change tracking
- Kafka / message streaming
- Elasticsearch / search indexing
- Report generation engine
- Scheduler for export processing
- Complaint-specific audit logic

## Platform Foundation Status

| Module | Status |
|--------|--------|
| Infrastructure | ✅ |
| MDM | ✅ |
| IDM | ✅ |
| ORG | ✅ |
| DOC | ✅ |
| NTF | ✅ |
| WRK | ✅ |
| **Audit (AUD)** | ✅ |
| Search (SRH) | 🔜 |
| Security | 🔜 |
