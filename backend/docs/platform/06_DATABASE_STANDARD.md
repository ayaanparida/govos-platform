# GPS-001 — 06 Database Standard

---

## 1. Database Engine

- **Primary:** PostgreSQL 15+
- **Schema:** `govos` (single schema, logical separation by table prefix)
- **Extensions:** `pgcrypto` for `gen_random_uuid()`

---

## 2. Table Design

Every aggregate table includes **standard audit columns**:

| Column | Type | Required | Notes |
|--------|------|----------|-------|
| `id` | UUID | Yes | PK, `gen_random_uuid()` default |
| `active` | BOOLEAN | Yes | Default `TRUE` |
| `deleted` | BOOLEAN | Yes | Soft delete, default `FALSE` |
| `version` | BIGINT | Yes | Optimistic locking, default `0` |
| `created_by` | VARCHAR(100) | Yes | Username |
| `created_date` | TIMESTAMPTZ | Yes | UTC |
| `updated_by` | VARCHAR(100) | Yes | Username |
| `updated_date` | TIMESTAMPTZ | Yes | UTC |
| `code` | VARCHAR(100) | Optional | Business key |

Mapped via `AuditableEntity` in Java.

---

## 3. Column Naming

- **snake_case** only
- Boolean: `is_*` avoided — use `active`, `deleted`, `enabled`
- Timestamps: `*_date` or `*_at` (consistent per table)
- UUIDs: `*_id` suffix
- JSON: `JSONB` type for flexible metadata
- Enums: `VARCHAR(30)` storing enum name

---

## 4. Soft Delete

- **Never** physical DELETE in application code for business entities
- Set `deleted = TRUE`, preserve row
- All queries filter `deleted = FALSE` unless admin restore
- **No ON DELETE CASCADE** — application-managed lifecycle

---

## 5. Cross-Module References

- Store **UUID only** — no foreign keys to other bounded context tables
- Example: `organization_id UUID NOT NULL` — no FK to `org_organization`
- Rationale: modular monolith → future service extraction

---

## 6. Indexes

Create indexes for:

- Foreign-scoped columns: `organization_id`
- Lookup keys: `code`, `status`
- Soft-delete filter: composite with `deleted`
- Query patterns documented in module README

Naming: `idx_{table}_{column(s)}`

---

## 7. Constraints

| Type | Naming |
|------|--------|
| Primary key | `pk_{table}` |
| Unique | `uk_{table}_{columns}` |
| Check | `ck_{table}_{rule}` |

Use CHECK constraints for enum-like VARCHAR columns where appropriate.

---

## 8. Migrations Location

All DDL in:

```
govos-infrastructure/src/main/resources/db/migration/
```

One migration file per bounded context major version increment.

---

## 9. Prohibited

- Multiple schemas per context (use prefix instead)
- Cross-context FK constraints
- Storing secrets in database
- `TEXT` for bounded fields — use explicit VARCHAR lengths
