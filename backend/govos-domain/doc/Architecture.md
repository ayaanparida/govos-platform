# DOC-001 — Architecture

**Bounded context:** Document Management (DOC)  
**Compliance:** GPS-001, DDD, Hexagonal Architecture, Modular Monolith

---

## 1. System Context

```mermaid
flowchart TB
    subgraph Products
        CMP[CMP Complaints]
        RTI[RTI Future]
        WRK_C[WRK Tasks]
    end

    subgraph Platform API
        DAS[DocumentApplicationService]
        DC[DocumentController]
    end

    subgraph DOC Domain
        DS[DocumentService]
        SS[StorageService]
        OCR[OcrProvider]
        PRE[PreviewService]
    end

    subgraph Platform Services
        SRH[SRH Search]
        AUD[AUD Audit]
        NTF[NTF Notifications]
        WRK[WRK Workflow]
        IDM[IDM Identity]
        ORG[ORG Organization]
    end

    subgraph External
        MINIO[MinIO / S3 / Azure / GCS]
        CLAM[ClamAV]
        TESS[Tesseract / Azure DI / Vision / Textract]
    end

    CMP --> DAS
    RTI --> DAS
    DAS --> DS
    DS --> SS
    SS --> MINIO
    DS --> OCR --> TESS
    DS --> PRE
    DS --> SRH
    DS --> AUD
    DS --> NTF
    DS --> WRK
    DS --> IDM
    DS --> ORG
    DC --> DAS
```

---

## 2. Layering (Hexagonal / Clean)

| Layer | Location | Responsibility |
|-------|----------|----------------|
| **Inbound adapters** | `govos-api` controllers, schedulers | HTTP, cron triggers |
| **Application** | `DocumentApplicationService` | Orchestration, ACL, transactions |
| **Domain** | `com.govos.doc.service` | Business rules, lifecycle |
| **Outbound ports** | `StorageProvider`, `OcrProvider`, `VirusScanProvider` | Abstractions |
| **Outbound adapters** | `storage.minio`, `ocr.tesseract` | Vendor implementations |
| **Persistence** | JPA repositories | PostgreSQL metadata |

**Dependency rule:** Domain never depends on API or vendor SDKs.

---

## 3. Aggregate Relationship Diagram

```mermaid
erDiagram
    Document ||--o{ DocumentVersion : has
    Document }o--|| Folder : placed_in
    Document }o--o| DocumentCategory : classified_as
    Document ||--o| DocumentMetadata : extends
    Document }o--|| StorageProvider : stored_on
    Document ||--o{ DocumentShare : shared_via
    Document ||--o{ DocumentAccessLog : audited_by
    DocumentRetentionPolicy ||--o{ Document : governs
    Folder ||--o{ Folder : parent_of
    DocumentVersion ||--o| DocumentMetadata : version_metadata
```

---

## 4. Storage Abstraction

```mermaid
flowchart LR
    DS[DocumentService]
    SP[StorageProvider Port]
    MINIO[MinioStorageAdapter]
    S3[S3StorageAdapter]
    AZ[AzureBlobAdapter]
    GCS[GcsStorageAdapter]
    LOCAL[LocalFileAdapter]

    DS --> SP
    SP --> MINIO
    SP --> S3
    SP --> AZ
    SP --> GCS
    SP --> LOCAL
```

See [StorageArchitecture.md](./StorageArchitecture.md).

---

## 5. Document Upload Flow

```mermaid
sequenceDiagram
    participant P as Product
    participant API as DocumentApplicationService
    participant DOC as DocumentService
    participant VAL as VirusScanProvider
    participant ST as StorageProvider
    participant SRH as SearchApplicationService
    participant AUD as AuditIntegration

    P->>API: upload(request, stream)
    API->>DOC: initiateUpload(metadata)
    DOC->>DOC: validate quota, permissions, mime
    DOC->>ST: putObject(stream, key)
    ST-->>DOC: storageRef, etag
    DOC->>VAL: scan(storageRef) [async optional]
    DOC->>DOC: create Document + DocumentVersion
    DOC->>SRH: index metadata + OCR text [async]
    DOC->>AUD: record document uploaded
    DOC-->>API: DocumentDto
    API-->>P: 201 Created
```

