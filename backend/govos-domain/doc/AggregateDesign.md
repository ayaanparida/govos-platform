# DOC-001 — Aggregate Design

**Pattern:** DDD aggregate roots with UUID identity, soft delete, optimistic locking (GPS-001 §08)

---

## 1. Aggregate Overview

| Aggregate | Root entity | Purpose |
|-----------|-------------|---------|
| **Document** | `Document` | Central document identity and lifecycle |
| **DocumentVersion** | `DocumentVersion` | Immutable file version blobs |
| **Folder** | `Folder` | Hierarchical organization |
| **DocumentCategory** | `DocumentCategory` | Taxonomy / classification tree |
| **DocumentMetadata** | `DocumentMetadata` | Extended attributes, OCR text, custom fields |
| **DocumentRetentionPolicy** | `DocumentRetentionPolicy` | Retention rules |
| **DocumentShare** | `DocumentShare` | Sharing grants and links |
| **DocumentAccessLog** | `DocumentAccessLog` | Access audit metadata |
| **StorageProvider** | `StorageProvider` | Storage backend configuration |

---

## 2. Document (Primary Aggregate Root)

**Consistency boundary:** Document identity, active version pointer, lifecycle status, organization scope.

### Responsibilities

- Own document identity (`id`, `code`)
- Track lifecycle status: `DRAFT`, `UPLOADED`, `APPROVED`, `ARCHIVED`, `DELETED`, `RESTORED`, `EXPIRED`
- Point to **active** `DocumentVersion`
- Enforce organization isolation (`organizationId`)
- Reference folder, category, retention policy (by ID)
- Never store binary content — only metadata

### Key attributes (conceptual)

| Attribute | Role |
|-----------|------|
| `id` | UUID primary key |
| `code` | Business key within organization |
| `organizationId` | Tenant isolation |
| `ownerId` | UUID — document owner (IDM user) |
| `folderId` | Optional folder placement |
| `categoryId` | Optional classification |
| `retentionPolicyId` | Applied retention rule |
| `activeVersionId` | Current version pointer |
| `status` | Lifecycle enum |
| `classification` | Security classification (PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED) |
| `mimeType` | Active version MIME (denormalized) |
| `title` | Display title |
| `description` | Optional description |
| `moduleCode` | Originating module (`CMP`, `RTI`) |
| `entityType` | Business entity type |
| `referenceId` | Business entity UUID |
| `visibility` | `PRIVATE`, `INTERNAL`, `SHARED`, `PUBLIC` |

### Invariants

- One active version at a time
- Status transitions follow defined state machine
- Soft delete sets `deleted=true`; binary retained until retention purge
- `organizationId` immutable after create

---

## 3. DocumentVersion

**Consistency boundary:** Immutable version record per upload.

### Responsibilities

- Store version number (monotonic integer: major*1000 + minor or sequential)
- Immutable blob reference (`storageKey`, `checksum`, `sizeBytes`)
- Preview and thumbnail references
- Virus scan result metadata
- Digital signature reference (future)
- Version-specific MIME and filename

### Key attributes

| Attribute | Role |
|-----------|------|
| `documentId` | Parent document UUID |
| `versionNumber` | Sequential version (1, 2, 3…) |
| `versionLabel` | Optional `1.0`, `2.1` display |
| `storageProviderId` | Provider used for this version |
| `storageKey` | Object key in provider |
| `checksum` | SHA-256 hex |
| `sizeBytes` | File size |
| `originalFilename` | User filename at upload |
| `uploadedById` | UUID user |
| `uploadedAt` | Timestamp |
| `virusScanStatus` | `PENDING`, `CLEAN`, `INFECTED`, `SKIPPED` |
| `previewStorageKey` | Generated preview object key |
| `thumbnailStorageKey` | Generated thumbnail key |
| `isImmutable` | Always true after commit |

### Invariants

- Versions are **append-only** — never mutate blob reference after commit
- Unique `(documentId, versionNumber)` per organization
- Rollback = switch active version pointer, not delete history

---

## 4. Folder

**Consistency boundary:** Folder tree within organization.

### Responsibilities

- Hierarchical folder structure
- Prevent cyclic parent assignment
- Organization-scoped folder codes

### Key attributes

| Attribute | Role |
|-----------|------|
| `organizationId` | Tenant |
| `parentFolderId` | Nullable root |
| `name` | Display name |
| `code` | Unique per org |
| `ownerId` | UUID owner |

### Invariants

- No cycles in parent chain
- Soft delete cascades logically (documents move or block delete if non-empty — policy TBD DOC-007)

---

