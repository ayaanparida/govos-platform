# GovOS Complaint Management (CMP)

**Sprint 1 — Architecture Blueprint (CMP-001)**

Citizen Grievance Management System (CGMS) bounded context for the GovOS Enterprise Government Platform.

> **Status:** Architecture only. No Java implementation, Flyway, REST, Angular, or tests in this sprint.  
> **Implementation begins:** CMP-002 onwards.

---

## 1. Purpose

CMP is the **first business module** built on the GovOS platform foundation. It models the full lifecycle of a citizen grievance — from draft submission through assignment, resolution, citizen verification, and closure — while **consuming** platform services for identity, organization, documents, notifications, workflow, audit, master data, and security.

CMP contains **only complaint business logic**. It does not duplicate platform capabilities.

| In scope (CMP) | Out of scope (CMP) |
|----------------|-------------------|
| Complaint domain model and lifecycle | REST controllers (`govos-api` — later sprint) |
| Assignment, SLA, escalation business rules | Angular portals |
| Status machine and invariants | Elasticsearch / OpenSearch |
| Domain events (records) | AI classification, OCR, duplicate ML |
| Integration contracts with platform modules | Workflow execution engine (WRK owns engine) |
| Attachment **references** to DOC | Background jobs, schedulers |

---

## 2. Architecture

### 2.1 Modular Monolith Placement

