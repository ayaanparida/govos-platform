# GovOS Master Data Management (MDM)

Sprint 0 Day 3 — Master Data bounded context for the GovOS platform.

## Overview

The MDM module manages reference data used across GovOS domains: lookup values, configuration codes, and enumerations. Examples include complaint statuses, document types, and jurisdiction codes.

This module provides the **domain layer only** — no REST controllers or APIs are exposed in Sprint 0 Day 3.

## Module Location

| Artifact | Package | Responsibility |
|----------|---------|----------------|
| `govos-common` | `com.govos.common.entity` | Reusable `BaseEntity` and `AuditableEntity` hierarchy |
| `govos-domain` | `com.govos.mdm` | MDM entities, services, repositories, DTOs, mappers |
| `govos-infrastructure` | `db/migration` | Flyway migration `V1_1_0__master_data.sql` |

## Package Structure

```
com.govos.mdm
├── config          # Bounded-context configuration
├── controller      # Reserved for future REST layer (empty)
├── dto             # Request/response records
├── entity          # MasterData JPA entity
├── exception       # Domain exceptions
├── mapper          # MapStruct entity ↔ DTO mapping
├── repository      # Spring Data JPA repositories
├── service         # Service interface and implementation
└── validator       # Business validation rules
```

## Entity Model

### Base Hierarchy (`govos-common`)

| Class | Fields |
|-------|--------|
| `BaseEntity` | `id` (UUID), `code`, `active`, `deleted`, `version` |
| `AuditableEntity` | extends `BaseEntity` + `createdBy`, `createdDate`, `updatedBy`, `updatedDate` |

Auditing is enabled via Spring Data JPA (`JpaAuditingConfig` in `govos-infrastructure`).

### MasterData

| Field | Type | Description |
|-------|------|-------------|
| `type` | String | Category grouping (e.g. `COMPLAINT_STATUS`) |
| `key` | String | Unique key within type (mapped to `data_key` column) |
| `value` | String | Display or stored value (mapped to `data_value` column) |
| `description` | String | Optional description |
| `displayOrder` | Integer | Sort order within type |
| `systemDefined` | Boolean | Protected from update/delete when `true` |
| `active` | Boolean | Inherited from `BaseEntity` |

Unique constraint: `(type, data_key)` where `deleted = false` is enforced at application level; database enforces `(type, data_key)` globally.

## Database

**Table:** `govos.mdm_master_data`  
**Migration:** `V1_1_0__master_data.sql`  
**Primary key:** UUID

Column `key` and `value` are stored as `data_key` and `data_value` to avoid PostgreSQL reserved-word conflicts.

## Service API (Internal)

`MasterDataService` provides:

- `getById(UUID id)`
- `getByTypeAndKey(String type, String key)`
- `getByType(String type)` — ordered by `displayOrder`
- `create(CreateMasterDataRequest request)`
- `update(UUID id, UpdateMasterDataRequest request)` — optimistic locking via `version`
- `softDelete(UUID id)` — sets `deleted = true`, `active = false`

### Business Rules

- Duplicate `(type, key)` pairs are rejected.
- System-defined records cannot be updated or soft-deleted.
- Soft-deleted records are excluded from all read operations.

## Dependencies

`govos-domain` requires:

- `govos-common` — base entity hierarchy
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-validation`
- MapStruct 1.6.3

## Runtime Wiring

Add `govos-domain` as a dependency of `govos-api` so entities, repositories, and services are on the classpath:

```xml
<dependency>
    <groupId>com.govos</groupId>
    <artifactId>govos-domain</artifactId>
</dependency>
```

Entity and repository scanning is configured in `govos-infrastructure` for the `com.govos` base package.

## Out of Scope (Sprint 0 Day 3)

- REST controllers and HTTP APIs
- Security and authentication
- Sample/seed data
- Other bounded contexts (Complaint, Organization, Workflow)

## Next Steps

- Sprint 0 Day 4+: MDM REST controllers in `govos-api`
- OpenAPI documentation for master data endpoints
- Admin portal UI for master data management