## 5. DocumentCategory

**Consistency boundary:** Classification taxonomy.

### Responsibilities

- Hierarchical or flat category tree
- MIME type hints, retention defaults
- Used for browse/filter — not access control alone

### Key attributes

| Attribute | Role |
|-----------|------|
| `code` | Category code |
| `name` | Display name |
| `parentCategoryId` | Optional hierarchy |
| `defaultRetentionPolicyId` | Optional default |
| `allowedMimeTypes` | Optional restriction list |

---

## 6. DocumentMetadata

**Consistency boundary:** Extended metadata and extracted content references.

### Responsibilities

- Custom JSON attributes (JSONB)
- OCR extracted text (for SRH indexing — not logged)
- EXIF / document properties
- Watermark applied flag
- Language detection result

### Key attributes

| Attribute | Role |
|-----------|------|
| `documentId` | Parent |
| `documentVersionId` | Optional version scope |
| `ocrText` | Extracted text (searchable via SRH) |
| `ocrLanguage` | Detected language |
| `ocrConfidence` | Average confidence |
| `customAttributes` | JSONB key-value |
| `pageCount` | For PDFs |

**Note:** May be embedded in `DocumentVersion` for v1 simplicity — aggregate split if metadata grows independently (DOC-002 decision).

---

## 7. DocumentRetentionPolicy

**Consistency boundary:** Retention rule definitions.

### Responsibilities

- Define retention duration (days/months/years)
- Action on expiry: `ARCHIVE`, `DELETE`, `NOTIFY`
- Legal hold flag
- Organization or platform-wide scope

### Key attributes

| Attribute | Role |
|-----------|------|
| `code` | Policy code |
| `name` | Display name |
| `retentionDays` | Duration |
| `actionOnExpiry` | Enum |
| `legalHold` | Boolean — blocks purge |
| `organizationId` | Nullable for platform default |

---

## 8. DocumentShare

**Consistency boundary:** Sharing grants.

### Responsibilities

- Share document with user, role, or external token
- Expiring share links
- Permission level: `READ`, `DOWNLOAD`, `COMMENT` (future)

### Key attributes

| Attribute | Role |
|-----------|------|
| `documentId` | Shared document |
| `sharedWithUserId` | Optional UUID |
| `sharedWithEmail` | Optional external |
| `shareToken` | Hashed token for URL |
| `expiresAt` | Optional expiry |
| `permission` | `READ`, `DOWNLOAD` |
| `createdById` | Sharer |

---

## 9. DocumentAccessLog

**Consistency boundary:** Append-only access records.

### Responsibilities

- Record VIEW, DOWNLOAD, PREVIEW, SHARE, DELETE attempts
- Support compliance reporting
- Complement AUD module (DOC stores operational log; AUD stores platform audit)

### Key attributes

| Attribute | Role |
|-----------|------|
| `documentId` | Target document |
| `userId` | Actor UUID |
| `action` | Enum |
| `ipAddress` | Optional |
| `userAgent` | Optional |
| `success` | Boolean |
| `timestamp` | Instant |

### Invariants

- **Append-only** — no update or delete

---

## 10. StorageProvider

**Consistency boundary:** Storage backend registry.

### Responsibilities

- Register MinIO, S3, Azure, GCS, Local providers
- Store connection metadata (endpoint, bucket, region)
- Mark default provider per environment
- **Never store secrets in DB** — reference secret keys only

### Key attributes

| Attribute | Role |
|-----------|------|
| `code` | Provider instance code |
| `providerType` | `MINIO`, `S3`, `AZURE_BLOB`, `GCS`, `LOCAL` |
| `bucketName` | Default bucket |
| `endpoint` | Service endpoint |
| `region` | Cloud region |
| `isDefault` | Default flag |
| `encryptionEnabled` | At-rest encryption flag |

---

## 11. Aggregate Relationship Rules

| Rule | Detail |
|------|--------|
| Document → Version | Composition; versions belong to one document |
| Document → Folder | Reference by ID |
| Document → Category | Reference by ID |
| Document → RetentionPolicy | Reference by ID |
| Document → Share | Separate aggregate; linked by documentId |
| AccessLog | Separate aggregate; append-only |
| Cross-context | `ownerId`, `organizationId` as UUID — no JPA `@ManyToOne` to IDM/ORG entities |

---

## 12. Tags vs Categories

**DocumentCategory** = governed taxonomy for retention and browse.  
**Tags** (future optional value object or separate aggregate) = lightweight labels. Early V1_4_0 used `DocumentTag` — DOC-002 will reconcile with this blueprint.
