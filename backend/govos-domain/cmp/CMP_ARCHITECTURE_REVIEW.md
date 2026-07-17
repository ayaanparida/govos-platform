# CMP-001.5 — Complaint Domain Architecture Review

| Field | Value |
|-------|-------|
| **Review ID** | CMP-001.5 |
| **Subject** | Citizen Grievance Management System (CGMS) — `com.govos.cmp` |
| **Documents reviewed** | `cmp/README.md` (CMP-001), `govos-architecture/docs/backend/product-architecture.md` |
| **Reviewer role** | Chief Enterprise Architect |
| **Date** | 2026-07-18 |
| **Verdict** | **Approved with conditions** — proceed to CMP-002 after applying mandatory amendments below |

> Documentation-only review. No platform modules modified. No Java, SQL, REST, or Angular generated.

---

## 1. Executive Summary

CMP-001 presents a **sound, enterprise-grade DDD design** aligned with GovOS product architecture principles: single aggregate root, platform service consumption, WRK polymorphic binding, DOC link strategy, and separation of business status from workflow orchestration.

The design is **implementation-ready** after resolving **7 mandatory amendments** and **12 recommended improvements** documented in this review.

| Area | Rating | Summary |
|------|--------|---------|
| Aggregate design | ✅ Strong | Single-root model is correct for v1 |
| Platform integration | ✅ Strong | No duplication of IDM, DOC, WRK, AUD, NTF |
| Lifecycle | ⚠️ Good with gaps | Missing `ARCHIVED`; officer-reject ambiguity |
| Workflow ownership | ✅ Strong | WRK owns process; CMP owns business status |
| Notifications | ⚠️ Good with gaps | Catalogue incomplete vs lifecycle |
| Audit scope | ⚠️ Good with gaps | Needs explicit full action matrix |
| GIS / multi-tenancy | ⚠️ Adequate | ULB and GeoJSON extension points missing |
| SLA model | ⚠️ Good with gaps | Category-tier SLA not modeled |
| AI readiness | ✅ Adequate | Extension points identifiable; correctly deferred |
| Implementation order | ✅ Strong | Phased roadmap is logical |

---

## 2. Review Scope

This review validates CMP-001 against:

- Enterprise DDD aggregate boundaries
- GovOS Product Architecture Layer (`product-architecture.md`)
- ADR-002 bounded-context package structure
- Platform module ownership (MDM, IDM, ORG, DOC, NTF, WRK, AUD, SEC)
- CGMS business requirements from project charter / project scope

**Out of scope:** Java implementation, Flyway scripts, REST contracts, Angular UX, security implementation, workflow engine implementation.

---

## 3. Aggregate Root Analysis

### 3.1 Review Candidates

The review checklist named eight candidates. CMP-001 uses slightly different naming in two cases (`ComplaintRating` instead of `ComplaintFeedback`; no `ComplaintCategory` entity). Assessment below uses both names.

| Candidate | Verdict | Classification | Rationale |
|-----------|---------|----------------|-----------|
| **Complaint** | ✅ **Aggregate Root** | Root entity (`cmp_complaint`) | Correct consistency boundary for status, assignment, resolution, SLA invariants |
| **ComplaintCategory** | ✅ **Not an entity** | MDM reference on `Complaint` | Category is configurable master data (`COMPLAINT_CATEGORY`); duplicating as entity would violate MDM ownership and create sync drift |
| **ComplaintAssignment** | ✅ **Child entity** | Append-only history within aggregate | Cannot exist without parent; `isCurrent` flag models active assignment; invariants tied to complaint status |
| **ComplaintComment** | ✅ **Child entity** | Within aggregate (v1) | Scoped to one complaint; extract to separate aggregate only if comment volume exceeds aggregate load thresholds (see §12) |
| **ComplaintAttachment** | ✅ **Child entity** | Link entity within aggregate | Metadata only; binary ownership is DOC |
| **ComplaintFeedback** | ✅ **Child entity** | Same as `ComplaintRating` | Post-closure 1:1 feedback; rename to `ComplaintFeedback` is optional — `ComplaintRating` is acceptable if UI uses "Feedback" label |
| **ComplaintEscalation** | ✅ **Child entity** | Append-only log within aggregate | Escalation is a complaint lifecycle event, not an independent business object |

### 3.2 Findings — Aggregate Design

| ID | Finding | Severity |
|----|---------|----------|
| AR-01 | Single aggregate root decision is **correct** for CGMS v1 consistency requirements | Info |
| AR-02 | `ComplaintDuplicate` creates **cross-aggregate references** within CMP (primary ↔ duplicate complaints). Acceptable if all mutations go through `ComplaintDuplicateService` with transactional boundaries — document explicitly in CMP-002 | Medium |
| AR-03 | `ComplaintMerge` affects two complaint records — requires **two-aggregate orchestration** in application service; cannot be a single-aggregate invariant | Medium |
| AR-04 | CMP-001 defines 14 child entity types — aggregate is **large but justified** for government grievance auditability; monitor load in CMP-010 | Low |

