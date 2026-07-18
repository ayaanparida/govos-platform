# DOC-001 — Implementation Roadmap

**Document Management (DOC)** — milestone plan aligned with SRH sprint pattern and GPS-001.

---

## Roadmap Overview

| Sprint | Title | Deliverables |
|--------|-------|--------------|
| **DOC-001** | **Architecture Blueprint** | **This documentation (complete)** |
| DOC-002 | Domain Model | Entities, enums, value objects |
| DOC-003 | Flyway | Schema aligned to DOC-001 + GPS-001 |
| DOC-004 | Repository | Spring Data JPA repositories |
| DOC-005 | DTO + Mapper | Records, MapStruct mappers |
| DOC-006 | Validation | Jakarta + business validators |
| DOC-007 | Services | Domain service implementations |
| DOC-008 | Events | Domain event records + publishing |
| DOC-009 | Tests | Unit tests + JaCoCo gates | ✅ Complete |
| DOC-010 | Application Layer | `DocumentApplicationService` in govos-api | ✅ Complete |
| DOC-011 | REST API | `/api/v1/documents/**`, 8 controllers, OpenAPI | ✅ Complete |
| DOC-012 | Storage Providers | `StorageProviderPort`, factory, 5 providers | ✅ Complete |
| DOC-013 | Versioning | Immutable versions, rollback, active pointer | 🔜 Next |
| DOC-014 | OCR | Tesseract, Azure DI, Vision, Textract |
| DOC-015 | Preview & Thumbnail | PDF/image preview generation |
| DOC-016 | Digital Signature | Signature provider abstraction + impl |
| DOC-017 | Search Integration | SRH indexing via SearchApplicationService |
| DOC-018 | Scheduler & Retention | Retention jobs, purge, expiry |
| DOC-019 | Production Hardening | Virus scan, metrics, resilience, security |
| DOC-020 | Release Certification | DOC v1.0.0 docs + validation |

---

## DOC-001 ✅

- Architecture documentation
- Aggregate design
- Storage, security, versioning, integration strategies
- GPS-001 compliance
- No production code

## DOC-002 ✅ (Current)

- JPA entities for all 9 aggregates
- 10 domain enums in `com.govos.doc.enums`
- 6 embeddable value objects in `com.govos.doc.valueobject`
- Extends `AuditableEntity`, UUID PKs, `@Version`, soft delete
- JPA indexes defined for future Flyway generation
- **No Flyway, repositories, services, DTOs, or tests**

---

## DOC-003 ✅

- Flyway migration `V2_2_0__document_management.sql`
- 9 DOC tables with GPS-001 audit columns
- Within-context FK constraints only
- GIN indexes for JSONB and OCR full-text placeholder
- Drops legacy V1_4_0 Sprint 0 schema
- **No repositories, services, DTOs, or tests**

---

## DOC-004 ✅ (Current)

- 9 Spring Data JPA repositories in `com.govos.doc.repository`
- `findByIdAndDeletedFalse` soft-delete pattern throughout
- `Pageable` on Document and DocumentVersion listings
- Derived queries only — no JPQL, no Specifications
- **No DTOs, mappers, validators, services, or tests**

---

## DOC-005 ✅ (Current)

- 28 Java record DTOs across 9 sub-packages
- 9 MapStruct mapper interfaces with Spring component model
- Embedded value object flattening (checksum, storage location, version number, path, share token)
- `@BeanMapping(IGNORE)` on update methods for partial updates
- **No validators, services, REST APIs, or tests**

---

## DOC-006 ✅

- 8 validators in `com.govos.doc.validator`
- `ValidationResult` / `ValidationError` / `DocumentValidationException` model
- `ValidationUtils` with SHA-256, MIME, document number, and size helpers
- Jakarta Bean Validation integration via injected `Validator`
- No repository access, services, REST APIs, or tests

---

## DOC-007 ✅

- 8 domain service interfaces in `com.govos.doc.service`
- 8 `@Service` implementations in `com.govos.doc.service.impl`
- 8 not-found exception types in `com.govos.doc.exception`
- Repository + validator integration; entity returns (no response DTO mapping)
- `@Transactional` write / `@Transactional(readOnly = true)` read pattern
- **No application layer, REST, storage adapters, OCR, preview, scheduler, events, search, or tests**

---

## DOC-008 ✅ (Current)

- `DocumentDomainEvent` contract + 34 immutable event records in `com.govos.doc.event`
- `DocumentEventTypes` canonical type constants
- `DocumentEvents` factory for building events from persisted state
- `DocumentEventPublisher` abstraction + `NoOpDocumentEventPublisher` default
- All 8 domain services publish events after successful writes
- **No Spring listeners, messaging, application layer, REST, or tests**

---

## DOC-009 — Tests ✅

- Entity, value object, validator, service, mapper, and event unit tests
- `DocumentTestFixtures` shared builders under `com.govos.doc.support`
- JaCoCo: ≥90% service/validator/mapper/event packages; ≥85% per-class gate
- **240** test methods; `mvn -pl govos-domain test verify` → BUILD SUCCESS
- **No** application layer, REST, integration tests, or infrastructure adapters

---

## DOC-010 — Application Layer ✅

- `DocumentApplicationService` / `DocumentApplicationServiceImpl` in `com.govos.doc.application`
- 47 orchestration methods — validate → domain service → map to response DTO
- 7 integration placeholder interfaces in `com.govos.doc.application.integration`
- Constructor injection; `@Transactional(readOnly = true)` class-level; write override
- `DocumentApplicationServiceTest` unit tests; `mvn -pl govos-api -am test` → BUILD SUCCESS
- **No** REST, storage, OCR, preview, scheduler, or integration implementations

