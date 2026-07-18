# GPS-001 — 23 Scheduler Standard

---

## 1. Ownership

**Each bounded context owns its own scheduling.** Platform modules schedule their own operational jobs. Products **never** schedule platform operations (e.g. products must not cron-reindex search).

Reference: SRH-019 — `com.govos.srh.scheduler`

---

## 2. Technology

- **Spring `@Scheduled` only** — no external job schedulers (Quartz, OpenSearch scheduler) unless ADR approved
- Enable with `@EnableScheduling` in module configuration
- Conditional: `@ConditionalOnProperty(prefix = "govos.{ctx}.scheduler", name = "enabled")`

---

## 3. Structure

```
com.govos.{context}.scheduler/
├── {Context}SchedulerService          Interface
├── {Context}SchedulerServiceImpl      Job orchestration
├── {Context}ScheduledTasks            @Scheduled cron methods
├── {Context}SchedulerProperties       Cron, retry config
├── {Context}SchedulerHistoryStore     Execution history
└── {Context}ScheduledJobNames         Constants
```

---

## 4. Job Naming

kebab-case constants:

```java
public static final String DAILY_FULL_REINDEX = "daily-full-reindex";
```

---

## 5. Retry Policy

| Setting | Typical default |
|---------|-----------------|
| `max-retries` | 3 |
| `initial-backoff-ms` | 1000 |
| `max-backoff-ms` | 30000 |
| `backoff-multiplier` | 2.0 |

Exponential backoff via dedicated retry executor — not raw retry loops in job methods.

---

## 6. Execution History

Record per execution:
- Job name
- Start/end time, duration
- Status (COMPLETED, FAILED, SKIPPED, RETRYING)
- Error message (sanitized)
- Documents processed count

In-memory acceptable for v1.0; persistent history requires ADR.

---

## 7. Metrics

| Metric | Purpose |
|--------|---------|
| `scheduler.executions` | Job run counter |
| `scheduler.duration` | Job duration timer |
| `scheduler.failures` | Failed jobs |
| `scheduler.retries` | Retry counter |
| `scheduler.skipped` | Skipped jobs |

Tag with `job` name.

---

## 8. Admin Triggers

Manual job triggers via REST:
- `POST /admin/scheduler/{action}`
- Permission: `{CTX}_ADMIN`
- Return `202 Accepted` with job record

---

## 9. Logging

Log: job name, duration, status, counts only.

Never log: document content, embeddings, credentials.

---

## 10. Cron Configuration

Externalize all cron expressions:

```yaml
govos.search.scheduler:
  reindex-cron: "0 0 2 * * *"
```

Support environment overrides for deployment timezone planning.

---

## 11. Prohibited

- Products scheduling SRH/platform jobs
- `@Scheduled` in `govos-api` controllers
- Silent failure swallowing in scheduled methods
- Hardcoded cron in Java without property override