**Rules:**
- Streaming upload — no full buffering of large files in heap
- Checksum computed during stream (SHA-256)
- Chunked/multipart upload for files above configurable threshold

---

## 6. Document Download Flow

```mermaid
sequenceDiagram
    participant U as User/Client
    participant API as DocumentApplicationService
    participant DOC as DocumentService
    participant AUTH as AuthorizationService
    participant ST as StorageProvider
    participant LOG as DocumentAccessLog

    U->>API: download(documentId)
    API->>DOC: authorizeDownload(user, documentId)
    DOC->>AUTH: check DOC_READ + org scope + share token
    alt Authorized
        DOC->>LOG: record DOWNLOAD
        DOC->>ST: getObject(key) or signedUrl()
        ST-->>API: stream / redirect URL
        API-->>U: 200 / 302
    else Denied
        DOC-->>API: 403 Forbidden
    end
```

---

## 7. Preview Generation Flow

```mermaid
sequenceDiagram
    participant API as DocumentApplicationService
    participant DOC as DocumentService
    participant PRE as PreviewService
    participant ST as StorageProvider

    API->>DOC: requestPreview(documentVersionId)
    DOC->>PRE: generate(sourceMime, storageRef)
    PRE->>ST: read source blob
    PRE->>PRE: render PDF/image preview
    PRE->>ST: write preview blob
    DOC->>DOC: update version previewRef
    DOC-->>API: PreviewDto
```

Supported targets: PDF preview for office docs, image resize for images. Async job for large files.

---

## 8. OCR Pipeline

```mermaid
flowchart TB
    UP[Upload Complete] --> Q[OCR Job Queue]
    Q --> SEL{Provider Config}
    SEL --> TESS[Tesseract]
    SEL --> AZDI[Azure Document Intelligence]
    SEL --> GV[Google Vision]
    SEL --> TX[AWS Textract]
    TESS --> TXT[Extracted Text]
    AZDI --> TXT
    GV --> TXT
    TX --> TXT
    TXT --> META[DocumentMetadata.ocrText]
    META --> SRH[SearchApplicationService.index]
```

OCR never stores raw images in search index — text extraction only.

---

## 9. Version Lifecycle

```mermaid
stateDiagram-v2
    [*] --> Draft: create metadata
    Draft --> Uploaded: binary stored
    Uploaded --> Approved: workflow approve
    Uploaded --> Archived: manual archive
    Approved --> Archived: retention / manual
    Archived --> Deleted: soft delete
    Deleted --> Restored: restore
    Uploaded --> Expired: retention policy
    Expired --> Deleted: purge job
```

See [VersioningStrategy.md](./VersioningStrategy.md).

---

## 10. Cross-Context Communication

| Target | Method | Purpose |
|--------|--------|---------|
| **SRH** | `SearchApplicationService` | Index metadata + OCR text |
| **AUD** | AUD integration API | Immutable audit events |
| **NTF** | Notification API | Share alerts, expiry warnings |
| **WRK** | Workflow API | Approval tasks on upload |
| **IDM** | UUID reference | Owner, creator identity |
| **ORG** | UUID reference | Organization isolation |

**Forbidden:** DOC → OpenSearch direct; products → MinIO/S3 direct.

See [IntegrationArchitecture.md](./IntegrationArchitecture.md).

---

## 11. Modular Monolith Rules

1. Products integrate via `DocumentApplicationService` only
2. Cross-context references are UUID columns (GPS-001)
3. DOC owns all scheduling for retention, OCR backlog, virus rescan
4. Binary blobs never in PostgreSQL — metadata only
5. Provider SDKs isolated in adapter packages

---

## 12. Future Microservice Extraction

DOC is extractable as standalone service when:

- `com.govos.doc` package has no product imports
- Storage and OCR ports remain stable
- REST contract `/api/v1/documents` unchanged
- PostgreSQL `doc_*` schema migrates to dedicated database