### 3.3 Recommendations — Aggregates

| ID | Recommendation | Priority |
|----|----------------|----------|
| AR-R1 | **Do not** introduce `ComplaintCategory` entity — keep MDM keys on `Complaint` | Mandatory |
| AR-R2 | Rename `ComplaintRating` → `ComplaintFeedback` **only if** product terminology standardizes on "Feedback"; otherwise keep `ComplaintRating` and map in API/DTO layer | Optional |
| AR-R3 | Document in CMP-002 README that duplicate/merge operations are **application-level transactions** spanning two `Complaint` aggregate instances | Mandatory |
| AR-R4 | Defer `ComplaintComment` extraction to separate aggregate until >500 comments per complaint or performance testing proves need | Future |

---

## 4. Entity Relationship Validation

### 4.1 Expected Relationship Chain

```
Complaint (root)
    ├── ComplaintStatusHistory      (1:* append-only)
    ├── ComplaintAssignment         (1:* append-only history; 1 current)
    ├── ComplaintComment            (1:*)
    ├── ComplaintAttachment         (1:* → doc.Document)
    ├── ComplaintFeedback/Rating    (1:0..1)
    ├── ComplaintResolution         (1:* attempts)
    ├── ComplaintSla                (1:1)
    ├── ComplaintEscalation         (1:* append-only)
    ├── ComplaintLocation           (1:0..1)
    ├── ComplaintWatcher            (1:*)
    ├── ComplaintTag                (1:*)
    ├── ComplaintDuplicate          (*:* link via service)
    └── ComplaintMerge              (1:* append-only)

External (platform — no CMP FK ownership of process state):
    WorkflowInstance    (WRK — lookup by referenceType + referenceId)
    Document            (DOC — FK from ComplaintAttachment only)
    AuditEvent          (AUD — polymorphic entityType + entityId)
    Notification        (NTF — created by orchestration)
```

### 4.2 Findings — Relationships

| ID | Finding | Severity |
|----|---------|----------|
| ER-01 | Relationship hierarchy in CMP-001 is **coherent and complete** for v1 CGMS | Info |
| ER-02 | `ComplaintAttachment.documentId` links to `Document` but **does not pin `documentVersionId`** — evidence integrity at resolution time may be ambiguous if document is versioned after link | Medium |
| ER-03 | Denormalized fields on `Complaint` (`departmentId`, `officeId`, `assignedOfficerId`) duplicate latest assignment — **acceptable** for query performance; must be updated atomically with assignment service | Low |
| ER-04 | Self-references (`primaryComplaintId`, `mergedIntoComplaintId`) lack explicit constraint that duplicate/merged complaints are **read-only or terminal** | Medium |
| ER-05 | WorkflowInstance relationship is correctly **polymorphic via WRK** — no JPA FK from CMP to WRK tables | Info |

### 4.3 Recommendations — Relationships

| ID | Recommendation | Priority |
|----|----------------|----------|
| ER-R1 | Add optional `documentVersionId` on `ComplaintAttachment` OR document `pinnedVersion` policy in DOC — pin version at link time for evidence | Recommended |
| ER-R2 | When `isDuplicate = true` or `mergedIntoComplaintId` is set, enforce terminal/read-only status via validator | Mandatory |
| ER-R3 | Assignment service must update denormalized `Complaint` fields in **same transaction** as `ComplaintAssignment` insert | Mandatory |

---

## 5. Lifecycle Review

### 5.1 Review Checklist vs CMP-001

| Expected state | Present in CMP-001 | Notes |
|----------------|-------------------|-------|
| Draft | ✅ `DRAFT` | |
| Submitted | ✅ `SUBMITTED` | |
| Assigned | ✅ `ASSIGNED` | |
| Accepted | ✅ `ACCEPTED` | |
| In Progress | ✅ `IN_PROGRESS` | |
| Resolved | ✅ `RESOLVED` | |
| Citizen Verification | ✅ `VERIFIED` | Named `VERIFIED` — equivalent |
| Closed | ✅ `CLOSED` | |
| Archived | ❌ **Missing** | Product architecture mentions Archived phase |
| — | ✅ `WAITING_FOR_CITIZEN` | Valid addition — not in review checklist |
| — | ✅ `REOPENED` | Valid addition |
| — | ✅ `REJECTED` | Terminal at intake |
| — | ✅ `CANCELLED` | Terminal before assignment |

### 5.2 Transition Validation

