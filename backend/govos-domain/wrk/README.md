# GovOS Workflow Engine (WRK)

Sprint 0 Day 8 — Workflow bounded context for the GovOS Enterprise Government Platform.

## Overview

The WRK module provides a **generic, configuration-driven workflow foundation**. Business processes (complaints, leave applications, RTI, file movement, certificates, licenses) are not hardcoded — each binds to a `WorkflowInstance` that references a published `WorkflowDefinition` through `WorkflowVersion`, `WorkflowStep`, and `WorkflowTransition`.

This module provides the **domain layer only** — no REST controllers, execution engine, scheduler, or state machine in Sprint 0 Day 8.

## Design Goal

```
Complaint (future)          Birth Certificate (future)
        ↓                              ↓
Workflow Instance              Workflow Instance
        ↓                              ↓
        └──────── Same Engine ─────────┘
                      ↓
            Workflow Definition
                      ↓
              Workflow Steps
```

Complaint becomes configuration, not custom code.

## Module Location

| Artifact | Package | Responsibility |
|----------|---------|----------------|
| `govos-domain` | `com.govos.wrk` | WRK entities, services, repositories, DTOs, mappers, validators |
| `govos-infrastructure` | `db/migration` | Flyway `V1_6_0__workflow.sql` |

## Package Structure

```
com.govos.wrk
├── config          # Bounded-context configuration
├── controller      # Reserved for future REST layer (empty)
├── dto             # Request/response records
├── entity          # JPA entities and enums
├── event           # Domain events (records)
├── exception       # Domain exceptions
├── mapper          # MapStruct entity ↔ DTO mapping
├── repository      # Spring Data JPA repositories
├── service         # Service interfaces and implementations
└── validator       # Business validation rules
```

## Entity Model

All entities extend `AuditableEntity` (`govos-common`).

| Entity | Table | Description |
|--------|-------|-------------|
| `WorkflowDefinition` | `wrk_workflow_definition` | Reusable workflow blueprint (code, name, description) |
| `WorkflowVersion` | `wrk_workflow_version` | Versioned snapshot of a definition (`version_number`, `published`) |
| `WorkflowStep` | `wrk_workflow_step` | Step within a version (name, type, sequence, SLA) |
| `WorkflowTransition` | `wrk_workflow_transition` | Directed edge between steps with optional condition |
| `WorkflowInstance` | `wrk_workflow_instance` | Running workflow bound to a business entity |
| `WorkflowTask` | `wrk_workflow_task` | Assignable work item for a step within an instance |
| `WorkflowHistory` | `wrk_workflow_history` | Append-only audit trail of workflow actions |
| `WorkflowAssignment` | `wrk_workflow_assignment` | User assignment record for a task |

### Enums

| Enum | Values |
|------|--------|
| `WorkflowStepType` | `START`, `END`, `USER_TASK`, `SYSTEM_TASK`, `GATEWAY` |
| `WorkflowInstanceStatus` | `PENDING`, `RUNNING`, `COMPLETED`, `CANCELLED`, `SUSPENDED` |
| `WorkflowTaskStatus` | `PENDING`, `ASSIGNED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`, `OVERDUE` |
| `WorkflowHistoryAction` | `INSTANCE_STARTED`, `INSTANCE_COMPLETED`, `INSTANCE_CANCELLED`, `TASK_CREATED`, `TASK_ASSIGNED`, `TASK_COMPLETED`, `STEP_ENTERED`, `STEP_EXITED`, `TRANSITION` |

### Cross-Domain References

- `WorkflowTask.assignedTo` → `com.govos.idm.entity.User`
- `WorkflowTask.assignedRole` → `com.govos.idm.entity.Role`
- `WorkflowHistory.performedBy` → `com.govos.idm.entity.User`
- `WorkflowAssignment.user` → `com.govos.idm.entity.User`

### Polymorphic Business Reference

`WorkflowInstance` uses `referenceType` + `referenceId` to link to any business entity without coupling WRK to a specific domain:

| Field | Example |
|-------|---------|
| `referenceType` | `COMPLAINT`, `LEAVE_APPLICATION`, `RTI` |
| `referenceId` | UUID of the business record |

## Database

| Migration | Version | Purpose |
|-----------|---------|---------|
| `V1_6_0__workflow.sql` | 1.6.0 | All WRK tables, FKs, partial unique indexes |

Key indexes:

- Unique definition `code` (active, non-deleted)
- Unique `(workflow_definition_id, version_number)` per version
- Unique `(workflow_version_id, sequence_number)` per step
- Unique `(from_step_id, to_step_id)` per transition
- Composite index on `(reference_type, reference_id)` for instance lookup
- Status indexes on instances and tasks

## Service API (Internal)

| Service | Key Operations |
|---------|----------------|
| `WorkflowDefinitionService` | CRUD, get by code, list all |
| `WorkflowVersionService` | CRUD, list by definition, get published version |
| `WorkflowStepService` | CRUD, list by workflow version |
| `WorkflowTransitionService` | CRUD, list by from/to step |
| `WorkflowInstanceService` | CRUD, list by reference/status/version |
| `WorkflowTaskService` | CRUD, list by instance/assignee/status |
| `WorkflowHistoryService` | Create, get, list by instance (append-only) |
| `WorkflowAssignmentService` | CRUD, list by task/user |

### Business Rules

- Duplicate definition codes rejected
- Duplicate version numbers per definition rejected
- Duplicate step sequence numbers per version rejected
- Transitions must connect steps within the **same** workflow version; `fromStep` ≠ `toStep`
- Duplicate task-user assignments rejected
- `WorkflowHistory` is append-only — no update or soft-delete
- Optimistic locking via `version` on update operations
- Soft-delete sets `deleted = true`, `active = false`
- Instance `code` auto-generated when not provided: `WFI-{referenceType}-{referenceIdPrefix}`

## Domain Events

| Event | Purpose |
|-------|---------|
| `WorkflowDefinitionPublishedEvent` | Version marked published |
| `WorkflowInstanceStartedEvent` | Instance created/started |
| `WorkflowTaskAssignedEvent` | Task assigned to user or role |
| `WorkflowTaskCompletedEvent` | Task completed |

Events are plain records; Spring event publishing is deferred.

## Out of Scope (Sprint 0 Day 8)

- REST controllers and HTTP APIs
- Workflow execution engine and state machine
- BPMN, Camunda, Activiti, Flowable
- Scheduler and SLA enforcement
- Complaint or any business-specific workflow
- Condition expression evaluation
- Security, JWT, authentication
- Sample/seed data

## Platform Foundation Status (Roadmap v2.0)

| Module | Status |
|--------|--------|
| Infrastructure | ✅ |
| MDM | ✅ |
| Identity (IDM) | ✅ |
| Organization (ORG) | ✅ |
| Document Management (DOC) | ✅ |
| Notification (NTF) | ✅ |
| **Workflow (WRK)** | ✅ |
| Search (SRH) | 🔜 Day 9 |
| Audit (AUD) | 🔜 Day 10 |
| Security | 🔜 Day 11 |

## Next Steps

- Workflow execution engine (step progression, transition evaluation)
- SLA scheduler and overdue task detection
- WRK REST controllers in `govos-api`
- Integration with Search (SRH) and Audit (AUD)
- Complaint module as first workflow consumer