---

## DOC-011 — REST API ✅

- 8 REST controllers in `com.govos.doc.api` (`govos-api`)
- 45 endpoints under `/api/v1/documents/**` sub-resources
- Delegates to `DocumentApplicationService` only — no business logic in controllers
- OpenAPI: `@Tag`, `@Operation`, `@ApiResponses`, `@Parameter`, `@SecurityRequirement`
- Security: `@PreAuthorize` with `DOC_READ`, `DOC_WRITE`, `DOC_DELETE`, `DOC_SHARE`, `DOC_ADMIN`
- `DocumentRestExceptionHandler` — DOC exceptions → 404/409/422
- `DocumentApiLogging` — structured logs (no sensitive content)
- Pagination via `PaginationRequest` + `SortParser` → `Pageable`
- MockMvc tests: `DocumentControllerTest`, `DocumentRestExceptionHandlerTest`, `DocumentApiSecurityTest`
- `mvn -pl govos-api -am test` → BUILD SUCCESS
- **No** multipart upload/download, storage providers, OCR, preview, scheduler, or integration implementations

---

## DOC-012 — Storage Providers ✅

- `StorageProviderPort` in `com.govos.doc.storage.port` — store, load, delete, copy, move, signed URLs, metadata, multipart, health
- `DocumentStorageService` / `DocumentStorageServiceImpl` — configuration-driven provider selection
- `StorageProviderFactory` — resolves `local|minio|s3|azure|gcs` from `govos.document.storage.provider`
- Provider implementations: `LocalStorageProvider`, `MinioStorageProvider`, `S3StorageProvider`, `AzureBlobStorageProvider`, `GoogleCloudStorageProvider`
- SDK client adapters with unconfigured fallbacks when credentials absent
- `DocumentStorageProperties` + `DocStorageConfiguration` — Spring beans for MinIO, S3, Azure, GCS clients
- Streaming via `StorageStreamSupport`; multipart abstraction on port
- `StorageMetricsRecorder` — Micrometer counters for uploads/downloads/deletes/failures/bytes
- `StorageHealthIndicator` — UP/DOWN/DEGRADED/UNKNOWN for future monitoring
- Unit tests with mocked SDKs; `mvn -pl govos-api -am test` → BUILD SUCCESS
- **No** REST upload/download, OCR, preview, scheduler, or integration implementations

---

## DOC-013 — Versioning

- Immutable version commits
- Active version pointer
- Rollback API
- Pinned version for legal evidence

---

## DOC-014 — OCR

- `OcrProviderPort` + adapters
- Async OCR jobs
- Store text in DocumentMetadata
- Trigger SRH re-index on completion

---

## DOC-015 — Preview & Thumbnail

- PDF preview for office documents
- Image thumbnails
- Async generation pipeline
- Storage of preview blobs

---

## DOC-016 — Digital Signature

- `SignatureProviderPort` (future providers)
- Sign/verify workflow integration with WRK
- Metadata only in DOC-001 — implementation here

---

## DOC-017 — Search Integration

- `DocumentSearchIntegrationImpl`
- Register `DOC_DOCUMENT` index in SRH
- Index metadata + OCR text on upload/version/OCR complete
- Remove from index on delete

---

## DOC-018 — Scheduler & Retention

- Spring `@Scheduled` jobs (DOC-owned)
- Retention expiry, purge, OCR backlog
- Admin triggers + history (SRH-019 pattern)
- `govos.doc.scheduler.*` configuration

---

## DOC-019 — Production Hardening

- ClamAV integration
- Micrometer metrics
- Resilience (retry, timeout)
- Operational health endpoints
- Watermarking on download

---

## DOC-020 — Release Certification

- DOC v1.0.0 certification docs (mirror SRH Release-1.0)
- Architecture validation
- Security review
- Compatibility matrix
- Code freeze

---

## Dependencies on Other Modules

| Module | Required by | Status |
|--------|-------------|--------|
| GPS-001 | All DOC sprints | ✅ v1.0.0 |
| IDM | DOC-002+ (UUID refs) | ✅ Active |
| ORG | DOC-007+ (org validation) | ✅ Active |
| SRH | DOC-017 | ✅ v1.0 Certified |
| AUD | DOC-008+ | ✅ Active |
| NTF | DOC-011+ | ✅ Active |
| WRK | DOC-016+ | ✅ Active |
| SEC | DOC-011 (permissions seed) | 🔜 Planned |

---

## Platform Foundation Status (Post DOC-001)

| Module | Status |
|--------|--------|
| **Document (DOC)** | ✅ DOC-006 Validation Layer |
| Search (SRH) | ✅ v1.0 Certified |
| CMP | ✅ Active (future DOC consumer) |
| GPS-001 | ✅ v1.0.0 |

---

## Version History

| Version | Date | Milestone |
|---------|------|-----------|
| DOC-006 v6.0.0 | 2026-07-18 | Validation layer implemented |
| DOC-005 v5.0.0 | 2026-07-18 | DTO and MapStruct mapper layer |
| DOC-004 v4.0.0 | 2026-07-18 | Repository layer implemented |
| DOC-003 v3.0.0 | 2026-07-18 | Flyway schema V2_2_0 implemented |
| DOC-002 v2.0.0 | 2026-07-18 | Domain model implemented |
| DOC-001 v1.0.0 | 2026-07-18 | Architecture blueprint approved |
