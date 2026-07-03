# GovOS Document Management (DOC)

Sprint 0 Day 6 — Document Management bounded context for the GovOS platform.

## Overview

The DOC module models document metadata, folder hierarchy, versioning, tagging, access logging, and storage provider configuration. Binary file storage is abstracted behind `StorageService`; MinIO integration is deferred.

This module provides the **domain layer only** — no REST controllers, upload APIs, or MinIO SDK in Sprint 0 Day 6.

## Module Location

| Artifact | Package | Responsibility |
|----------|---------|----------------|
| `govos-domain` | `com.govos.doc` | DOC entities, services, repositories, DTOs, mappers, storage |
| `govos-infrastructure` | `db/migration` | Flyway migration `V1_4_0__document_management.sql` |

## Package Structure

```
com.govos.doc
├── config          # Bounded-context configuration
├── controller      # Reserved for future REST layer (empty)
├── dto             # Request/response records
├── entity          # JPA entities and enums
├── event           # Domain events (records)
├── exception       # Domain exceptions
├── mapper          # MapStruct entity ↔ DTO mapping
├── repository      # Spring Data JPA repositories
├── service         # Service interfaces and implementations
├── storage         # StorageService abstraction and MinIO adapter stub
└── validator       # Business validation rules
```

## Entity Model

All entities extend `AuditableEntity` (`govos-common`).

| Entity | Table | Description |
|--------|-------|-------------|
| `StorageProvider` | `doc_storage_provider` | External storage backend (MinIO, S3, etc.) |
| `Folder` | `doc_folder` | Hierarchical folder structure |
| `Document` | `doc_document` | Document metadata and storage reference |
| `DocumentVersion` | `doc_document_version` | Version history per document |
| `DocumentTag` | `doc_document_tag` | Reusable tag definitions |
| `DocumentTagMapping` | `doc_document_tag_mapping` | Document-to-tag assignments |
| `DocumentAccessLog` | `doc_document_access_log` | Access audit trail |

### Cross-Domain References

- `Folder.owner` → `com.govos.idm.entity.User`
- `Document.owner` → `com.govos.idm.entity.User`
- `DocumentAccessLog.user` → `com.govos.idm.entity.User`

## Document Model

| Field | Description |
|-------|-------------|
| `code` | Business identifier |
| `originalFilename` | User-facing filename |
| `storedFilename` | Object key in storage provider |
| `mimeType` / `extension` | File type metadata |
| `size` / `checksum` | Integrity metadata |
| `storageProvider` | Target storage backend |
| `folder` | Optional folder placement |
| `owner` | Owning user |
| `visibility` | `PRIVATE`, `INTERNAL`, `PUBLIC` |
| `status` | `DRAFT`, `ACTIVE`, `ARCHIVED`, `DELETED` |

UUID remains the primary key via `BaseEntity`.

## Storage Abstraction

| Component | Status |
|-----------|--------|
| `StorageService` | Interface defined (`upload`, `download`, `delete`) |
| `MinioStorageService` | Stub — throws `UnsupportedOperationException` |
| MinIO SDK | Not included in Sprint 0 Day 6 |

## Database

**Migration:** `V1_4_0__document_management.sql` (schema version **1.4.0**)

Partial unique indexes support soft-delete for codes, tag names, version numbers, and tag mappings.

## Service API (Internal)

| Service | Key Operations |
|---------|----------------|
| `StorageProviderService` | CRUD, list by provider type |
| `FolderService` | CRUD, list by owner/parent |
| `DocumentService` | CRUD, list by folder/owner/status |
| `DocumentVersionService` | CRUD, list by document |
| `DocumentTagService` | CRUD, get by name |
| `DocumentTagMappingService` | Assign tag, remove mapping |
| `DocumentAccessLogService` | Create and query access logs |

### Business Rules

- Duplicate codes rejected (storage provider, folder, document)
- Duplicate tag names rejected
- Unique document version number per document
- Unique document-tag mapping per pair
- Folder parent assignment prevents cycles
- Access logs are append-only (create + read)
- Optimistic locking via `version` on update operations
- Soft-delete sets `deleted = true`, `active = false`

## Domain Events

| Event | Purpose |
|-------|---------|
| `DocumentCreatedEvent` | Document metadata created |
| `DocumentVersionCreatedEvent` | New version recorded |
| `DocumentAccessLoggedEvent` | Access audit entry |

Events are plain records; Spring event publishing is deferred.

## Out of Scope (Sprint 0 Day 6)

- REST controllers and HTTP APIs
- File upload/download endpoints
- MinIO SDK integration
- Security, JWT, authentication
- Complaint bounded context
- Sample/seed data

## Platform Foundation Status

| Module | Status |
|--------|--------|
| Infrastructure | ✅ |
| MDM | ✅ |
| Identity (IDM) | ✅ |
| Organization (ORG) | ✅ |
| Document Management (DOC) | ✅ |

## Next Steps

- MinIO SDK integration and upload/download in `MinioStorageService`
- DOC REST controllers in `govos-api`
- `govos-security` module
- Spring application event publishing for DOC domain events
