# Changelog

All notable changes to the GovOS Platform are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Planned

- Search (SRH) bounded context
- Security module (`govos-security`)
- REST API layer (`govos-api` controllers)
- Angular workspace
- Complaint (CMP) bounded context
- Service unit tests for ORG, DOC, NTF, and WRK modules

## [0.1.0-alpha] - 2026-07-04

### Added

- Maven multi-module backend (Java 21, Spring Boot 3.5.16, PostgreSQL 16, Flyway, MapStruct)
- Docker Compose stack for local PostgreSQL development
- **Infrastructure** — persistence, JPA, Flyway, HikariCP, physical naming strategy
- **Master Data (MDM)** — `com.govos.mdm` reference data foundation
- **Identity (IDM)** — users, roles, permissions, assignments, login/password history, refresh tokens
- **Organization (ORG)** — organizations, departments, offices, employees, designations
- **Document Management (DOC)** — folders, documents, versions, tags, storage provider abstraction
- **Notification (NTF)** — channels, templates, delivery tracking, queue metadata, provider stubs
- **Workflow (WRK)** — generic workflow definition, version, step, transition, instance, task foundation
- **Audit (AUD)** — audit events, actors, entities, field-level changes, sessions, export requests
- Flyway migrations `V1` through `V1_7_0` (schema version **1.7.0**)
- 45 Spring Data JPA repositories across bounded contexts
- GitHub Actions CI workflow (`mvn verify` on pull requests to `develop`)
- Pull request template with merge quality gate checklist
- JaCoCo coverage gate (80% line coverage on MDM, IDM, and AUD service packages)
- Maven Enforcer (Java 21+, Maven 3.9+, dependency convergence)
- Flyway migration strategy documentation and naming validation test
- 142 unit tests (MDM, IDM, AUD service layers)
- Bounded-context README files (MDM, ORG, DOC, NTF, WRK, AUD)

### Known Limitations

- No REST controllers or public HTTP APIs (domain layer only)
- No Spring Security, JWT, or authentication enforcement
- No workflow execution engine, scheduler, or state machine
- No notification delivery engine or external provider integrations
- No search indexing (Elasticsearch or equivalent)
- No automatic audit capture (AOP / entity listeners deferred)
- No Angular or web UI
- No complaint or other business-process modules
- ORG, DOC, NTF, and WRK modules lack service-layer unit tests (grandfathered under Sprint 0)

[0.1.0-alpha]: https://github.com/ayaanparida/govos-platform/releases/tag/v0.1.0-alpha