| Transition | Valid? | Issue |
|------------|--------|-------|
| DRAFT → SUBMITTED | ✅ | |
| SUBMITTED → ASSIGNED | ✅ | |
| ASSIGNED → ACCEPTED | ✅ | |
| ACCEPTED → IN_PROGRESS | ✅ | |
| IN_PROGRESS → RESOLVED | ✅ | |
| RESOLVED → VERIFIED | ✅ | Citizen verification |
| VERIFIED → CLOSED | ✅ | |
| CLOSED → (archived) | ⚠️ | No `ARCHIVED` state |
| ASSIGNED → REJECTED (officer) | ⚠️ | **Ambiguous** — conflates assignment rejection with complaint terminal rejection |
| RESOLVED → REOPENED | ✅ | |
| REOPENED → IN_PROGRESS | ✅ | |

### 5.3 Findings — Lifecycle

| ID | Finding | Severity |
|----|---------|----------|
| LC-01 | **`ARCHIVED` state is missing** from `ComplaintStatus` enum — product-architecture.md defines Archived as universal phase after Closed | **High** |
| LC-02 | Officer assignment rejection mapped to complaint status `REJECTED` **conflicts** with admin intake `REJECTED` (terminal) — officer reject should trigger **reassignment**, not terminal complaint rejection | **High** |
| LC-03 | `ASSIGNED → ASSIGNED` (reassignment) documented but no distinct sub-state for "awaiting acceptance" after reassignment | Medium |
| LC-04 | No explicit **auto-close** rule timeout (RESOLVED → VERIFIED if citizen silent) — common in grievance systems | Medium |
| LC-05 | `CANCELLED` only from DRAFT/SUBMITTED — consider whether citizen may withdraw after assignment (jurisdiction-specific) | Low |
| LC-06 | `WAITING_FOR_CITIZEN` SLA pause documented — **response SLA** pause rules not specified | Low |

### 5.4 Recommendations — Lifecycle

| ID | Recommendation | Priority |
|----|----------------|----------|
| LC-R1 | Add `ARCHIVED` status OR `archivedAt` + `archived` boolean on `Complaint` with transition `CLOSED → ARCHIVED` (admin/system retention job) | **Mandatory** |
| LC-R2 | Split rejection semantics: officer assignment reject sets `ComplaintAssignment.assignmentStatus = REJECTED` and complaint stays `ASSIGNED` (or new status `PENDING_REASSIGNMENT`); reserve complaint-level `REJECTED` for **intake rejection only** | **Mandatory** |
| LC-R3 | Document auto-verify policy: e.g. `RESOLVED → VERIFIED` after 7 days citizen inaction (configurable in `CmpProperties`) — implementation deferred but **design now** | Recommended |
| LC-R4 | Add transition matrix row for `CLOSED → ARCHIVED` with actor = System/Admin | Mandatory (with LC-R1) |
| LC-R5 | Clarify whether `REOPENED` requires prior status `CLOSED` only (not `REJECTED`/`CANCELLED`) | Recommended |

### 5.5 Revised Lifecycle (Recommended)

```
DRAFT → SUBMITTED → ASSIGNED → ACCEPTED → IN_PROGRESS ↔ WAITING_FOR_CITIZEN
                              ↓ (reassign)
                         ASSIGNED / PENDING_REASSIGNMENT
                              ↓
IN_PROGRESS → RESOLVED → VERIFIED → CLOSED → ARCHIVED
                  ↓           ↓
              REOPENED    REOPENED (citizen rejects resolution)

SUBMITTED → REJECTED (intake — terminal)
DRAFT/SUBMITTED → CANCELLED (terminal)
CLOSED → REOPENED (admin only — extraordinary)
```

---

## 6. Workflow Ownership

### 6.1 Confirmation

| Question | Answer |
|----------|--------|
| Does `WorkflowInstance` own **process orchestration state**? | ✅ **Yes** — step progression, task assignment, WRK history |
| Does `Complaint` store **only current business status**? | ✅ **Yes** — `Complaint.status` is business lifecycle, not WRK step |
| Does CMP store workflow definitions? | ✅ **No** — correct |
| Does CMP store `workflow_instance_id` FK? | ✅ **No** — lookup via `referenceType` + `referenceId`; optional cache deferred |

### 6.2 Findings — Workflow

| ID | Finding | Severity |
|----|---------|----------|
| WF-01 | Dual-state model (CMP status + WRK instance status) is **architecturally correct** but requires **synchronization rules** to prevent drift | Medium |
| WF-02 | CMP-001 creates WRK instance on `SUBMITTED → ASSIGNED` but sequence diagram also shows create on submit — **timing ambiguity** | Medium |
| WF-03 | No documented rule for WRK `SUSPENDED` / `CANCELLED` instance vs CMP `WAITING_FOR_CITIZEN` / `CANCELLED` | Medium |
| WF-04 | WRK execution engine not implemented in Sprint 0 — CMP must not assume automatic task progression | Info |

### 6.3 Recommendations — Workflow

