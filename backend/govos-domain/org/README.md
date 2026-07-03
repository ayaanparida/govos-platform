# GovOS Organization Management (ORG)

Sprint 0 Day 5 — Organization bounded context for the GovOS platform.

## Overview

The ORG module models government organizational structure: departments, offices, designations, employee postings, and user-to-organization membership. It links identity users (`idm_user`) to organizational context for multi-tenant government operations.

This module provides the **domain layer only** — no REST controllers or APIs are exposed in Sprint 0 Day 5.

## Module Location

| Artifact | Package | Responsibility |
|----------|---------|----------------|
| `govos-domain` | `com.govos.org` | ORG entities, services, repositories, DTOs, mappers |
| `govos-infrastructure` | `db/migration` | Flyway `V1_3_0__organization.sql`, `V1_3_1__organization_refinements.sql` |

## Package Structure

```
com.govos.org
├── config          # Bounded-context configuration
├── controller      # Reserved for future REST layer (empty)
├── dto             # Request/response records
├── entity          # JPA entities
├── exception       # Domain exceptions
├── mapper          # MapStruct entity ↔ DTO mapping
├── mdm             # MDM type constants (e.g. ORGANIZATION_TYPE)
├── repository      # Spring Data JPA repositories
├── service         # Service interfaces and implementations
└── validator       # Business validation rules
```

## Entity Model

All entities extend `AuditableEntity` (`govos-common`), except `EmployeeNumberSequence` (technical sequence table).

| Entity | Table | Description |
|--------|-------|-------------|
| `Organization` | `org_organization` | Top-level government body or agency |
| `Department` | `org_department` | Department within an organization |
| `Office` | `org_office` | Physical office under a department |
| `Designation` | `org_designation` | Job title and grade |
| `Employee` | `org_employee` | User posting to org structure |
| `UserOrganization` | `org_user_organization` | User membership and default org |
| `DepartmentHierarchy` | `org_department_hierarchy` | Explicit parent-child dept edges |
| `EmployeeNumberSequence` | `org_employee_number_sequence` | Yearly counter for business employee numbers |

### Cross-Domain References

- `Employee.user` → `com.govos.idm.entity.User`
- `UserOrganization.user` → `com.govos.idm.entity.User`
- `Organization.type` → MDM `ORGANIZATION_TYPE` (validated via `MasterDataRepository`)

## Design Decisions

### Employee Number Strategy

- **Primary key:** UUID (`id` on `Employee`)
- **Business identifier:** `employeeNumber` — auto-generated, immutable after creation
- **Format:** `EMP-YYYY-NNNNNN` (e.g. `EMP-2026-000001`)
- **Generation:** `EmployeeNumberGenerator` with pessimistic lock on `org_employee_number_sequence`
- Clients must **not** supply `employeeNumber` on create or update

### Department Hierarchy

Unlimited nesting depth — no fixed levels (Government → Revenue → District Office → Tahasil → …).

Two complementary mechanisms:

1. **`Department.parentDepartment`** — primary tree edge (self-reference)
2. **`DepartmentHierarchy`** — optional explicit parent-child edges for complex structures

`DepartmentTreeValidator` enforces same-organization scope and cycle detection across both mechanisms.

### Office Geo Coordinates

`Office.latitude` and `Office.longitude` (`NUMERIC(10,7)`) store WGS84 coordinates for future GIS features. Partial index `idx_org_office_geo_coordinates` supports geo queries.

### Organization Types (MDM, not enums)

`Organization.type` is a **string reference** to MDM master data type `ORGANIZATION_TYPE`.

Examples (configured in MDM, not hard-coded):

- `STATE_GOVERNMENT`
- `DISTRICT`
- `MUNICIPALITY`
- `PANCHAYAT`
- `UNIVERSITY`
- `HOSPITAL`

`OrganizationValidator` rejects types not present in MDM. `OrganizationStatus` remains an enum for workflow state (pending/active/suspended).

## Database

| Migration | Version | Purpose |
|-----------|---------|---------|
| `V1_3_0__organization.sql` | 1.3.0 | Core ORG tables |
| `V1_3_1__organization_refinements.sql` | 1.3.1 | Employee sequence, MDM type column, GIS index |

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
- Employee numbers auto-generated; not client-supplied
- Organization type must exist in MDM `ORGANIZATION_TYPE`
- Department parent assignment and hierarchy edges: same org, no cycles, unlimited depth
- Only one default organization per user at a time
- Optimistic locking via `version` on update operations
- Soft-delete sets `deleted = true`, `active = false`

## Out of Scope (Sprint 0 Day 5)

- REST controllers and HTTP APIs
- Security, JWT, authentication (`govos-security` — next after ORG foundation)
- Complaint bounded context
- Sample/seed data

## Platform Foundation Status

| Module | Status |
|--------|--------|
| Infrastructure | ✅ |
| MDM | ✅ |
| Identity (IDM) | ✅ |
| Organization (ORG) | ✅ |
| Security | Next |

With Users, Roles, Permissions, Employees, and Organizations in place, the security module can focus on authentication and authorization rather than domain modeling.

## Next Steps

- Seed MDM `ORGANIZATION_TYPE` values
- Sprint 0 Day 6+: ORG REST controllers in `govos-api`
- `govos-security` module
- OpenAPI documentation for organization endpoints
- Admin portal org structure management UI
