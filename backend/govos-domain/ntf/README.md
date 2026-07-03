# GovOS Notification (NTF)

Sprint 0 Day 7 — Notification bounded context for the GovOS platform.

## Overview

The NTF module models notification metadata, templates, channels, delivery tracking, user preferences, queue entries, and event subscriptions. External delivery is abstracted behind `NotificationProvider`; actual sending is deferred.

This module provides the **domain layer only** — no REST controllers, delivery engine, scheduling, or provider integrations in Sprint 0 Day 7.

## Module Location

| Artifact | Package | Responsibility |
|----------|---------|----------------|
| `govos-domain` | `com.govos.ntf` | NTF entities, services, repositories, DTOs, mappers, providers |
| `govos-infrastructure` | `db/migration` | Flyway `V1_5_0__notification.sql`, `V1_5_1__notification_refinements.sql` |

## Package Structure

```
com.govos.ntf
├── config          # Bounded-context configuration
├── controller      # Reserved for future REST layer (empty)
├── dto             # Request/response records
├── entity          # JPA entities and enums
├── event           # Domain events (records)
├── exception       # Domain exceptions
├── mapper          # MapStruct entity ↔ DTO mapping
├── mdm             # MDM type constants (e.g. DELIVERY_STATUS)
├── provider        # NotificationProvider abstraction and stubs
├── repository      # Spring Data JPA repositories
├── service         # Service interfaces and implementations
├── template        # Placeholder syntax and variable validation
└── validator       # Business validation rules
```

## Entity Model

All entities extend `AuditableEntity` (`govos-common`).

| Entity | Table | Description |
|--------|-------|-------------|
| `NotificationChannel` | `ntf_notification_channel` | Delivery channel configuration |
| `Notification` | `ntf_notification` | Notification message record |
| `NotificationTemplate` | `ntf_notification_template` | Reusable message templates |
| `NotificationDelivery` | `ntf_notification_delivery` | Delivery attempt tracking |
| `NotificationPreference` | `ntf_notification_preference` | Per-user channel opt-in/out |
| `NotificationQueue` | `ntf_notification_queue` | Queue metadata (no engine yet) |
| `NotificationSubscription` | `ntf_notification_subscription` | User subscription to event types |

### Cross-Domain References

- `NotificationPreference.user` → `com.govos.idm.entity.User`
- `NotificationSubscription.user` → `com.govos.idm.entity.User`

## Notification Model

| Field | Description |
|-------|-------------|
| `code` | Business identifier |
| `recipient` | Target address (email, phone, user ref) |
| `subject` / `body` | Message content |
| `channel` | Delivery channel |
| `status` | `PENDING`, `SCHEDULED`, `SENT`, `FAILED`, `CANCELLED` |
| `priority` | `LOW`, `NORMAL`, `HIGH`, `URGENT` |
| `scheduledAt` / `sentAt` | Scheduling metadata (no scheduler yet) |

## Design Decisions

### Template Variables

Templates support `{{variableName}}` placeholders in `subjectTemplate` and `bodyTemplate`:

```
Hello {{firstName}}

Your complaint {{complaintNumber}} has been assigned to {{officerName}}.
```

Declared variable names are stored in `templateVariables` (JSON array). `NotificationTemplateValidator` ensures every placeholder in the template text is declared. Rendering is deferred to a later sprint.

### Delivery Status

`DeliveryStatus` enum (future MDM type `DELIVERY_STATUS`):

- `PENDING`, `QUEUED`, `SENT`, `DELIVERED`, `FAILED`, `CANCELLED`

Not a boolean — supports full delivery lifecycle tracking.

### Retry Policy

Retry fields are modeled on queue and delivery records; no retry engine in Sprint 0:

| Entity | Fields |
|--------|--------|
| `NotificationQueue` | `retryCount`, `maxRetry`, `nextRetryAt` |
| `NotificationDelivery` | `retryCount`, `maxRetry`, `nextRetryAt`, `lastAttempt` |

Default `maxRetry` is **3**. `nextRetryAt` is populated when a retry engine is added.

## Provider Abstraction

| Provider | Status |
|----------|--------|
| `NotificationProvider` | Interface (`send`) |
| `EmailProvider` | Stub |
| `SmsProvider` | Stub |
| `WhatsappProvider` | Stub |
| `PushProvider` | Stub |
| `InAppProvider` | Stub |
| `WebhookProvider` | Stub |

All provider implementations throw `UnsupportedOperationException`.

## Database

| Migration | Version | Purpose |
|-----------|---------|---------|
| `V1_5_0__notification.sql` | 1.5.0 | Core NTF tables |
| `V1_5_1__notification_refinements.sql` | 1.5.1 | Template variables, delivery status, retry fields |

Partial unique indexes support soft-delete for codes, user-channel preferences, and subscriptions.

## Service API (Internal)

| Service | Key Operations |
|---------|----------------|
| `NotificationChannelService` | CRUD, list by provider |
| `NotificationService` | CRUD, list by channel/status/recipient |
| `NotificationTemplateService` | CRUD, list by channel |
| `NotificationDeliveryService` | CRUD, list by notification/status |
| `NotificationPreferenceService` | CRUD, list by user |
| `NotificationQueueService` | CRUD, list by notification |
| `NotificationSubscriptionService` | CRUD, list by user/event type |

### Business Rules

- Duplicate codes rejected (channel, notification, template)
- Template placeholders must be declared in `templateVariables`
- Unique user-channel preference per active record
- Unique user-event-channel subscription per active record
- Optimistic locking via `version` on update operations
- Soft-delete sets `deleted = true`, `active = false`

## Domain Events

| Event | Purpose |
|-------|---------|
| `NotificationCreatedEvent` | Notification record created |
| `NotificationScheduledEvent` | Notification scheduled |
| `NotificationDeliveryRecordedEvent` | Delivery attempt recorded |

Events are plain records; Spring event publishing is deferred.

## Out of Scope (Sprint 0 Day 7)

- REST controllers and HTTP APIs
- Email, SMS, WhatsApp, Push delivery
- Delivery engine and retry logic
- Scheduling (Quartz or similar)
- Message queuing (Kafka or similar)
- Security, JWT, authentication
- Sample/seed data

## Platform Foundation Status

| Module | Status |
|--------|--------|
| Infrastructure | ✅ |
| MDM | ✅ |
| Identity (IDM) | ✅ |
| Organization (ORG) | ✅ |
| Document Management (DOC) | ✅ |
| Notification (NTF) | ✅ |

## Next Steps

- Provider SDK integrations (email, SMS, etc.)
- Delivery engine with retry and scheduling
- NTF REST controllers in `govos-api`
- `govos-security` module
- Spring application event publishing for NTF domain events
