# GPS-001 — 07 Flyway Standard

---

## 1. Location

```
govos-infrastructure/src/main/resources/db/migration/
```

All bounded contexts share this directory. **Never** place Flyway scripts in domain modules.

---

## 2. Naming Convention

```
V{major}_{minor}_{patch}__{description}.sql
```

| Component | Rule | Example |
|-----------|------|---------|
| Version | Semantic, underscores | `V2_1_0` |
| Separator | Double underscore | `__` |
| Description | snake_case, context name | `search`, `complaint` |

Examples:
- `V1__baseline.sql`
- `V1_2_0__identity.sql`
- `V2_1_0__search.sql`

---

## 3. Migration Header Block

Every migration must start with a comment block:

```sql
-- =============================================================================
-- GovOS Flyway Migration V2.1.0 — Search (SRH)
-- =============================================================================
-- Version     : 2.1.0
-- Purpose     : Search platform domain schema
-- Author      : GovOS Platform Team
-- Architecture: SRH-001 (frozen), SRH-002.1 entities
-- Dependencies: V1__baseline.sql, V1_1_0 through V1_7_0
-- ADR         : ADR-002 Modular Monolith, ADR-005 PostgreSQL, ADR-008 Flyway
-- Notes       : Cross-module references are UUID columns only
-- =============================================================================
```

---

## 4. Rules

| Rule | Detail |
|------|--------|
| **Immutable** | Never modify applied migrations |
| **Forward only** | Fix with new migration |
| **Idempotent DDL** | Use `IF NOT EXISTS` where supported |
| **No DML seeds** | Reference data via separate process or dedicated seed migration with ADR |
| **One context per file** | Prefer separate files per bounded context |
| **Dependencies** | Document prerequisite versions in header |

---

## 5. Version Numbering

| Range | Context |
|-------|---------|
| V1.x | Platform foundation (MDM, IDM, ORG, DOC, WRK, NTF, AUD) |
| V2.0.x | First product (CMP) |
| V2.1.x | Search (SRH) |
| V2.2.x+ | Future contexts |

Coordinate version bumps with platform release planning.

---

## 6. Testing

- `FlywayMigrationNamingTest` validates naming conventions
- Migrations applied on application startup (Spring Boot Flyway auto-config)
- Test containers recommended for integration tests requiring schema

---

## 7. Rollback

Flyway Community does not support rollback scripts in GovOS baseline. **Forward-fix only** with compensating migration.

---

## 8. Prohibited

- Flyway scripts in `govos-domain`
- Manual schema changes in production without migration
- DROP TABLE without architecture approval and ADR
