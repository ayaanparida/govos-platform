# GPS-001 — 16 Event Standard

---

## 1. Purpose

Domain events represent **significant state changes** within a bounded context. GovOS v1.0 uses **immutable event records** as contracts (SRH-008 pattern).

---

## 2. Event Record Pattern

```java
public record ComplaintCreatedEvent(
    UUID eventId,
    UUID complaintId,
    UUID organizationId,
    Instant occurredAt
) {}
```

- Location: `com.govos.{context}.event`
- Immutable Java records
- Include `eventId`, aggregate ID, `organizationId`, timestamp

---

## 3. Naming

| Pattern | Example |
|---------|---------|
| `{Aggregate}{PastTense}Event` | `ComplaintCreatedEvent` |
| `{Aggregate}{PastTense}Event` | `SearchDocumentIndexedEvent` |

Use past tense verbs.

---

## 4. Publishing (Future)

v1.0 baseline: **contracts only**. Publishing via:

- Spring ApplicationEvent (in-process)
- Message broker (future ADR)

Modules must not assume events are consumed until wired.

---

## 5. Rules

| Rule | Detail |
|------|--------|
| **Infrastructure events ≠ domain events** | SchedulerStarted is observability, not domain |
| **No PII in events** | IDs and metadata only |
| **Version events** | Add new event type; don't mutate existing |
| **Cross-context** | Consumers use ACL; don't import foreign events into domain logic |

---

## 6. Observation vs Domain Events

| Type | Package | Example |
|------|---------|---------|
| Domain event | `{context}.event` | `ComplaintStatusChangedEvent` |
| Observation event | `{context}.observability` | `SearchStarted` (SRH-020) |

Keep separate.

---

## 7. Prohibited

- Mutable event classes
- Events with entity references
- Cross-context event handlers without ACL
