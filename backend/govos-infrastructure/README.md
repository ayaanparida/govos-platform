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

See [db/migration/README.md](src/main/resources/db/migration/README.md) for the full strategy.

### Version Scheme

```
V{major}_{minor}_{patch}__{description}.sql
```

Sprint 0 uses major `1` with minor numbers assigned per bounded context (1.1 = MDM, 1.2 = IDM, …). Patch versions (`1.3.1`, `1.5.1`) are for non-breaking refinements within a module.

| Version | File | Scope |
|---------|------|-------|
| 1.0.0 | `V1__baseline.sql` | Extensions + `govos` schema |
| 1.1.0 | `V1_1_0__master_data.sql` | MDM (`mdm_*`) |
| 1.2.0 | `V1_2_0__identity.sql` | IDM (`idm_*`) |
| 1.3.0 | `V1_3_0__organization.sql` | ORG (`org_*`) |
| 1.3.1 | `V1_3_1__organization_refinements.sql` | ORG refinements |
| 1.4.0 | `V1_4_0__document_management.sql` | DOC (`doc_*`) |
| 1.5.0 | `V1_5_0__notification.sql` | NTF (`ntf_*`) |
| 1.5.1 | `V1_5_1__notification_refinements.sql` | NTF refinements |
| 1.6.0 | `V1_6_0__workflow.sql` | WRK (`wrk_*`) |

**Rules:**
- Migration files are immutable once merged
- Never use `ddl-auto: create` or `update`
- One migration per schema change; coordinate version numbers in PRs
- Flyway: `validateOnMigrate`, `validateMigrationNaming`, `outOfOrder=false`, `cleanDisabled=true`

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
