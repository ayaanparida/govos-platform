# GPS-001 — 22 Logging Standard

---

## 1. Framework

- **SLF4J** facade only — no direct Log4j/Logback calls in domain code
- Logger declaration: `private static final Logger LOGGER = LoggerFactory.getLogger(ClassName.class);`

---

## 2. Structured Format

Prefer key=value pairs for machine parsing:

```
LOGGER.info("operation={} status={} durationMs={} requestId={}",
    operation, status, durationMs, requestId);
```

Dedicated loggers for cross-cutting concerns:
- `com.govos.{context}.operation`
- `com.govos.{context}.trace`

---

## 3. MDC Fields

| Field | Purpose |
|-------|---------|
| `requestId` | HTTP correlation |
| `traceId` | Distributed trace |
| `spanId` | Span correlation |
| `organizationId` | Tenant (when available) |
| `userId` | Actor (when available) |

Set in filters; cleared in `finally` block.

---

## 4. Log Levels

| Level | Usage |
|-------|-------|
| ERROR | Failures requiring attention; include exception |
| WARN | Degraded mode, retries, misconfiguration |
| INFO | Operation completion, startup, job execution |
| DEBUG | Span detail, development diagnostics |
| TRACE | Not used in production code |

---

## 5. Never Log

| Category | Examples |
|----------|----------|
| Secrets | API keys, passwords, JWT tokens |
| PII | National IDs, phone numbers, addresses |
| Search content | Query text, document JSON |
| Embeddings | Vector arrays |
| Full request/response bodies | May contain sensitive data |

---

## 6. Safe Fields

- Operation name
- Duration (ms)
- Status (SUCCESS/ERROR)
- UUID identifiers
- Counts (document count, batch size)
- Provider name (not credentials)
- Job name

---

## 7. Exception Logging

```java
LOGGER.error("operation={} status=ERROR requestId={}", operation, requestId, exception);
```

Include stack trace at ERROR for unexpected failures. Client receives sanitized message.

---

## 8. Security Logging

`SecurityRequestLoggingFilter` logs:
- URI, username, status, durationMs, requestId
- Never log Authorization header value

---

## 9. Production Configuration

```yaml
logging:
  level:
    com.govos: INFO
    org.springframework: WARN
```

Centralize logs via ELK/Loki/CloudWatch in production.

---

## 10. Prohibited

- `System.out.println` in production code
- Logging inside tight loops at INFO
- String concatenation with sensitive data (`"key=" + apiKey`)