```
┌─────────────────────────────────────────────────────────────────┐
│                        govos-api (future)                       │
│                   REST controllers per domain                   │
└────────────────────────────┬────────────────────────────────────┘
                             │ calls services only
┌────────────────────────────▼────────────────────────────────────┐
│                        govos-domain                             │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐  │
│  │   MDM   │ │   IDM   │ │   ORG   │ │   DOC   │ │   NTF   │  │
│  └────┬────┘ └────┬────┘ └────┬────┘ └────┬────┘ └────┬────┘  │
│       │           │           │           │           │        │
│  ┌────┴───────────┴───────────┴───────────┴───────────┴────┐  │
│  │                         CMP                              │  │
│  │              (Complaint business logic)                  │  │
│  └────┬───────────┬─────────────────────────────────────────┘  │
│       │           │                                             │
│  ┌────▼────┐ ┌────▼────┐ ┌─────────┐                           │
│  │   WRK   │ │   AUD   │ │ Security│                           │
│  └─────────┘ └─────────┘ └─────────┘                           │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 Module Location

| Artifact | Package / Path | Responsibility |
|----------|------------------|----------------|
| `govos-domain` | `com.govos.cmp` | CMP entities, services, repositories, DTOs, mappers, validators, events |
| `govos-infrastructure` | `db/migration` | Flyway **`V2_0_0__complaint.sql`** (CMP-003+) |
| `govos-common` | `BaseEntity`, `AuditableEntity` | Shared entity bases (unchanged) |

### 2.3 Package Structure

Follows ADR-002 and existing bounded-context conventions exactly.

```
com.govos.cmp
├── config/         # @Configuration, CMP properties (SLA defaults, attachment limits)
├── controller/     # package-info.java ONLY — REST lives in govos-api (future)
├── dto/            # Records: ComplaintDto, CreateComplaintRequest, etc. (CMP-004+)
├── entity/         # JPA entities and enums
├── event/          # Domain event records
├── exception/      # Domain exceptions
├── mapper/         # MapStruct entity ↔ DTO (CMP-004+)
├── mdm/            # CmpMasterDataTypes constants
├── projection/     # ComplaintTimeline read-model assembly (no persisted entity)
├── repository/     # Spring Data JPA repositories
├── service/        # Service interfaces and implementations
├── validator/      # Business validation rules
├── listener/       # Reserved — event listeners deferred
├── scheduler/      # Reserved — empty (no schedulers in CMP)
└── util/           # Reserved
```

### 2.4 Design Principles

1. **Single aggregate root** — `Complaint` owns all complaint-specific entities.
2. **No cross-domain repository access** — CMP services call IDM, ORG, DOC, WRK, NTF, AUD, MDM **services** only.
3. **No workflow logic in CMP** — status transitions are CMP business rules; step orchestration is WRK configuration.
4. **No duplicate audit trail** — field-level changes go to AUD; CMP keeps `ComplaintStatusHistory` for the status state machine only.
5. **MDM for configurable values** — categories, resolution codes, closure reasons are not hard-coded enums in the database.
6. **Polymorphic workflow binding** — `WorkflowInstance.referenceType = "COMPLAINT"`, `referenceId = complaint.id`.

---

## 3. Aggregate Roots

### 3.1 Design Decision

After DDD analysis, CMP defines **one aggregate root**:

| Aggregate Root | Table | Rationale |
|----------------|-------|-----------|
| **`Complaint`** | `cmp_complaint` | Consistency boundary for status, assignment, resolution, SLA, and all child records |

All other complaint-specific tables are **entities within the Complaint aggregate**. They are not independent aggregate roots because their invariants are always evaluated in the context of a parent complaint (e.g. cannot assign a closed complaint; cannot resolve without acceptance).

### 3.2 Rejected as Separate Aggregate Roots

| Candidate | Decision | Reason |
|-----------|----------|--------|
| `ComplaintAssignment` | Child entity (append-only history) | Assignment cannot exist without a complaint; current assignment = latest active record |
| `ComplaintResolution` | Child entity | Resolution is part of complaint closure invariant |
| `ComplaintSLA` | Child entity | SLA milestones are derived from complaint priority and lifecycle events |
| `ComplaintComment` | Child entity | Comments are always scoped to one complaint; extract later only if volume requires |
| `ComplaintAttachment` | Child entity | Metadata link to DOC; lifecycle tied to complaint |
| `ComplaintEscalation` | Child entity (append-only) | Escalation is a complaint lifecycle event |
| `ComplaintDuplicate` | Link entity | Relationship between two complaints; managed via Complaint service |
| `ComplaintMerge` | Child entity (append-only) | Merge is an administrative action on a complaint |
| `ComplaintRating` | Child entity | Post-closure feedback; one rating per complaint |
| `ComplaintWatcher` | Child entity | Subscription to complaint updates |
| `ComplaintTag` | Link entity | Tag assignment on a complaint |
| `ComplaintLocation` | Child entity (1:1) | Geographic context of the grievance |
| `ComplaintStatusHistory` | Child entity (append-only) | Status machine audit trail (CMP-specific; not a duplicate of AUD) |

### 3.3 Not Entities — Platform or Read-Model Concerns

| Concept | Treatment |
|---------|-----------|
| `ComplaintTimeline` | **Read model** assembled by `ComplaintTimelineProjectionService` from status history, comments, assignments, escalations, and WRK history |
| `ComplaintHistory` (field-level) | **Delegated to AUD** — `AuditEvent` + `AuditChange` with `entityType = "COMPLAINT"` |
| `ComplaintSource` | **MDM reference** (`COMPLAINT_SOURCE`) + `ComplaintSource` enum for validation |
| `ComplaintChannel` | **MDM reference** (`COMPLAINT_CHANNEL`) |
| `ComplaintPriority` | **CMP enum** validated against MDM (`COMPLAINT_PRIORITY`) for SLA metadata |
| `ComplaintCategoryReference` | **Fields on Complaint** — `categoryType` + `categoryKey` MDM lookup; no separate table |

---

## 4. Entity Catalog

All entities extend `AuditableEntity` unless noted. Schema: `govos`. Table prefix: `cmp_`.

### 4.1 Complaint (Aggregate Root)

| Attribute | Detail |
|-----------|--------|
| **Purpose** | Core grievance record; holds current status, priority, source, category, citizen reference, and denormalized assignment pointers |
| **Aggregate owner** | Self (root) |
| **Table** | `cmp_complaint` |

| Field | Type | Notes |
|-------|------|-------|
| `code` | String | Business number, e.g. `CMP-2026-000042` (unique among non-deleted) |
| `title` | String | Short summary |
| `description` | Text | Full grievance text |
| `status` | Enum `ComplaintStatus` | Current lifecycle state |
| `priority` | Enum `ComplaintPriority` | LOW, MEDIUM, HIGH, CRITICAL, EMERGENCY |
| `source` | Enum `ComplaintSource` | Submission origin |
| `channel` | String | MDM key under `COMPLAINT_CHANNEL` |
| `categoryKey` | String | MDM key under `COMPLAINT_CATEGORY` |
| `subCategoryKey` | String | MDM key under `COMPLAINT_SUB_CATEGORY` |
| `complaintTypeKey` | String | MDM key under `COMPLAINT_TYPE` |
| `citizenUserId` | UUID FK | → `idm_user` (complainant) |
| `submittedByUserId` | UUID FK | → `idm_user` (may differ from citizen for officer-assisted) |
| `organizationId` | UUID FK | → `org_organization` (jurisdiction) |
| `departmentId` | UUID FK | → `org_department` (current owning department) |
| `officeId` | UUID FK | → `org_office` (current owning office) |
| `assignedOfficerId` | UUID FK | → `idm_user` (denormalized current assignee) |
| `submittedAt` | Instant | When moved from DRAFT to SUBMITTED |
| `closedAt` | Instant | When status became CLOSED |
| `rejectionReasonKey` | String | MDM key under `COMPLAINT_REJECTION_REASON` |
| `closureReasonKey` | String | MDM key under `COMPLAINT_CLOSURE_REASON` |
| `isDuplicate` | Boolean | Flag when marked duplicate |
| `primaryComplaintId` | UUID FK | → `cmp_complaint` (self-ref when duplicate) |
| `mergedIntoComplaintId` | UUID FK | → `cmp_complaint` (when merged away) |

| Concern | Behavior |
|---------|----------|
| **Cardinality** | 1 complaint : N child entities |
| **Soft delete** | Yes — `deleted = true`, `active = false`; closed complaints may be soft-deleted by admin only |
| **Optimistic locking** | Yes — `@Version` via `AuditableEntity` |
| **Auditing** | Yes — `createdBy`, `createdDate`, `updatedBy`, `updatedDate` |
| **Referenced BC** | IDM, ORG, MDM (keys) |

---

### 4.2 ComplaintLocation

| Attribute | Detail |
|-----------|--------|
| **Purpose** | Geographic context — address, ward, village, GPS coordinates for GIS integration (future) |
| **Aggregate owner** | Complaint |
| **Table** | `cmp_complaint_location` |
| **Cardinality** | 1:1 with Complaint (optional) |

| Field | Type | Notes |
|-------|------|-------|
| `complaintId` | UUID FK | → `cmp_complaint` (unique among non-deleted) |
| `addressLine1` | String | |
| `addressLine2` | String | |
| `landmark` | String | |
| `wardKey` | String | MDM key (future GIS) |
| `villageKey` | String | MDM key |
| `districtKey` | String | MDM key |
| `stateKey` | String | MDM key |
| `postalCode` | String | |
| `latitude` | Decimal | GPS capture |
| `longitude` | Decimal | GPS capture |

| Concern | Behavior |
|---------|----------|
| **Soft delete** | Yes — cascaded logically when complaint soft-deleted |
| **Optimistic locking** | Yes |
| **Auditing** | Yes |
| **Referenced BC** | MDM (ward/village keys) |

---

### 4.3 ComplaintStatusHistory

| Attribute | Detail |
|-----------|--------|
| **Purpose** | Append-only log of status transitions for the complaint state machine |
| **Aggregate owner** | Complaint |
| **Table** | `cmp_complaint_status_history` |
| **Cardinality** | N:1 Complaint |

| Field | Type | Notes |
|-------|------|-------|
| `complaintId` | UUID FK | → `cmp_complaint` |
| `fromStatus` | Enum | Previous status (null on creation) |
| `toStatus` | Enum | New status |
| `changedByUserId` | UUID FK | → `idm_user` |
| `reason` | Text | Optional transition reason |
| `reasonKey` | String | MDM key when applicable |
| `occurredAt` | Instant | Transition timestamp |

| Concern | Behavior |
|---------|----------|
| **Soft delete** | **No** — append-only; never updated or deleted |
| **Optimistic locking** | No — insert-only |
| **Auditing** | `createdBy` / `createdDate` only |
| **Referenced BC** | IDM, MDM |

---

### 4.4 ComplaintAssignment

| Attribute | Detail |
|-----------|--------|
| **Purpose** | Assignment history — department, office, officer, with reassignment and escalation trail |
| **Aggregate owner** | Complaint |
| **Table** | `cmp_complaint_assignment` |
| **Cardinality** | N:1 Complaint |

| Field | Type | Notes |
|-------|------|-------|
| `complaintId` | UUID FK | → `cmp_complaint` |
| `assignmentType` | Enum | `INITIAL`, `REASSIGNMENT`, `TRANSFER`, `ESCALATION` |
| `departmentId` | UUID FK | → `org_department` |
| `officeId` | UUID FK | → `org_office` |
| `officerUserId` | UUID FK | → `idm_user` |
| `assignedByUserId` | UUID FK | → `idm_user` |
| `assignmentStatus` | Enum | `PENDING`, `ACCEPTED`, `REJECTED`, `COMPLETED`, `SUPERSEDED` |
| `assignedAt` | Instant | |
| `acceptedAt` | Instant | |
| `rejectedAt` | Instant | |
| `rejectionReasonKey` | String | MDM key |
| `remarks` | Text | |
| `isCurrent` | Boolean | Exactly one `true` per complaint at a time |

| Concern | Behavior |
|---------|----------|
| **Soft delete** | No — superseded via `isCurrent = false` and `assignmentStatus = SUPERSEDED` |
| **Optimistic locking** | Yes on accept/reject actions |
| **Auditing** | Yes |
| **Referenced BC** | IDM, ORG, MDM |

**Assignment model:**

```
Complaint
    ↓
