# GPS-001 — 14 Validation Standard

---

## 1. Two-Layer Validation

| Layer | Mechanism | Scope |
|-------|-----------|-------|
| **Structural** | Jakarta Validation (`@NotNull`, `@Size`) | DTO annotations |
| **Business** | `{Aggregate}Validator` classes | Domain rules |

Both layers required for write operations.

---

## 2. Jakarta Validation

On request DTOs and controller parameters:

```java
@PostMapping("/indexes")
public ApiResponse<SearchIndexDto> create(@Valid @RequestBody SearchIndexCreateRequest request)
```

Global handler: `GlobalExceptionHandler` → `400 Bad Request` with field errors.

---

## 3. Business Validators

```java
@Component
public class SearchIndexValidator {
    public void validateCreate(SearchIndexCreateRequest request) {
        // uniqueness, state rules, cross-field validation
    }
}
```

Throw typed exceptions: `{Context}ValidationException` or specific variants.

---

## 4. Validation Response Format

```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "fields": [
      { "field": "code", "message": "must not be blank" }
    ]
  },
  "requestId": "..."
}
```

---

## 5. Rules

| Rule | Detail |
|------|--------|
| Fail fast | Validate before persistence |
| Typed exceptions | No generic `IllegalArgumentException` in domain |
| No validation in controllers | Delegate to validator or service |
| Organization scoping | Validate tenant context on multi-tenant operations |

---

## 6. Query Validation

- Pagination bounds enforced (max page size from config)
- Guard against deep pagination (SRH pattern: max offset)
- Sanitize sort field names (allowlist)

---

## 7. Prohibited

- Silent coercion of invalid input
- Validation only in database constraints (app layer required too)
- Trusting client-side validation alone
