# GPS-001 — 15 Exception Standard

---

## 1. Typed Domain Exceptions

Each bounded context defines exceptions in `com.govos.{context}.exception`:

| Pattern | HTTP (via handler) | Example |
|---------|-------------------|---------|
| `{Aggregate}NotFoundException` | 404 | `SearchIndexNotFoundException` |
| `{Context}ValidationException` | 400 | `SearchValidationException` |
| `{Context}ConflictException` | 409 | `SearchConflictException` |
| `{Operation}Exception` | 500/503 | `SemanticSearchException` |

---

## 2. Base Pattern

```java
public class SearchIndexNotFoundException extends RuntimeException {
    public SearchIndexNotFoundException(UUID id) {
        super("Search index not found: " + id);
    }
}
```

- Extend `RuntimeException` (unchecked)
- Include identifying information in message (ID, code — not PII)
- No error codes in exception class — handler maps to API error

---

## 3. Global Exception Handler

`com.govos.api.common.advice.GlobalExceptionHandler`:

- Maps domain exceptions to HTTP status
- Returns `ErrorResponse` with code, message, requestId
- Logs server errors at ERROR; client errors at WARN

---

## 4. Rules

| Rule | Detail |
|------|--------|
| **No swallowed exceptions** | Log and rethrow or translate |
| **No stack traces to client** | Internal detail in logs only |
| **Preserve cause** | `super(message, cause)` when wrapping |
| **Context-specific** | Don't reuse another context's exceptions |

---

## 5. Engine / Provider Exceptions

Wrap external failures in domain exceptions:

```java
throw new SemanticSearchException("Embedding provider unavailable", cause);
```

Never expose vendor error bodies to API clients.

---

## 6. Prohibited

- Generic `Exception` catches in domain logic
- Returning error messages with SQL, stack traces, or secrets
- Checked exceptions in service interfaces