Department (ORG)
    ↓
Office (ORG)
    ↓
Officer (IDM User linked via ORG Employee)
    ↓
Reassignment → new ComplaintAssignment row; previous marked SUPERSEDED
    ↓
Escalation → ComplaintEscalation + new ComplaintAssignment at higher level
```

---

### 4.5 ComplaintResolution

| Attribute | Detail |
|-----------|--------|
| **Purpose** | Resolution attempts — supports rejection at verification and reopen |
| **Aggregate owner** | Complaint |
| **Table** | `cmp_complaint_resolution` |
| **Cardinality** | N:1 Complaint (multiple attempts if reopened) |

| Field | Type | Notes |
|-------|------|-------|
| `complaintId` | UUID FK | → `cmp_complaint` |
| `resolvedByUserId` | UUID FK | → `idm_user` |
| `resolutionCodeKey` | String | MDM key under `COMPLAINT_RESOLUTION_CODE` |
| `resolutionSummary` | Text | Officer narrative |
| `resolvedAt` | Instant | |
| `citizenVerified` | Boolean | Set during VERIFIED transition |
| `citizenVerifiedAt` | Instant | |
| `citizenRejectionReason` | Text | When citizen rejects resolution |
| `isCurrent` | Boolean | Latest resolution attempt |

| Concern | Behavior |
|---------|----------|
| **Soft delete** | No — historical resolutions retained |
| **Optimistic locking** | Yes |
| **Auditing** | Yes |
| **Referenced BC** | IDM, MDM |

---

### 4.6 ComplaintSla

| Attribute | Detail |
|-----------|--------|
| **Purpose** | SLA milestone tracking per complaint — deadlines computed from priority at submission |
| **Aggregate owner** | Complaint |
| **Table** | `cmp_complaint_sla` |
| **Cardinality** | 1:1 with Complaint |

| Field | Type | Notes |
|-------|------|-------|
| `complaintId` | UUID FK | → `cmp_complaint` (unique) |
| `priority` | Enum | Snapshot at SLA creation |
| `responseDueAt` | Instant | First response deadline |
| `resolutionDueAt` | Instant | Full resolution deadline |
| `responseMetAt` | Instant | Actual first response |
| `resolutionMetAt` | Instant | Actual resolution |
| `responseBreached` | Boolean | |
| `resolutionBreached` | Boolean | |
| `pausedAt` | Instant | When WAITING_FOR_CITIZEN |
| `totalPausedDuration` | Duration | Accumulated pause time |

| Concern | Behavior |
|---------|----------|
| **Soft delete** | Yes — follows complaint |
| **Optimistic locking** | Yes |
| **Auditing** | Yes |
| **Referenced BC** | None (priority validated via MDM at creation) |

> SLA **enforcement** (breach detection, alerts) is a future platform scheduler concern. CMP records milestones and breach flags only.

---

### 4.7 ComplaintEscalation

| Attribute | Detail |
|-----------|--------|
| **Purpose** | Escalation events when SLA breached or manually escalated |
| **Aggregate owner** | Complaint |
| **Table** | `cmp_complaint_escalation` |
| **Cardinality** | N:1 Complaint |

| Field | Type | Notes |
|-------|------|-------|
| `complaintId` | UUID FK | → `cmp_complaint` |
| `escalationLevel` | Enum | `L1`, `L2`, `L3`, `DEPARTMENT_HEAD`, `COLLECTOR` |
| `escalationReason` | Enum | `SLA_BREACH`, `MANUAL`, `CITIZEN_REQUEST`, `OFFICER_REQUEST` |
| `escalatedByUserId` | UUID FK | → `idm_user` |
| `escalatedToUserId` | UUID FK | → `idm_user` (optional — may escalate to role/department) |
| `escalatedToDepartmentId` | UUID FK | → `org_department` |
| `remarks` | Text | |
| `escalatedAt` | Instant | |

| Concern | Behavior |
|---------|----------|
| **Soft delete** | No — append-only |
| **Optimistic locking** | No — insert-only |
| **Auditing** | `createdBy` / `createdDate` |
| **Referenced BC** | IDM, ORG |

---

### 4.8 ComplaintComment

| Attribute | Detail |
|-----------|--------|
| **Purpose** | Internal and citizen-visible remarks on a complaint |
| **Aggregate owner** | Complaint |
| **Table** | `cmp_complaint_comment` |
| **Cardinality** | N:1 Complaint |

| Field | Type | Notes |
|-------|------|-------|
| `complaintId` | UUID FK | → `cmp_complaint` |
| `authorUserId` | UUID FK | → `idm_user` |
| `commentText` | Text | |
| `visibility` | Enum | `INTERNAL`, `CITIZEN_VISIBLE` |
| `commentType` | Enum | `REMARK`, `CLARIFICATION_REQUEST`, `CLARIFICATION_RESPONSE`, `SYSTEM` |

| Concern | Behavior |
|---------|----------|
| **Soft delete** | Yes — author or admin may retract |
| **Optimistic locking** | Yes |
| **Auditing** | Yes |
| **Referenced BC** | IDM |

---

### 4.9 ComplaintAttachment

| Attribute | Detail |
|-----------|--------|
| **Purpose** | Links complaint evidence to DOC module documents |
| **Aggregate owner** | Complaint |
| **Table** | `cmp_complaint_attachment` |
| **Cardinality** | N:1 Complaint |

| Field | Type | Notes |
|-------|------|-------|
| `complaintId` | UUID FK | → `cmp_complaint` |
| `documentId` | UUID FK | → `doc_document` |
| `attachmentType` | Enum | `IMAGE`, `VIDEO`, `DOCUMENT`, `LINK` |
| `displayName` | String | Citizen-facing label |
| `uploadedByUserId` | UUID FK | → `idm_user` |
| `sortOrder` | Integer | Display sequence |

| Concern | Behavior |
|---------|----------|
| **Soft delete** | Yes — unlinks from complaint; DOC document soft-deleted separately via DOC service |
| **Optimistic locking** | Yes |
| **Auditing** | Yes |
| **Referenced BC** | DOC, IDM |

---

### 4.10 ComplaintWatcher

| Attribute | Detail |
|-----------|--------|
| **Purpose** | Users subscribed to complaint update notifications |
| **Aggregate owner** | Complaint |
| **Table** | `cmp_complaint_watcher` |
| **Cardinality** | N:1 Complaint |

| Field | Type | Notes |
|-------|------|-------|
| `complaintId` | UUID FK | → `cmp_complaint` |
| `userId` | UUID FK | → `idm_user` |
| `watcherRole` | Enum | `CITIZEN`, `OFFICER`, `DEPARTMENT_HEAD`, `COLLECTOR`, `ADMIN` |
| `notifyOnStatusChange` | Boolean | Default true |
| `notifyOnComment` | Boolean | |

| Concern | Behavior |
|---------|----------|
| **Soft delete** | Yes — unsubscribe |
| **Optimistic locking** | Yes |
| **Auditing** | Yes |
| **Referenced BC** | IDM |

Unique: `(complaint_id, user_id)` where `deleted = false`.

---

### 4.11 ComplaintTag

| Attribute | Detail |
|-----------|--------|
| **Purpose** | Tag assignment for filtering and reporting |
| **Aggregate owner** | Complaint |
| **Table** | `cmp_complaint_tag` |
| **Cardinality** | N:M Complaint ↔ MDM tag keys |

| Field | Type | Notes |
|-------|------|-------|
| `complaintId` | UUID FK | → `cmp_complaint` |
| `tagKey` | String | MDM key under `COMPLAINT_TAG` |
| `appliedByUserId` | UUID FK | → `idm_user` |

| Concern | Behavior |
|---------|----------|
| **Soft delete** | Yes — remove tag |
| **Optimistic locking** | Yes |
| **Auditing** | Yes |
| **Referenced BC** | MDM, IDM |

---

### 4.12 ComplaintDuplicate

| Attribute | Detail |
|-----------|--------|
| **Purpose** | Links a duplicate complaint to its primary record |
| **Aggregate owner** | Complaint (primary side) |
| **Table** | `cmp_complaint_duplicate` |
| **Cardinality** | N:1 primary Complaint |

| Field | Type | Notes |
|-------|------|-------|
| `primaryComplaintId` | UUID FK | → `cmp_complaint` |
| `duplicateComplaintId` | UUID FK | → `cmp_complaint` |
| `detectedBy` | Enum | `MANUAL`, `SYSTEM` (future AI out of scope) |
| `detectedByUserId` | UUID FK | → `idm_user` (null for SYSTEM) |
| `similarityScore` | Decimal | Optional; future use |
| `remarks` | Text | |

| Concern | Behavior |
|---------|----------|
| **Soft delete** | Yes — unlink duplicate |
| **Optimistic locking** | Yes |
| **Auditing** | Yes |
| **Referenced BC** | CMP (self), IDM |

Unique: `(primary_complaint_id, duplicate_complaint_id)` where `deleted = false`.

---

### 4.13 ComplaintMerge

| Attribute | Detail |
|-----------|--------|
| **Purpose** | Append-only record when complaints are merged into a primary |
| **Aggregate owner** | Complaint (surviving record) |
| **Table** | `cmp_complaint_merge` |
| **Cardinality** | N:1 surviving Complaint |

| Field | Type | Notes |
|-------|------|-------|
| `survivingComplaintId` | UUID FK | → `cmp_complaint` |
| `mergedComplaintId` | UUID FK | → `cmp_complaint` |
| `mergedByUserId` | UUID FK | → `idm_user` |
| `mergeReason` | Text | |
| `mergedAt` | Instant | |

| Concern | Behavior |
|---------|----------|
| **Soft delete** | No — append-only |
| **Optimistic locking** | No — insert-only |
| **Auditing** | `createdBy` / `createdDate` |
| **Referenced BC** | CMP (self), IDM |

---

### 4.14 ComplaintRating

| Attribute | Detail |
|-----------|--------|
| **Purpose** | Citizen satisfaction feedback after closure |
| **Aggregate owner** | Complaint |
| **Table** | `cmp_complaint_rating` |
| **Cardinality** | 1:1 with Complaint (optional, post-CLOSED) |

| Field | Type | Notes |
|-------|------|-------|
| `complaintId` | UUID FK | → `cmp_complaint` (unique) |
| `ratedByUserId` | UUID FK | → `idm_user` (citizen) |
| `rating` | Integer | 1–5 |
| `feedback` | Text | Optional comment |
| `ratedAt` | Instant | |

| Concern | Behavior |
|---------|----------|
| **Soft delete** | Yes |
| **Optimistic locking** | Yes |
| **Auditing** | Yes |
| **Referenced BC** | IDM |

---

## 5. Enums (CMP-Owned)

Stored as `@Enumerated(STRING)`, values `UPPER_SNAKE_CASE`.

### ComplaintStatus

`DRAFT`, `SUBMITTED`, `ASSIGNED`, `ACCEPTED`, `IN_PROGRESS`, `WAITING_FOR_CITIZEN`, `RESOLVED`, `VERIFIED`, `CLOSED`, `REJECTED`, `CANCELLED`, `REOPENED`

### ComplaintPriority

`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`, `EMERGENCY`

### ComplaintSource

`CITIZEN_PORTAL`, `OFFICER_PORTAL`, `COLLECTOR_PORTAL`, `MOBILE_APP`, `WHATSAPP`, `EMAIL`, `CALL_CENTER`, `WALK_IN`, `API`

### Other Enums

| Enum | Values |
|------|--------|
| `AssignmentType` | `INITIAL`, `REASSIGNMENT`, `TRANSFER`, `ESCALATION` |
| `AssignmentStatus` | `PENDING`, `ACCEPTED`, `REJECTED`, `COMPLETED`, `SUPERSEDED` |
| `EscalationLevel` | `L1`, `L2`, `L3`, `DEPARTMENT_HEAD`, `COLLECTOR` |
| `EscalationReason` | `SLA_BREACH`, `MANUAL`, `CITIZEN_REQUEST`, `OFFICER_REQUEST` |
| `AttachmentType` | `IMAGE`, `VIDEO`, `DOCUMENT`, `LINK` |
| `CommentVisibility` | `INTERNAL`, `CITIZEN_VISIBLE` |
| `CommentType` | `REMARK`, `CLARIFICATION_REQUEST`, `CLARIFICATION_RESPONSE`, `SYSTEM` |
| `WatcherRole` | `CITIZEN`, `OFFICER`, `DEPARTMENT_HEAD`, `COLLECTOR`, `ADMIN` |
| `DuplicateDetectedBy` | `MANUAL`, `SYSTEM` |

---

## 6. Complaint Lifecycle

### 6.1 State Diagram

```
                    ┌──────────┐
                    │  DRAFT   │
                    └────┬─────┘
                         │ submit
                         ▼
                    ┌──────────┐     reject      ┌──────────┐
         ┌─────────│SUBMITTED │────────────────►│ REJECTED │
         │         └────┬─────┘                 └──────────┘
         │              │ assign
         │              ▼
         │         ┌──────────┐
         │         │ ASSIGNED │
         │         └────┬─────┘
         │         accept│  reject
         │              ▼         ──────────────► REJECTED
         │         ┌──────────┐
         │         │ ACCEPTED │
         │         └────┬─────┘
         │              │ start work
         │              ▼
         │    ┌─────────────────┐◄────────────────┐
         │    │  IN_PROGRESS    │                 │
         │    └────────┬────────┘                 │
         │   wait     │              resume       │
         │              ▼                          │
         │    ┌─────────────────────┐              │
         │    │ WAITING_FOR_CITIZEN │──────────────┘
         │    └─────────────────────┘
         │              │ resolve
         │              ▼
         │         ┌──────────┐
         │         │ RESOLVED │
         │         └────┬─────┘
         │    verify    │    reject resolution
         │              ▼              ──────► REOPENED ──► IN_PROGRESS
         │         ┌──────────┐
         │         │ VERIFIED │
         │         └────┬─────┘
         │              │ close
         │              ▼
         │         ┌──────────┐
         └────────►│  CLOSED  │
                   └──────────┘

  CANCELLED ← from DRAFT, SUBMITTED (citizen/admin cancel before assignment)