| ID | Recommendation | Priority |
|----|----------------|----------|
| WF-R1 | Document **source of truth matrix**: CMP status drives citizen/officer UX; WRK status drives task queue; orchestration service synchronizes on transitions | Mandatory |
| WF-R2 | Standardize: create `WorkflowInstance` on **`SUBMITTED`** (not ASSIGNED) so workflow owns routing to department; CMP transitions to `ASSIGNED` when first assignment completes | Recommended |
| WF-R3 | Map CMP `WAITING_FOR_CITIZEN` ↔ WRK `SUSPENDED` in orchestration contract | Recommended |
| WF-R4 | CMP must never infer business status solely from WRK task state — always update CMP status explicitly in lifecycle service | Mandatory |

---

## 7. Notification Events Review

### 7.1 Checklist vs CMP-001 Catalogue

| Review event | CMP-001 coverage | NTF template |
|--------------|------------------|--------------|
| Complaint Submitted | ✅ `ComplaintSubmittedEvent` | `CMP_COMPLAINT_SUBMITTED` |
| Complaint Assigned | ✅ `ComplaintAssignedEvent` | `CMP_COMPLAINT_ASSIGNED` |
| Complaint Escalated | ✅ `ComplaintEscalatedEvent` | `CMP_COMPLAINT_ESCALATED` |
| Complaint Resolved | ✅ `ComplaintResolvedEvent` | `CMP_COMPLAINT_RESOLVED` |
| Complaint Closed | ✅ `ComplaintClosedEvent` | `CMP_COMPLAINT_CLOSED` |
| Complaint Reopened | ✅ `ComplaintReopenedEvent` | ❌ Template missing |
| Feedback Received | ✅ `ComplaintRatedEvent` | ❌ Template missing |

### 7.2 Additional Events in CMP-001 (Not in Checklist)

| Event | Template status | Recommendation |
|-------|-----------------|----------------|
| `ComplaintCreatedEvent` | Missing | Draft save — optional notify |
| `ComplaintAcceptedEvent` | ✅ `CMP_COMPLAINT_ACCEPTED` | Keep |
| `ComplaintRejectedEvent` | Missing | **Add** `CMP_COMPLAINT_REJECTED` |
| `ComplaintVerifiedEvent` | Missing | **Add** `CMP_COMPLAINT_VERIFIED` |
| `ComplaintCommentAddedEvent` | Missing | Add when citizen-visible |
| `ComplaintSlaBreachedEvent` | ✅ `CMP_COMPLAINT_SLA_BREACH` | Keep |
| `ComplaintStatusChangedEvent` | Generic fallback | Use for unexpected transitions |

### 7.3 Findings — Notifications

| ID | Finding | Severity |
|----|---------|----------|
| NT-01 | Notification catalogue is **lifecycle-complete in domain events** but **incomplete in NTF template list** | Medium |
| NT-02 | No template for `WAITING_FOR_CITIZEN` — high citizen-impact gap | Medium |
| NT-03 | `ComplaintRatedEvent` / Feedback Received has no `CMP_COMPLAINT_FEEDBACK_RECEIVED` template | Low |
| NT-04 | Watcher-based notification (`ComplaintWatcher`) documented but not tied to event catalogue | Low |

### 7.4 Recommendations — Notifications

| ID | Recommendation | Priority |
|----|----------------|----------|
| NT-R1 | Extend NTF template seed list (CMP-013) with: `CMP_COMPLAINT_REOPENED`, `CMP_COMPLAINT_REJECTED`, `CMP_COMPLAINT_VERIFIED`, `CMP_COMPLAINT_WAITING_FOR_CITIZEN`, `CMP_COMPLAINT_FEEDBACK_RECEIVED` | Mandatory |
| NT-R2 | Map each lifecycle transition in §6.6 of CMP-001 to exactly one primary NTF template | Recommended |
| NT-R3 | Officer rejection (reassignment) should notify dept head — add `CMP_COMPLAINT_REASSIGNMENT_REQUIRED` | Recommended |

---

## 8. Audit Scope

### 8.1 Mandatory Audit Events — Complete Matrix

Every business action below **must** produce an AUD `AuditEvent` (and `AuditChange[]` when fields change).

