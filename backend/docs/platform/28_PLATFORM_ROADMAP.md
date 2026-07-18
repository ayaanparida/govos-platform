# GPS-001 — 28 Platform Roadmap

**Last updated:** 2026-07-18  
**GPS-001 status:** Approved v1.0.0

---

## 1. Platform Module Maturity

| Module | Code | Type | Status | Release |
|--------|------|------|--------|---------|
| Infrastructure | INF | Platform | ✅ Active | Flyway V1 baseline |
| Master Data | MDM | Platform | ✅ Active | V1_1_0 |
| Identity | IDM | Platform | ✅ Active | V1_2_0 |
| Organization | ORG | Platform | ✅ Active | V1_3_0 |
| Document | DOC | Platform | ✅ Active | V1_4_0 |
| Notification | NTF | Platform | ✅ Active | V1_5_0 |
| Workflow | WRK | Platform | ✅ Active | V1_6_0 |
| Audit | AUD | Platform | ✅ Active | V1_7_0 |
| **Search** | **SRH** | **Platform** | **✅ v1.0 Certified** | **SRH-001–020** |
| Security | SEC | Platform | 🔜 Planned | IDM permission seed |
| Complaints | CMP | Product | ✅ Active | V2_0_0, CMP-015 search |

---

## 2. Engineering Standards

| Standard | Status |
|----------|--------|
| GPS-001 Platform Development Standard | ✅ v1.0.0 (this document) |
| Product architecture ADR | ✅ govos-architecture |
| PostgreSQL ADR (ADR-005) | ✅ Active |
| Modular monolith ADR (ADR-002) | ✅ Active |
| Flyway ADR (ADR-008) | ✅ Active |

---

## 3. Bounded Context Roadmap

### Platform (Next)

| Priority | Module | Focus |
|----------|--------|-------|
| P1 | SEC | IDM permission seed for all `{CTX}_*` permissions |
| P2 | DOC | Document search integration via SRH |
| P3 | WRK | Workflow event → index triggers |
| P4 | NTF | Notification preference search |

### Products (Next)

| Priority | Product | Focus |
|----------|---------|-------|
| P1 | CMP | Enhanced search facets, semantic complaints |
| P2 | RTI | New product — GPS-001 compliant from day one |
| P3 | Trade License | New product integration |

---

## 4. SRH Post v1.0 (Not Started)

SRH v1.0 is **feature complete**. Future work requires new release cycle:

- Additional product consumers
- Persistent scheduler/observability history
- Async indexing (requires ADR)
- Additional search engines (requires ADR)

**DOC-001 not started** per platform planning.

---

## 5. Technical Evolution

| Area | v1.0 State | Future |
|------|------------|--------|
| Architecture | Modular monolith | Selective microservice extraction |
| Events | Contract records | Event bus integration |
| Auth | JWT RBAC | Fine-grained ABAC (optional) |
| Frontend | Angular 20 REST client | Shared search UI components |
| Deployment | Docker/K8s compatible | Official Helm chart |

---

## 6. Compliance Matrix

All new bounded contexts must:

1. Reference GPS-001 in module README
2. Pass code review checklist (doc 27)
3. Achieve JaCoCo coverage gates
4. Document sprint milestones `{CTX}-{nnn}`
5. Certify with release process (doc 26) before v1.0 tag

---

## 7. Document Roadmap

| Document | Version | Next review |
|----------|---------|-------------|
| GPS-001 | 1.0.0 | On Spring Boot major upgrade |
| SRH certification | 1.0.0 | SRH v1.1 planning |
| CMP README | Active | CMP search enhancements |

---

## 8. Version History

| Version | Date | Change |
|---------|------|--------|
| GPS-001 v1.0.0 | 2026-07-18 | Initial platform development standard |