```

### 6.2 Transition Matrix

| From | To | Actor | Business Rule |
|------|----|-------|---------------|
| `DRAFT` | `SUBMITTED` | Citizen / Officer | Title, description, category required; citizen user set |
| `DRAFT` | `CANCELLED` | Citizen | Only owner may cancel draft |
| `SUBMITTED` | `ASSIGNED` | System / Admin | Department and officer assigned; WRK instance started |
| `SUBMITTED` | `REJECTED` | Admin / Collector | Rejection reason required (MDM key) |
| `SUBMITTED` | `CANCELLED` | Citizen / Admin | Before assignment only |
| `ASSIGNED` | `ACCEPTED` | Officer | Current assignment accepted |
| `ASSIGNED` | `REJECTED` | Officer | Officer rejection reason; triggers reassignment |
| `ASSIGNED` | `ASSIGNED` | Admin | Reassignment — new assignment row; status stays ASSIGNED |
| `ACCEPTED` | `IN_PROGRESS` | Officer | Officer begins work |
| `IN_PROGRESS` | `WAITING_FOR_CITIZEN` | Officer | Clarification requested; SLA paused |
| `WAITING_FOR_CITIZEN` | `IN_PROGRESS` | Citizen / Officer | Clarification provided |
| `IN_PROGRESS` | `RESOLVED` | Officer | Resolution record created; resolution code required |
| `RESOLVED` | `VERIFIED` | Citizen | Citizen confirms satisfaction |
| `RESOLVED` | `REOPENED` | Citizen | Citizen rejects resolution; reason required |
| `VERIFIED` | `CLOSED` | System / Admin | Auto-close after verification or admin close |
| `REOPENED` | `IN_PROGRESS` | Officer | New resolution cycle; previous resolution `isCurrent = false` |
| `CLOSED` | `REOPENED` | Admin / Collector | Extraordinary reopen; audit + escalation |

### 6.3 Invalid Transitions

| Invalid | Reason |
|---------|--------|
| Any → `DRAFT` | Draft is creation-only |
| `CLOSED` → `IN_PROGRESS` | Must go through `REOPENED` |
| `REJECTED` → any except view | Terminal for that submission |
| `CANCELLED` → any | Terminal |
| `RESOLVED` → `CLOSED` | Must pass through `VERIFIED` (or admin override with audit) |
| `ASSIGNED` → `RESOLVED` | Must accept and progress through `IN_PROGRESS` |
| Officer actions on unassigned complaint | Permission + assignment check |

Every transition validates:

1. Current status matches expected `fromStatus`
2. Actor has permission (`cmp:complaint:*` via Security)
3. Optimistic lock `version` matches
4. Required child records exist (assignment, resolution, etc.)

### 6.4 Workflow Integration per Transition

CMP **never** stores workflow definitions. On applicable transitions, CMP calls WRK services:

| CMP Transition | WRK Action |
|----------------|------------|
| `SUBMITTED` → `ASSIGNED` | Create `WorkflowInstance` with `referenceType = "COMPLAINT"`, `referenceId = complaint.id`; use published complaint workflow definition |
| `ASSIGNED` → `ACCEPTED` | Complete current WRK user task (accept) |
| `ASSIGNED` → `REJECTED` | Complete WRK task with rejection outcome; WRK routes per definition |
| `IN_PROGRESS` → `RESOLVED` | Complete resolution task |
| `RESOLVED` → `VERIFIED` | Complete citizen verification task |
| `REOPENED` | WRK may create follow-up task per definition — CMP requests via `WorkflowTaskService` |

CMP reads workflow state via:

```java
workflowInstanceService.getByReference("COMPLAINT", complaintId)
```

CMP does **not** maintain a `workflow_instance_id` FK. Optional denormalized cache may be added in CMP-010+ for read performance; WRK remains source of truth.

### 6.5 Audit Events per Transition

Each transition publishes a CMP domain event **and** creates an AUD record:

| Transition | CMP Domain Event | AUD `eventCode` pattern | AUD `action` |
|------------|------------------|-------------------------|--------------|
| Create | `ComplaintCreatedEvent` | `{code}-CREATED` | `CREATE` |
| Submit | `ComplaintSubmittedEvent` | `{code}-SUBMITTED` | `TRANSITION` |
| Assign | `ComplaintAssignedEvent` | `{code}-ASSIGNED` | `ASSIGN` |
| Accept | `ComplaintAcceptedEvent` | `{code}-ACCEPTED` | `TRANSITION` |
| Reject | `ComplaintRejectedEvent` | `{code}-REJECTED` | `TRANSITION` |
| Resolve | `ComplaintResolvedEvent` | `{code}-RESOLVED` | `TRANSITION` |
| Verify | `ComplaintVerifiedEvent` | `{code}-VERIFIED` | `TRANSITION` |
| Close | `ComplaintClosedEvent` | `{code}-CLOSED` | `TRANSITION` |
| Reopen | `ComplaintReopenedEvent` | `{code}-REOPENED` | `TRANSITION` |
| Escalate | `ComplaintEscalatedEvent` | `{code}-ESCALATED-{level}` | `TRANSITION` |

AUD `entityType = "COMPLAINT"`, `entityId = complaint.id`. Field diffs via `AuditChange` for updates (category change, priority change, etc.).

### 6.6 Notifications per Transition

| Transition | Citizen | Officer | Dept Head | Collector | Admin |
|------------|---------|---------|-----------|-----------|-------|
| Submitted | ✓ confirmation | | ✓ new complaint | | ✓ |
| Assigned | ✓ assigned to dept | ✓ new in queue | ✓ | | |
| Accepted | ✓ officer name | | | | |
| Rejected (officer) | ✓ reason | | ✓ reassignment needed | | |
| Rejected (admin) | ✓ reason | | | | |
| Waiting for citizen | ✓ action required | | | | |
| Clarification received | | ✓ | | | |
| Resolved | ✓ verify request | | | | |
| Verified | ✓ thank you | ✓ | | | |
| Reopened | ✓ | ✓ | ✓ | ✓ | ✓ |
| Closed | ✓ closure summary | ✓ | | | |
| Escalated | ✓ | ✓ | ✓ | ✓ | ✓ |
| SLA breach (future) | | ✓ | ✓ | ✓ | ✓ |

Notifications created via `NotificationService` (NTF) using templates:

- `CMP_COMPLAINT_SUBMITTED`
- `CMP_COMPLAINT_ASSIGNED`
- `CMP_COMPLAINT_ACCEPTED`
- `CMP_COMPLAINT_RESOLVED`
- `CMP_COMPLAINT_VERIFICATION_REQUEST`
- `CMP_COMPLAINT_CLOSED`
- `CMP_COMPLAINT_ESCALATED`
- `CMP_COMPLAINT_SLA_BREACH`

Template variables: `{{complaintNumber}}`, `{{citizenName}}`, `{{officerName}}`, `{{departmentName}}`, `{{status}}`, `{{dueDate}}`.

---

## 7. Priority Model

Priority is a CMP enum validated against MDM type `COMPLAINT_PRIORITY`. SLA defaults below are CMP configuration (`CmpProperties`); MDM may override per deployment via metadata JSON in `data_value`.

| Priority | Response SLA | Resolution SLA | Escalation Policy | Officer Level | Collector Notify |
|----------|-------------|----------------|-------------------|---------------|------------------|
| **LOW** | 5 business days | 30 calendar days | Escalate to L2 after resolution breach | L1 officer | No |
| **MEDIUM** | 2 business days | 15 calendar days | Escalate to L2 at 75% resolution SLA | L1 officer | No |
| **HIGH** | 1 business day | 7 calendar days | Escalate to Dept Head at 50%; Collector at breach | L1–L2 officer | On breach |
| **CRITICAL** | 8 hours | 3 calendar days | Escalate to Dept Head at 25%; Collector at 50% | L2 officer | At 50% SLA |
| **EMERGENCY** | 2 hours | 24 hours | Immediate Dept Head + Collector notification; L3 escalation at 50% | Senior officer / Dept Head | Immediately |

**SLA pause:** `WAITING_FOR_CITIZEN` pauses resolution SLA clock (`ComplaintSla.pausedAt`).

**SLA breach:** CMP sets `responseBreached` / `resolutionBreached` flags and emits `ComplaintEscalatedEvent`. Automated escalation execution is deferred to a future platform scheduler.

---

## 8. Complaint Source & Channel

### Source (`ComplaintSource` enum)

Validates against MDM type `COMPLAINT_SOURCE`.

| Source | Description | Typical Actor |
|--------|-------------|---------------|
| `CITIZEN_PORTAL` | Web citizen portal | Citizen |
| `OFFICER_PORTAL` | Officer-assisted entry | Officer |
| `COLLECTOR_PORTAL` | Collector-initiated | Collector |
| `MOBILE_APP` | Flutter mobile app | Citizen / Officer |
| `WHATSAPP` | WhatsApp integration (future) | Citizen |
| `EMAIL` | Email ingestion (future) | System |
| `CALL_CENTER` | Call center agent | Officer |
| `WALK_IN` | Physical counter | Officer |
| `API` | External system integration | System |

### Channel (`channel` field — MDM `COMPLAINT_CHANNEL`)

Finer-grained delivery context: `WEB`, `ANDROID`, `IOS`, `SMS`, `IVR`, `COUNTER`, `API`.

Source + channel together drive notification template selection and reporting.

---

## 9. Attachment Strategy

CMP stores **metadata links only**. Binary content lives in DOC.

### Flow

```
Citizen/Officer uploads file
        ↓