| # | Business action | AUD `action` | AUD `eventType` | CMP domain event |
|---|-----------------|--------------|-----------------|------------------|
| 1 | Create draft complaint | `CREATE` | `ENTITY_CREATED` | `ComplaintCreatedEvent` |
| 2 | Update draft fields | `UPDATE` | `ENTITY_UPDATED` | — (field diff only) |
| 3 | Submit complaint | `TRANSITION` | `ENTITY_UPDATED` | `ComplaintSubmittedEvent` |
| 4 | Cancel draft/submitted | `TRANSITION` | `ENTITY_UPDATED` | — (add `ComplaintCancelledEvent`) |
| 5 | Intake reject (admin) | `TRANSITION` | `ENTITY_UPDATED` | `ComplaintRejectedEvent` |
| 6 | Assign / reassign | `ASSIGN` | `ENTITY_UPDATED` | `ComplaintAssignedEvent` |
| 7 | Accept assignment | `TRANSITION` | `ENTITY_UPDATED` | `ComplaintAcceptedEvent` |
| 8 | Reject assignment (officer) | `TRANSITION` | `ENTITY_UPDATED` | — (add `ComplaintAssignmentRejectedEvent`) |
| 9 | Start work | `TRANSITION` | `ENTITY_UPDATED` | `ComplaintStatusChangedEvent` |
| 10 | Request citizen clarification | `TRANSITION` | `ENTITY_UPDATED` | — (add `ComplaintClarificationRequestedEvent`) |
| 11 | Citizen clarification response | `TRANSITION` | `ENTITY_UPDATED` | — (add `ComplaintClarificationReceivedEvent`) |
| 12 | Record resolution | `TRANSITION` | `ENTITY_UPDATED` | `ComplaintResolvedEvent` |
| 13 | Citizen verify resolution | `TRANSITION` | `ENTITY_UPDATED` | `ComplaintVerifiedEvent` |
| 14 | Citizen reject resolution | `TRANSITION` | `ENTITY_UPDATED` | — (add `ComplaintResolutionRejectedEvent`) |
| 15 | Close complaint | `TRANSITION` | `ENTITY_UPDATED` | `ComplaintClosedEvent` |
| 16 | Reopen complaint | `TRANSITION` | `ENTITY_UPDATED` | `ComplaintReopenedEvent` |
| 17 | Archive complaint | `TRANSITION` | `ENTITY_UPDATED` | — (add `ComplaintArchivedEvent`) |
| 18 | Escalate | `TRANSITION` | `ENTITY_UPDATED` | `ComplaintEscalatedEvent` |
| 19 | SLA breach recorded | `TRANSITION` | `ENTITY_UPDATED` | `ComplaintSlaBreachedEvent` |
| 20 | Add comment | `CREATE` | `ENTITY_CREATED` | `ComplaintCommentAddedEvent` |
| 21 | Retract comment | `DELETE` | `ENTITY_DELETED` | — |
| 22 | Link attachment | `CREATE` | `ENTITY_CREATED` | `ComplaintAttachmentLinkedEvent` |
| 23 | Unlink attachment | `DELETE` | `ENTITY_DELETED` | — |
| 24 | Link duplicate | `UPDATE` | `ENTITY_UPDATED` | `ComplaintDuplicateLinkedEvent` |
| 25 | Merge complaints | `UPDATE` | `ENTITY_UPDATED` | `ComplaintMergedEvent` |
| 26 | Submit feedback/rating | `CREATE` | `ENTITY_CREATED` | `ComplaintRatedEvent` |
| 27 | Change priority | `UPDATE` | `ENTITY_UPDATED` | — |
| 28 | Change category | `UPDATE` | `ENTITY_UPDATED` | — |
| 29 | Add/remove watcher | `CREATE`/`DELETE` | `ENTITY_*` | — |
| 30 | Add/remove tag | `CREATE`/`DELETE` | `ENTITY_*` | — |
| 31 | View complaint (sensitive) | `READ` | `ENTITY_VIEWED` | — (optional v1.1) |
| 32 | Pause/resume SLA | `UPDATE` | `ENTITY_UPDATED` | — |

### 8.2 Findings — Audit

| ID | Finding | Severity |
|----|---------|----------|
| AU-01 | CMP-001 covers major transitions but ** lacks explicit cancel, archive, assignment-reject, clarification events** | Medium |
| AU-02 | Split between `ComplaintStatusHistory` (state machine) and AUD (field-level) is **correct** — no duplication concern | Info |
| AU-03 | `eventCode` pattern `{code}-{ACTION}` is suitable for compliance reporting | Info |

### 8.3 Recommendations — Audit

| ID | Recommendation | Priority |
|----|----------------|----------|
| AU-R1 | Add missing domain events listed in §8.1 before CMP-008 | Mandatory |
| AU-R2 | Append §8.1 matrix to CMP-001 README as authoritative audit catalogue | Mandatory |
| AU-R3 | Orchestration service owns AUD creation — never scatter audit calls across services | Mandatory |

---

## 9. Document Ownership & Attachment Strategy

### 9.1 Validation

```
Citizen upload → DOC DocumentService.create → CMP ComplaintAttachment.link(documentId)
                                                      ↓
                                              DOC DocumentVersion (versioning)
```

| Rule | CMP-001 | Valid? |
|------|---------|--------|
| No binary in CMP tables | ✅ | ✅ |
| DOC owns storage | ✅ | ✅ |
| CMP owns link metadata | ✅ | ✅ |
| Versioning via DOC | ✅ | ⚠️ Version pin not explicit |
| Max size / MIME whitelist | ✅ | ✅ |
| Citizen vs officer upload visibility | ✅ | ✅ |

