# GovOS Infrastructure Module

Persistence and database infrastructure for the GovOS platform.

## Responsibility

| Concern | Implementation |
|---------|----------------|
| PostgreSQL datasource | HikariCP via `DatasourceConfig` |
| JPA / Hibernate | `JpaConfig` |
| Flyway migrations | `FlywayConfig` + `db/migration/` |
| Physical naming | `GovosPhysicalNamingStrategy` |
| JPA auditing (infra) | `JpaAuditingConfig` |
| Constants | `PersistenceConstants` |

This module does **not** contain business entities, repositories, or REST APIs.

## Package Structure

```
com.govos.infrastructure
├── config          Auto-configuration, datasource, JPA, Flyway
├── datasource      PostgreSQL connection package
├── flyway          Migration package marker
├── jpa             Hibernate naming strategy
├── audit           JPA auditing infrastructure
├── persistence     Shared persistence constants
└── util            Naming utilities
```

## Database Conventions

| Item | Convention |
|------|------------|
| Schema | `govos` |
| Tables | `{domain}_{entity}` — e.g. `idm_user`, `org_organization` |
| Columns | `snake_case` |
| Pluralization | Disabled |
| Primary keys | UUID (`gen_random_uuid()`) |
| Migrations | Flyway only — `ddl-auto: validate` |

## Flyway Migrations

| Version | File | Scope |
|---------|------|-------|
| V1 | `V1__baseline.sql` | Extensions + `govos` schema |
| V2–V9 | Reserved | Identity (`idm_*`) |
| V10–V19 | Reserved | Organization (`org_*`) |
| V20–V29 | Reserved | Audit (`aud_*`) |

**Rules:**
- Migration files are immutable once merged
- Never use `ddl-auto: create` or `update`
- One migration per schema change

## Hibernate Settings

| Property | Value |
|----------|-------|
| `ddl-auto` | `validate` |
| `show_sql` | `false` |
| `format_sql` | `true` |
| `jdbc.batch_size` | `50` |
| `order_inserts` | `true` |
| `order_updates` | `true` |
| `open-in-view` | `false` |
| `generate_statistics` | `false` |

## Local Development

Ensure Docker infrastructure is running:

```bash
cd docker
docker compose up -d postgres
```

Default connection (overridable via environment variables):

| Property | Default |
|----------|---------|
| URL | `jdbc:postgresql://localhost:5432/govos` |
| Username | `govos` |
| Password | `govos_dev_password` |
| Schema | `govos` |

## Activation

Add the module as a dependency in `govos-api`:

```xml
<dependency>
    <groupId>com.govos</groupId>
    <artifactId>govos-infrastructure</artifactId>
</dependency>
```

Auto-configuration is registered via:

```
META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

## Related ADRs

- [ADR-005 PostgreSQL](../../../govos-architecture/docs/90-adr/adr-005-postgresql.md)
- [ADR-008 Flyway](../../../govos-architecture/docs/90-adr/adr-008-flyway.md)