DOC DocumentService.create (storage, versioning)
        ↓
CMP ComplaintAttachmentService.link(documentId, complaintId)
        ↓
cmp_complaint_attachment row
```

### Rules

| Rule | Value |
|------|-------|
| **Max attachments per complaint** | 10 |
| **Max file size** | 10 MB (images/documents), 50 MB (video) — enforced at DOC upload |
| **Versioning** | DOC `DocumentVersion` — new upload creates new version; CMP link points to current document version via `documentId` (DOC tracks versions internally) |
| **Allowed MIME types** | `image/jpeg`, `image/png`, `image/webp`, `application/pdf`, `video/mp4`, `video/quicktime` |
| **LINK type** | External URL stored in DOC as document with `mimeType = text/uri-list` |
| **Visibility** | Inherited from DOC document visibility; complaint attachments default `INTERNAL` unless citizen-uploaded |

### Attachment Types

| Type | DOC Integration |
|------|-----------------|
| `IMAGE` | Photo evidence |
| `VIDEO` | Video evidence |
| `DOCUMENT` | PDF, official letters |
| `LINK` | External reference URL |

On complaint soft-delete: CMP soft-deletes attachment links; DOC documents archived via DOC service (no cascade delete of binaries).

---

## 10. Workflow Integration

### Contract

| WRK Field | CMP Value |
|-----------|-----------|
| `referenceType` | `"COMPLAINT"` (constant: `CmpWorkflowReferenceType.COMPLAINT`) |
| `referenceId` | `complaint.getId()` |

### Sequence — Complaint Submission

```
1. ComplaintService.submit(complaintId)
2. CMP validates transition DRAFT → SUBMITTED
3. CMP calls WorkflowDefinitionService.getByCode("COMPLAINT_STANDARD")  // MDM/config driven
4. CMP calls WorkflowInstanceService.create({
     referenceType: "COMPLAINT",
     referenceId: complaintId,
     workflowVersionId: publishedVersion.id
   })
