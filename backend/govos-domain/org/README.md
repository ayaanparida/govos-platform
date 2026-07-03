# GovOS Organization Management (ORG)

Sprint 0 Day 5 — Organization bounded context for the GovOS platform.

## Overview

The ORG module models government organizational structure: departments, offices, designations, employee postings, and user-to-organization membership. It links identity users (`idm_user`) to organizational context for multi-tenant government operations.

This module provides the **domain layer only** — no REST controllers or APIs are exposed in Sprint 0 Day 5.

## Module Location

| Artifact | Package | Responsibility |
|----------|---------|----------------|
| `govos-domain` | `com.govos.org` | ORG entities, services, repositories, DTOs, mappers |
| `govos-infrastructure` | `db/migration` | Flyway migration `V1_3_0__organization.sql` |

## Package Structure

```
com.govos.org
├── config          # Bounded-context configuration
├── controller      # Reserved for future REST layer (empty)
├── dto             # Request/response records
├── entity          # JPA entities
├── exception       # Domain exceptions
├── mapper          # MapStruct entity ↔ DTO mapping
├── repository      # Spring Data JPA repositories
├── service         # Service interfaces and implementations
└── validator       # Business validation rules
```

## Entity Model

All entities extend `AuditableEntity` (`govos-common`).

| Entity | Table | Description |
|--------|-------|-------------|
| `Organization` | `org_organization` | Top-level government body or agency |
| `Department` | `org_department` | Department within an organization |
| `Office` | `org_office` | Physical office under a department |
| `Designation` | `org_designation` | Job title and grade |
| `Employee` | `org_employee` | User posting to org structure |
| `UserOrganization` | `org_user_organization` | User membership and default org |
| `DepartmentHierarchy` | `org_department_hierarchy` | Explicit parent-child dept edges |

### Cross-Domain References

- `Employee.user` → `com.govos.idm.entity.User`
- `UserOrganization.user` → `com.govos.idm.entity.User`

## Database

**Migration:** `V1_3_0__organization.sql` (schema version **1.3.0**)

Partial unique indexes support soft-delete and:

- One default organization per user (`default_organization = true`)
- Unique employee numbers among active records
- Unique hierarchy edges per parent-child pair

## Service API (Internal)

| Service | Key Operations |
|---------|----------------|
| `OrganizationService` | CRUD, get by code |
| `DepartmentService` | CRUD, list by organization/parent |
| `OfficeService` | CRUD, list by department |
| `DesignationService` | CRUD, get by code |
| `EmployeeService` | CRUD, list by org/user, get by employee number |
| `UserOrganizationService` | Assign, revoke, set default org |
| `DepartmentHierarchyService` | Create/remove hierarchy edges |

### Business Rules

- Duplicate codes rejected within scope (org, dept per org, office per dept)
- Duplicate employee numbers rejected
- System prevents self-referencing department hierarchy
- Only one default organization per user at a time
- Optimistic locking via `version` on update operations
- Soft-delete sets `deleted = true`, `active = false`

## Out of Scope (Sprint 0 Day 5)

- REST controllers and HTTP APIs
- Security, JWT, authentication
- Complaint bounded context
- Sample/seed data

## Next Steps

- Sprint 0 Day 6+: ORG REST controllers in `govos-api`
- OpenAPI documentation for organization endpoints
- Admin portal org structure management UI