### 9.2 Findings — Documents

| ID | Finding | Severity |
|----|---------|----------|
| DOC-01 | Attachment strategy is **architecturally correct** and aligned with product-architecture.md | Info |
| DOC-02 | Resolution evidence should **immutable pin** document version at resolve time | Medium |
| DOC-03 | Officer-uploaded resolution documents (PDF report) not distinguished from citizen evidence in attachment model — consider `attachmentRole` enum: `EVIDENCE`, `RESOLUTION`, `INTERNAL` | Medium |

### 9.3 Recommendations — Documents

| ID | Recommendation | Priority |
|----|----------------|----------|
| DOC-R1 | Add `attachmentRole` or extend `attachmentType` with `RESOLUTION_PROOF` | Recommended |
| DOC-R2 | On `ComplaintResolvedEvent`, optionally snapshot linked evidence version IDs in `ComplaintResolution` | Recommended |
| DOC-R3 | Keep upload size enforcement in DOC — CMP validator only checks count limit (10) | Mandatory |

---

## 10. GIS Support Review

### 10.1 Field Checklist vs `ComplaintLocation`

| GIS field | CMP-001 | Status |
|-----------|---------|--------|
| Latitude | ✅ `latitude` | OK |
| Longitude | ✅ `longitude` | OK |
| Ward | ✅ `wardKey` (MDM) | OK |
| Village | ✅ `villageKey` (MDM) | OK |
| ULB (Urban Local Body) | ❌ Missing | **Gap** |
| District | ✅ `districtKey` (MDM) | OK |
| State | ✅ `stateKey` (MDM) | OK |
| Pin code | ✅ `postalCode` | OK |
| GeoJSON future | ❌ Not reserved | **Gap** |

### 10.2 Findings — GIS

| ID | Finding | Severity |
|----|---------|----------|
| GIS-01 | Core location model is **adequate for v1** citizen address + GPS capture | Info |
| GIS-02 | **ULB missing** — required for Indian municipal grievance routing (ward → ULB → district) | Medium |
| GIS-03 | No extension column for future GeoJSON polygon (ward boundary, complaint pin area) | Low |
| GIS-04 | Ward/village as MDM keys is correct — GIS module can seed MDM later | Info |

### 10.3 Recommendations — GIS

| ID | Recommendation | Priority |
|----|----------------|----------|
| GIS-R1 | Add `ulbKey` (MDM type `COMPLAINT_ULB`) to `ComplaintLocation` in CMP-002 | **Mandatory** |
| GIS-R2 | Add nullable `geoJson` TEXT column (or JSONB) with comment "reserved — populated by GIS module" | Recommended |
| GIS-R3 | Add MDM hierarchy validation: ward belongs to ULB belongs to district (validator calls MDM metadata or future GIS service) | Future |
| GIS-R4 | Map/heat-map read models belong in **projection layer**, not new entities | Mandatory |

---

## 11. SLA Ownership Review

### 11.1 Expected Chain

```
Category → Priority → Resolution Time → Escalation Rule → Collector Alert
```

### 11.2 CMP-001 Model

| Layer | Owner | Implementation |
|-------|-------|----------------|
| Category | MDM | `categoryKey` on Complaint — **no SLA metadata** |
| Priority | CMP enum + MDM | `ComplaintPriority` validated against MDM |
| Resolution time | CMP `ComplaintSla` | Computed from **priority only** at submission |
| Escalation | CMP `ComplaintEscalation` + priority table | Documented in §7 |
| Collector alert | NTF + escalation level | Documented |

### 11.3 Findings — SLA

| ID | Finding | Severity |
|----|---------|----------|
| SLA-01 | SLA ownership in CMP aggregate is **correct** — platform scheduler deferred | Info |
| SLA-02 | **Category → default priority** mapping not documented (MDM metadata could hold default priority per category) | Medium |
| SLA-03 | Category-specific SLA override (e.g. "Road pothole" = HIGH regardless of citizen selection) not supported | Medium |
| SLA-04 | Business days vs calendar days for response SLA — not specified in computation rules | Low |
| SLA-05 | `ComplaintSla.priority` snapshots priority at creation — **priority change mid-lifecycle** does not recalculate SLA | Medium |

### 11.4 Recommendations — SLA

| ID | Recommendation | Priority |
|----|----------------|----------|
| SLA-R1 | Extend MDM `COMPLAINT_CATEGORY` metadata JSON: `{ "defaultPriority": "HIGH", "slaOverrideHours": null }` | Recommended |
| SLA-R2 | SLA computation order: **category override → priority default → CmpProperties fallback** | Recommended |
| SLA-R3 | Document SLA recalculation rule when priority changed before `IN_PROGRESS` | Recommended |
| SLA-R4 | Separate `responseDueAt` (business days) computation from `resolutionDueAt` (calendar days) in service spec | Recommended |
| SLA-R5 | Collector alert triggers remain in escalation service — do not duplicate in SLA entity | Mandatory |