5. WRK creates WorkflowInstance + initial WorkflowTask(s)
6. CMP transitions to ASSIGNED (or SUBMITTED until auto-assign completes)
7. CMP emits ComplaintSubmittedEvent
8. AUD + NTF wired by application layer (future) or CMP orchestration service
```

### CMP Responsibilities vs WRK Responsibilities

| Concern | Owner |
|---------|-------|
| Complaint status enum and business rules | CMP |
| Workflow definition, steps, transitions | WRK |
| Task assignment to users/roles | WRK |
| Task completion triggers | WRK emits `WorkflowTaskCompletedEvent`; CMP listener reacts (future) |
| SLA on workflow steps | WRK step metadata; CMP `ComplaintSla` for business SLA |
| Workflow history | WRK `WorkflowHistory` (append-only) |

CMP **must not** duplicate `WorkflowStep`, `WorkflowTransition`, or execution logic.

---

## 11. Domain Events

Plain Java records in `com.govos.cmp.event`. Spring publishing deferred to CMP-008+.

| Event | Key Payload Fields |
|-------|-------------------|
| `ComplaintCreatedEvent` | complaintId, code, citizenUserId, source, occurredAt |
| `ComplaintSubmittedEvent` | complaintId, code, categoryKey, priority, occurredAt |
| `ComplaintAssignedEvent` | complaintId, departmentId, officeId, officerUserId, assignmentId, occurredAt |
| `ComplaintAcceptedEvent` | complaintId, officerUserId, assignmentId, occurredAt |
| `ComplaintRejectedEvent` | complaintId, rejectedByUserId, reasonKey, rejectionType (OFFICER/ADMIN), occurredAt |
| `ComplaintStatusChangedEvent` | complaintId, fromStatus, toStatus, changedByUserId, occurredAt |
| `ComplaintResolvedEvent` | complaintId, resolutionId, resolutionCodeKey, resolvedByUserId, occurredAt |
| `ComplaintVerifiedEvent` | complaintId, resolutionId, verifiedByUserId, occurredAt |
| `ComplaintClosedEvent` | complaintId, closureReasonKey, occurredAt |
| `ComplaintReopenedEvent` | complaintId, reopenedByUserId, reason, occurredAt |
| `ComplaintEscalatedEvent` | complaintId, escalationLevel, reason, escalatedToDepartmentId, occurredAt |
| `ComplaintCommentAddedEvent` | complaintId, commentId, authorUserId, visibility, occurredAt |
| `ComplaintAttachmentLinkedEvent` | complaintId, attachmentId, documentId, attachmentType, occurredAt |
| `ComplaintDuplicateLinkedEvent` | primaryComplaintId, duplicateComplaintId, detectedBy, occurredAt |
| `ComplaintMergedEvent` | survivingComplaintId, mergedComplaintId, mergedByUserId, occurredAt |
| `ComplaintRatedEvent` | complaintId, rating, ratedByUserId, occurredAt |
| `ComplaintSlaBreachedEvent` | complaintId, breachType (RESPONSE/RESOLUTION), occurredAt |

---

## 12. Master Data Integration

Constants in `com.govos.cmp.mdm.CmpMasterDataTypes`:

| MDM Type | Purpose | Example Keys |
|----------|---------|--------------|
| `COMPLAINT_CATEGORY` | Top-level classification | `WATER_SUPPLY`, `ROAD`, `SANITATION`, `ELECTRICITY` |
| `COMPLAINT_SUB_CATEGORY` | Sub-classification | `PIPE_LEAK`, `POTHOLE`, `GARBAGE_COLLECTION` |
| `COMPLAINT_TYPE` | Nature of grievance | `SERVICE_REQUEST`, `COMPLAINT`, `SUGGESTION` |
| `COMPLAINT_PRIORITY` | Priority with SLA metadata | `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`, `EMERGENCY` |
| `COMPLAINT_SOURCE` | Submission source labels | Mirrors `ComplaintSource` enum |
| `COMPLAINT_CHANNEL` | Delivery channel | `WEB`, `ANDROID`, `COUNTER` |
| `COMPLAINT_RESOLUTION_CODE` | Resolution classification | `FIXED`, `WORKAROUND`, `NOT_REPRODUCIBLE`, `DUPLICATE` |
| `COMPLAINT_CLOSURE_REASON` | Closure justification | `RESOLVED`, `NO_ACTION_REQUIRED`, `DUPLICATE`, `INVALID` |
| `COMPLAINT_REJECTION_REASON` | Rejection at intake/assignment | `OUT_OF_JURISDICTION`, `INSUFFICIENT_INFO`, `DUPLICATE` |
| `COMPLAINT_TAG` | Reporting tags | `VIP`, `SENSITIVE`, `MEDIA_ATTENTION` |

Validation pattern (same as ORG):

```java
masterDataRepository.findByTypeAndKeyAndDeletedFalse(
    CmpMasterDataTypes.COMPLAINT_CATEGORY, categoryKey)
    .orElseThrow(() -> new InvalidComplaintCategoryException(categoryKey));
