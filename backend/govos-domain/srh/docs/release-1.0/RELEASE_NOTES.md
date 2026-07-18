# SRH Release Notes — v1.0.0

**Release:** SRH v1.0.0 (Platform Certified)  
**Date:** 2026-07-18  
**Type:** Platform freeze — documentation and certification only (no feature changes)

---

## Summary

GovOS Search (SRH) v1.0.0 certifies the Search bounded context as a reusable platform service consumable by all GovOS products. This release completes milestones **SRH-001 through SRH-020**.

SRH v1.0.0 is **feature complete**. No new search capabilities are included in this release artifact.

---

## Certified Milestones

| Sprint | Capability |
|--------|------------|
| SRH-001 | Architecture blueprint — platform ownership, DDD boundaries |
| SRH-002 | Entities, enums, Flyway schema, repositories |
| SRH-003 | DTOs, MapStruct mappers, validators, exceptions |
| SRH-004 | `SearchIndexService` + `SearchEngineProvider` abstraction |
| SRH-005 | `SearchQueryService` — search, facets, pagination |
| SRH-006 | `SearchAdministrationService` + alias management |
| SRH-007 | Sync jobs + bulk reindex |
| SRH-008 | Domain event records (contracts) |
| SRH-009 | Unit tests + JaCoCo coverage gates |
| SRH-010 | `SearchApplicationService` orchestration layer |
| SRH-011 | REST API — `SearchController` |
| SRH-012 | OpenSearch integration |
| SRH-013 | Product integration — CMP consumer |
| SRH-014 | Advanced query API |
| SRH-015 | Administration — monitoring, analytics, dashboards |
| SRH-016 | AI semantic search — hybrid ranking |
| SRH-017 | Production hardening — resilience, cache, metrics |
| SRH-018 | Production AI providers — OpenAI, Azure, Ollama |
| SRH-019 | Scheduler & automation |
| SRH-020 | Observability & distributed tracing |

---

## What's Included

- Full-text, facet, autocomplete, geo, semantic, and hybrid search
- Index/document/alias/sync-job lifecycle management
- OpenSearch engine adapter with alias rotation
- Embedding providers (mock, OpenAI, Azure OpenAI, Ollama)
- Production resilience, caching, guards, metrics
- Spring scheduler for operational jobs
- OpenTelemetry/Micrometer observability
- 70+ REST endpoints under `/api/v1/search`
- Comprehensive certification documentation (this release)

---

## What's Not Included (Post v1.0)

- Additional product consumers beyond CMP
- IDM permission seed data (SRH_* declared in code; assignment external)
- Async indexing / message-driven sync
- Multi-engine adapters (Solr, Elasticsearch native)
- Frontend search UI components

---

## Upgrade Notes

First stable release — no prior SRH version to upgrade from. Deploy alongside:

- PostgreSQL (metadata)
- OpenSearch 2.x cluster
- GovOS Platform API v0.1.0+ with JWT security

---

## Verification

```
mvn -pl govos-api -am test
→ BUILD SUCCESS
```

---

## Documentation Index

See [CERTIFICATION_INDEX.md](./CERTIFICATION_INDEX.md) for the full documentation set.