---

## 12. AI Extension Points

AI is **explicitly out of scope** for CMP v1. Future integration points (no platform modification until proven cross-product):

| AI capability | Extension point | Integration pattern | Phase |
|---------------|-----------------|---------------------|-------|
| **Complaint classification** | On `DRAFT → SUBMITTED` | External service returns `categoryKey`, `subCategoryKey`, `confidence`; orchestration applies if citizen did not select | v2+ |
| **Duplicate detection** | On `SUBMITTED` | Service returns candidate IDs + `similarityScore`; `ComplaintDuplicateService.link(detectedBy=SYSTEM)` | v2+ |
| **Priority prediction** | On `SUBMITTED` | Suggests `priority`; citizen/officer confirms; SLA recalculated before assignment | v2+ |
| **OCR** | On attachment link | DOC upload hook → OCR service → append text to `ComplaintComment(SYSTEM)` or metadata field | v3+ |
| **Image recognition** | On IMAGE attachment | Async classification tag → `ComplaintTag` or MDM suggestion | v3+ |
| **Sentiment analysis** | On `description` / comments | Read-only analytics field on projection — **never** auto-change status | v3+ |
| **Translation** | On read API / NTF render | NTF template rendering layer or API response mapper | v2+ |
| **Chatbot** | Citizen portal | Separate channel adapter → creates `Complaint` via API with `source=API`, `channel=CHATBOT` | v2+ |
| **Predictive analytics** | Reporting | Read-only warehouse / projection — no write path to CMP aggregate | v4+ |

### 12.1 Findings — AI

| ID | Finding | Severity |
|----|---------|----------|
| AI-01 | `ComplaintDuplicate.similarityScore` and `detectedBy=SYSTEM` are **appropriate hooks** | Info |
| AI-02 | Do **not** add `aiClassificationJson` column until AI sprint — use projection or sidecar table in AI module | Recommended |
| AI-03 | AI must never bypass lifecycle validators or auto-transition without human confirmation (except duplicate **suggestion**) | Mandatory |

---

## 13. Multi-Tenancy Review

### 13.1 Ownership Hierarchy

| Level | CMP-001 support | Mechanism |
|-------|-----------------|-----------|
| **Department** | ✅ | `departmentId` FK → ORG |
| **Municipality / ULB** | ⚠️ Partial | Via `organizationId` (ORG) — ULB key missing on location |
| **District** | ✅ | `ComplaintLocation.districtKey` |
| **State** | ✅ | `ComplaintLocation.stateKey` + org hierarchy |
| **Future SaaS** | ⚠️ Partial | `organizationId` is tenant boundary candidate |

### 13.2 Findings — Multi-Tenancy

| ID | Finding | Severity |
|----|---------|----------|
| MT-01 | **`organizationId` on Complaint is the correct tenant/jurisdiction anchor** for single-DB multi-municipality deployment | Info |
| MT-02 | All list queries must filter by `organizationId` from security context — **not yet documented** in CMP-001 | Medium |
| MT-03 | Cross-organization complaint transfer (state escalation) needs explicit `transferToOrganizationId` or reassignment to state-level org — not modeled | Low |
| MT-04 | SaaS row-level isolation: rely on ORG + SEC permissions; **no separate `tenant_id`** needed if organization IS tenant | Info |

### 13.3 Recommendations — Multi-Tenancy

| ID | Recommendation | Priority |
|----|----------------|----------|
| MT-R1 | Document that `organizationId` is **mandatory** on submit and drives data isolation | Mandatory |
| MT-R2 | All repository list methods must accept `organizationId` filter from authenticated context | Mandatory |
| MT-R3 | Add `ulbKey` + link to ORG office for municipal routing (GIS-R1) | Mandatory |
| MT-R4 | Defer dedicated `tenant_id` column until true multi-tenant SaaS ADR — ORG organization suffices for v1 | Info |

---

## 14. Risk Register

| ID | Risk | Impact | Likelihood | Mitigation |
|----|------|--------|------------|------------|
| R-01 | Officer reject conflated with terminal REJECTED | High | High | Apply LC-R2 before CMP-007 |
| R-02 | CMP status / WRK status drift | High | Medium | Apply WF-R1, WF-R4; orchestration tests in CMP-009 |
| R-03 | Large aggregate performance (many comments/attachments) | Medium | Medium | Pagination on child queries; AR-R4 extraction path |
| R-04 | Missing ARCHIVED state blocks compliance retention policy | Medium | High | Apply LC-R1 before CMP-003 Flyway |
| R-05 | Evidence version not pinned at resolution | Medium | Medium | Apply DOC-R2 |
| R-06 | WRK engine not implemented — CMP assumes task completion | High | High | CMP-007 implements lifecycle without WRK dependency; integrate WRK in CMP-014 |
| R-07 | SLA breach without scheduler — flags never set | Medium | High | Manual breach recording in v1; document scheduler as platform future |
| R-08 | Duplicate/merge cross-aggregate inconsistency | Medium | Low | Two-phase orchestration + AR-R3 |
| R-09 | Multi-tenant data leak without org filter | High | Medium | MT-R2 + security integration tests at CMP-011 |
| R-10 | AI auto-classification bypasses citizen consent | Medium | Low | AI-03 policy in architecture |

