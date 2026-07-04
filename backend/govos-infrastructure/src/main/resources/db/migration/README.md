# GovOS Flyway Migrations

Versioned SQL schema changes for the `govos` PostgreSQL schema.

## Naming Convention

```
V{major}_{minor}_{patch}__{description}.sql
```

| Segment | Rule | Example |
|---------|------|---------|
| `major` | Platform schema generation | `1` |
| `minor` | Bounded context / module | `2` = IDM |
| `patch` | Increment within module (`0` = initial, `1+` = refinements) | `1` = refinement |
| `description` | Lowercase snake_case summary | `identity`, `organization_refinements` |

Examples:

```
V1__baseline.sql
V1_1_0__master_data.sql
V1_2_0__identity.sql
V1_3_1__organization_refinements.sql
V1_6_0__workflow.sql
```

Flyway `validateMigrationNaming` is enabled — files must match Flyway versioned migration format.

## Version Assignment (Sprint 0)

| Version | Module | Prefix | File |
|---------|--------|--------|------|
| 1.0.0 | Infrastructure baseline | — | `V1__baseline.sql` |
| 1.1.0 | Master Data (MDM) | `mdm_` | `V1_1_0__master_data.sql` |
| 1.2.0 | Identity (IDM) | `idm_` | `V1_2_0__identity.sql` |
| 1.3.0 | Organization (ORG) | `org_` | `V1_3_0__organization.sql` |
| 1.3.1 | Organization refinements | `org_` | `V1_3_1__organization_refinements.sql` |
| 1.4.0 | Document (DOC) | `doc_` | `V1_4_0__document_management.sql` |
| 1.5.0 | Notification (NTF) | `ntf_` | `V1_5_0__notification.sql` |
| 1.5.1 | Notification refinements | `ntf_` | `V1_5_1__notification_refinements.sql` |
| 1.6.0 | Workflow (WRK) | `wrk_` | `V1_6_0__workflow.sql` |

### Next Modules (reserved)

| Version | Module | Prefix |
|---------|--------|--------|
| 1.7.0 | Search (SRH) | `srh_` |
| 1.8.0 | Audit (AUD) | `aud_` |
| 1.9.0 | Security | `sec_` |

Coordinate version numbers in PRs to avoid conflicts. One bounded context per minor version; use patch versions for non-breaking refinements within the same module.

## Rules

1. **Immutable** — never edit a migration after merge to `develop`/`main`
2. **Forward-fix only** — schema mistakes are corrected with a new patch/minor migration
3. **No Hibernate DDL** — `ddl-auto: validate` only; entities must match Flyway schema
4. **Single schema** — all tables in `govos` schema
5. **Table prefix** — `{domain}_{entity}` (e.g. `idm_user`, `wrk_workflow_definition`)
6. **Soft-delete indexes** — partial unique indexes use `WHERE deleted = FALSE`
7. **Comments** — header block with module, ADR references, and purpose

## Migration Template

```sql
-- =============================================================================
-- GovOS Flyway Migration V{x.y.z} — {Module Name} ({CODE})
-- =============================================================================
-- Domain    : {CODE}
-- ADR       : ADR-002 Modular Monolith, ADR-008 Flyway
-- =============================================================================

CREATE TABLE govos.{prefix}_{entity} (
    id              UUID            NOT NULL,
    ...
    CONSTRAINT pk_{prefix}_{entity} PRIMARY KEY (id)
);
```

## Pre-Merge Checklist

- [ ] Version number is unique and follows module assignment table
- [ ] JPA entities match column names and types exactly
- [ ] FK references point to existing tables from earlier migrations
- [ ] Partial unique indexes account for soft-delete
- [ ] Local `mvn verify` passes; app starts with Flyway validate
- [ ] `FlywayMigrationNamingTest` passes (naming + uniqueness)

## CI Validation

- Spring Boot runs Flyway on startup with `validateOnMigrate=true`
- `FlywayMigrationNamingTest` validates file naming and version uniqueness without a database