```

Departments and offices are **not** MDM — they come from ORG (`DepartmentService`, `OfficeService`).

---

## 13. UML Domain Diagram (Textual)

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                         COMPLAINT AGGREGATE                                    │
│  ┌─────────────────────────────────────────────────────────────────────────┐ │
│  │                         «AggregateRoot»                                  │ │
│  │                           Complaint                                      │ │
│  │  - code, title, description, status, priority, source, channel        │ │
│  │  - categoryKey, subCategoryKey, complaintTypeKey                      │ │
│  │  - citizenUserId ────────────────► idm.User                           │ │
│  │  - organizationId ───────────────► org.Organization                   │ │
│  │  - departmentId ─────────────────► org.Department                     │ │
│  │  - officeId ─────────────────────► org.Office                           │ │
│  │  - assignedOfficerId ────────────► idm.User                           │ │
│  └──────────┬──────────────────────────────────────────────────────────────┘ │
│             │ 1                                                                │
│    ┌────────┼────────┬──────────┬───────────┬──────────┬───────────┐          │
│    │ 0..1  │ 1    * │ 1      * │ 1       * │ 1      * │ 1       * │          │
│    ▼       ▼        ▼          ▼           ▼          ▼           ▼          │
│ Location  Status  Assignment Resolution   Sla      Escalation   Comment       │
│ History   (log)   (history)  (attempts)  (1:1)    (log)                     │
│             │                                                                │
│    ┌────────┼────────┬──────────┬───────────┬──────────┐                      │
│    │ *      │ *    0..1│ *      │ *         │ *        │                      │
│    ▼        ▼        ▼    ▼       ▼           ▼          ▼                      │
│ Attachment Watcher  Tag  Duplicate Merge    Rating                          │
│     │                                                                        │
│     │ documentId ──────────────────► doc.Document                          │
└─────┼────────────────────────────────────────────────────────────────────────┘
      │
      │ referenceType="COMPLAINT", referenceId=complaint.id
      ▼
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  wrk.Workflow   │     │  aud.AuditEvent │     │ ntf.Notification│
│  Instance       │     │  + AuditChange  │     │  (via templates)│
└─────────────────┘     └─────────────────┘     └─────────────────┘

┌──────────────────────────────────────────────────────────────────────────────┐
│                         READ MODEL (not persisted)                           │
│  ComplaintTimeline = StatusHistory + Comments + Assignments + Escalations   │
│                      + WRK WorkflowHistory + AUD Events (filtered)           │
└──────────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────────┐
│                              MDM LOOKUPS                                       │
│  COMPLAINT_CATEGORY | SUB_CATEGORY | TYPE | PRIORITY | SOURCE | CHANNEL       │
│  RESOLUTION_CODE | CLOSURE_REASON | REJECTION_REASON | TAG                  │
└──────────────────────────────────────────────────────────────────────────────┘
```