---

## 15. Mandatory Amendments Before CMP-002

The following amendments were applied in **CMP-001.6** (`README.md`):

| # | Amendment | Status |
|---|-----------|--------|
| 1 | Add `ARCHIVED` status | ✅ Applied |
| 2 | Fix officer assignment rejection semantics | ✅ Applied |
| 3 | Add `ulbKey` to `ComplaintLocation` | ✅ Applied |
| 4 | Document workflow/CMP status ownership matrix | ✅ Applied |
| 5 | Complete NTF template catalogue | ✅ Applied |
| 6 | Append full audit action matrix | ✅ Applied |
| 7 | Document duplicate/merge as cross-aggregate orchestration | ✅ Applied |

---

## 16. Recommended Implementation Order

Adjusted sequence incorporating review findings:

| Order | ID | Deliverable | Dependency / note |
|-------|-----|-------------|-------------------|
| 1 | CMP-001.5 | Architecture review (this document) | ✅ Complete |
| 2 | CMP-001.6 | README errata — architecture frozen v1.0 | ✅ Complete |
| 3 | CMP-002 | Entities + enums (incl. `ARCHIVED`, `PENDING_REASSIGNMENT`, `ulbKey`) | |
| 4 | CMP-003 | Flyway `V2_0_0__complaint.sql` | Match CMP-002 exactly |
| 5 | CMP-004 | Repositories (org-scoped queries) | |
| 6 | CMP-005 | DTOs + MapStruct | |
| 7 | CMP-006 | Validators + exceptions (MDM, org, lifecycle rules) | |
| 8 | CMP-007 | `ComplaintService`, `ComplaintLifecycleService`, `ComplaintAssignmentService` | **Without WRK dependency** |
| 9 | CMP-008 | Domain events (full §8.1 catalogue) + `ComplaintOrchestrationService` skeleton | AUD/NTF stubs |
| 10 | CMP-009 | Resolution, SLA, Escalation, Comment, Attachment services | Apply DOC-R1 |
| 11 | CMP-010 | Duplicate, Merge, Feedback, Timeline projection | Cross-aggregate txs |
| 12 | CMP-011 | Unit + integration tests | Lifecycle + tenancy |
| 13 | CMP-012 | REST in `govos-api` | OpenAPI |
| 14 | CMP-013 | MDM + NTF seed data | Full template list |
| 15 | CMP-014 | WRK complaint workflow definition + orchestration wiring | Requires WRK engine or manual task simulation |
| 16 | CMP-015 | Angular citizen/officer features | |

**Critical path:** CMP-001.6 → CMP-002 → CMP-003 → CMP-007 (lifecycle correctness) → CMP-011 (tests) → CMP-012 (API).

---

## 17. Overall Verdict

| Question | Answer |
|----------|--------|
| Is the domain model enterprise-DDD compliant? | **Yes** — single aggregate root with well-defined child entities |
| Should `ComplaintCategory` be an aggregate? | **No** — MDM reference is correct |
| Is workflow ownership clear? | **Yes** — with synchronization rules to document |
| Is the lifecycle complete? | **Mostly** — add `ARCHIVED`; fix officer-reject semantics |
| Is the design ready for implementation? | **Yes, after CMP-001.6 errata** |

CMP-001 is a **strong foundation** for the first GovOS business product. The identified gaps are **refinements**, not structural flaws. No platform module changes are required.

---

## 18. Related Documents

| Document | Path |
|----------|------|
| CMP-001 Domain Blueprint | `cmp/README.md` |
| Product Architecture Layer | `govos-architecture/docs/backend/product-architecture.md` |
| Workflow Engine | `govos-domain/wrk/README.md` |
| Document Management | `govos-domain/doc/README.md` |
| Audit Module | `govos-domain/audit/README.md` |
| Entity Standards | `govos-architecture/docs/06-engineering/entity-standards.md` |

---

## 19. Sign-Off

| Role | Status | Date |
|------|--------|------|
| Chief Enterprise Architect | Review complete — **Approved with conditions** | 2026-07-18 |
| CMP-001.6 errata | **Applied** — see `README.md` | 2026-07-18 |
| Implementation gate | **Open** — CMP-002 may proceed | 2026-07-18 |
| Architecture freeze | **CMP v1.0 frozen** at CMP-001.6 | 2026-07-18 |

---

*End of CMP-001.5 Architecture Review*
