# GovOS Platform Development Standard (GPS-001)

**Document ID:** GPS-001  
**Version:** 1.0.0  
**Status:** Approved — Engineering Baseline  
**Scope:** All GovOS bounded contexts and platform modules

---

## Purpose

GPS-001 is the **official engineering baseline** for the GovOS Enterprise Government Platform. Every bounded context (SRH, DOC, IDM, ORG, WRK, AUD, NTF, MDM, CMP, and future modules) must conform to these standards.

GPS-001 is **documentation only** — not a product module, not a bounded context, and not executable code.

---

## Standard Documents

| # | Document | Topic |
|---|----------|-------|
| 01 | [Platform Overview](./01_PLATFORM_OVERVIEW.md) | Vision, modules, modular monolith |
| 02 | [Architecture Principles](./02_ARCHITECTURE_PRINCIPLES.md) | DDD, hexagonal, SOLID, CQRS |
| 03 | [Bounded Context Guidelines](./03_BOUND_CONTEXT_GUIDELINES.md) | Ownership, dependencies, ACL |
| 04 | [Package Structure Standard](./04_PACKAGE_STRUCTURE_STANDARD.md) | Standard packages per context |
| 05 | [Naming Conventions](./05_NAMING_CONVENTIONS.md) | Classes, tables, APIs, permissions |
| 06 | [Database Standard](./06_DATABASE_STANDARD.md) | Schema, columns, indexes |
| 07 | [Flyway Standard](./07_FLYWAY_STANDARD.md) | Migration naming and rules |
| 08 | [Entity Standard](./08_ENTITY_STANDARD.md) | JPA entities, audit, soft delete |
| 09 | [Repository Standard](./09_REPOSITORY_STANDARD.md) | Spring Data JPA patterns |
| 10 | [Service Standard](./10_SERVICE_STANDARD.md) | Domain services |
| 11 | [Application Layer Standard](./11_APPLICATION_LAYER_STANDARD.md) | Application services in govos-api |
| 12 | [DTO Standard](./12_DTO_STANDARD.md) | Request/response objects |
| 13 | [Mapper Standard](./13_MAPPER_STANDARD.md) | MapStruct conventions |
| 14 | [Validation Standard](./14_VALIDATION_STANDARD.md) | Jakarta Validation + validators |
| 15 | [Exception Standard](./15_EXCEPTION_STANDARD.md) | Typed domain exceptions |
| 16 | [Event Standard](./16_EVENT_STANDARD.md) | Domain events |
| 17 | [REST API Standard](./17_REST_API_STANDARD.md) | HTTP conventions |
| 18 | [Security Standard](./18_SECURITY_STANDARD.md) | JWT, RBAC, secrets |
| 19 | [Observability Standard](./19_OBSERVABILITY_STANDARD.md) | Metrics, tracing, logging |
| 20 | [Testing Standard](./20_TESTING_STANDARD.md) | JUnit, Mockito, JaCoCo |
| 21 | [Configuration Standard](./21_CONFIGURATION_STANDARD.md) | Properties and profiles |
| 22 | [Logging Standard](./22_LOGGING_STANDARD.md) | Structured logging, PII |
| 23 | [Scheduler Standard](./23_SCHEDULER_STANDARD.md) | Spring Scheduling |
| 24 | [AI Integration Standard](./24_AI_INTEGRATION_STANDARD.md) | Providers, embeddings |
| 25 | [Deployment Standard](./25_DEPLOYMENT_STANDARD.md) | Docker, Kubernetes |
| 26 | [Release Standard](./26_RELEASE_STANDARD.md) | Versioning, certification |
| 27 | [Code Review Checklist](./27_CODE_REVIEW_CHECKLIST.md) | Enterprise review gate |
| 28 | [Platform Roadmap](./28_PLATFORM_ROADMAP.md) | Module maturity matrix |

---

## Quick Reference

| Area | Standard |
|------|----------|
| Java | 21 (LTS) |
| Spring Boot | 3.5.x |
| Architecture | Modular monolith, DDD, hexagonal |
| API base path | `/api/v1/{context}` |
| Database schema | `govos` |
| Table prefix | `{ctx}_` (e.g. `srh_`, `cmp_`) |
| Auth | JWT Bearer + method security |
| Mapping | MapStruct (no Lombok on entities) |
| Migrations | Flyway in `govos-infrastructure` |
| Coverage | JaCoCo gates per module |

---

## Compliance

New bounded contexts must reference GPS-001 in their module README. Platform certification (e.g. SRH Release-1.0) requires GPS-001 compliance review.

---

## Document Control

| Field | Value |
|-------|-------|
| Owner | GovOS Platform Architecture |
| Created | 2026-07-18 |
| Type | Documentation standard |
| Code changes | None |