---

## 14. Service Layer (Internal API — CMP-005+)

| Service | Responsibility |
|---------|----------------|
| `ComplaintService` | CRUD, submit, cancel, status queries |
| `ComplaintLifecycleService` | State machine transitions, invariant enforcement |
| `ComplaintAssignmentService` | Assign, reassign, accept, reject assignment |
| `ComplaintResolutionService` | Record resolution, citizen verify/reject |
| `ComplaintSlaService` | Create SLA, pause/resume, record breach |
| `ComplaintEscalationService` | Manual and SLA-triggered escalation |
| `ComplaintCommentService` | Add, list, soft-delete comments |
| `ComplaintAttachmentService` | Link/unlink DOC documents |
| `ComplaintWatcherService` | Subscribe/unsubscribe watchers |
| `ComplaintDuplicateService` | Link/unlink duplicates |
| `ComplaintMergeService` | Merge complaints |
| `ComplaintRatingService` | Record citizen rating |
| `ComplaintTimelineProjectionService` | Assemble timeline read model |
| `ComplaintOrchestrationService` | Coordinates WRK + AUD + NTF on transitions |

Cross-domain calls only through platform services — never repositories.

---

## 15. Security Integration

Permissions (defined in Security module — not implemented in CMP):

| Permission | Action |
|------------|--------|
| `cmp:complaint:create` | Create draft / submit |
| `cmp:complaint:read` | View complaint |
| `cmp:complaint:update` | Edit draft fields |
| `cmp:complaint:assign` | Assign / reassign |
| `cmp:complaint:accept` | Accept assignment |
| `cmp:complaint:reject` | Reject at intake or assignment |
| `cmp:complaint:resolve` | Record resolution |
| `cmp:complaint:verify` | Citizen verification |
| `cmp:complaint:close` | Close complaint |
| `cmp:complaint:reopen` | Reopen closed complaint |
| `cmp:complaint:escalate` | Manual escalation |
| `cmp:complaint:merge` | Merge duplicates |
| `cmp:complaint:comment` | Add comments |
| `cmp:complaint:admin` | Full admin override |

CMP validators check business rules; Security filter chain checks permissions.

---

## 16. Implementation Roadmap

| Sprint | ID | Deliverable |
|--------|-----|-------------|
| 1 | **CMP-001** | Architecture blueprint (this document) |
| 1 | CMP-002 | Entity classes and enums |
| 1 | CMP-003 | Flyway `V2_0_0__complaint.sql` |
| 1 | CMP-004 | Repositories |
| 1 | CMP-005 | DTOs and MapStruct mappers |
| 1 | CMP-006 | Validators and exceptions |
| 1 | CMP-007 | Core services (Complaint, Lifecycle, Assignment) |
| 1 | CMP-008 | Domain events and orchestration service |
| 1 | CMP-009 | Resolution, SLA, Escalation, Comment, Attachment services |
| 1 | CMP-010 | Duplicate, Merge, Rating, Timeline projection |
| 2 | CMP-011 | REST controllers in `govos-api` |
| 2 | CMP-012 | MDM seed data for complaint types |
| 2 | CMP-013 | NTF template seed data |
| 2 | CMP-014 | WRK complaint workflow definition seed |
| 2 | CMP-015 | Angular citizen/officer complaint features |

### Future (Out of Scope)

- AI classification and duplicate detection
- Elasticsearch full-text search
- OCR for attachment processing
- WhatsApp / email ingestion adapters
- GIS heat maps
- Automated SLA scheduler
- BPMN import

---

## 17. Related Documentation

| Document | Location |
|----------|----------|
| ADR-001 Modular Monolith | `govos-architecture/docs/90-adr/adr-001-modular-monolith.md` |
| ADR-002 DDD Package Structure | `govos-architecture/docs/90-adr/adr-002-domain-driven-design.md` |
| Entity Standards | `govos-architecture/docs/06-engineering/entity-standards.md` |
| Workflow Engine (WRK) | `govos-domain/wrk/README.md` |
| Document Management (DOC) | `govos-domain/doc/README.md` |
| Notification (NTF) | `govos-domain/ntf/README.md` |
| Audit (AUD) | `govos-domain/audit/README.md` |
| Master Data (MDM) | `govos-domain/mdm/README.md` |
| Organization (ORG) | `govos-domain/org/README.md` |
| Project Scope | `govos-architecture/docs/00-project-charter/project-scope.md` |
| Flyway Convention | `V2_0_0__complaint.sql` (CMP-003) |

---

## 18. Summary

CMP is a **single-aggregate-root** bounded context that models citizen grievances end-to-end while delegating identity, organization, documents, notifications, workflow orchestration, audit, and configurable lookups to existing platform modules. The complaint lifecycle is a explicit state machine with fourteen child entity types, append-only history for status/assignment/escalation/merge, and a composed timeline read model. This document is the authoritative blueprint for implementation prompts CMP-002 onwards.
