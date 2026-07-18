# GPS-001 — 12 DTO Standard

---

## 1. Types

| DTO | Purpose | Example |
|-----|---------|---------|
| `{Aggregate}Dto` | Response / read model | `SearchIndexDto` |
| `{Aggregate}CreateRequest` | Create payload | `SearchIndexCreateRequest` |
| `{Aggregate}UpdateRequest` | Update payload | `SearchIndexUpdateRequest` |
| Query request/response | Read path | `SearchRequest`, `SearchResponse` |
| Admin DTOs | Administration | `SearchDashboardDto` |

---

## 2. Location

- Domain DTOs: `com.govos.{context}.dto`
- Query DTOs: `com.govos.{context}.query` (when read-model separation applies)
- API wrapper: `com.govos.api.common.response.ApiResponse<T>`

---

## 3. Records vs Classes

| Use records | Use classes |
|-------------|-------------|
| Immutable response DTOs | JPA entities (never records) |
| Event records | DTOs requiring Jackson custom deserialization edge cases |
| Simple value carriers | Buildable request DTOs with validation groups |

Prefer **records** for new immutable DTOs (Java 21).

---

## 4. Validation Annotations

Request DTOs use Jakarta Validation:

```java
public record SearchIndexCreateRequest(
    @NotBlank @Size(max = 100) String code,
    @NotBlank @Size(max = 255) String name,
    @NotNull UUID organizationId
) {}
```

Controller: `@Valid @RequestBody`

---

## 5. Rules

| Rule | Detail |
|------|--------|
| No entity references | DTOs contain IDs, not entity objects |
| No business logic | Pure data carriers |
| Serializable boundary | JSON-friendly types only |
| Nullability | Document optional fields; use `Optional` sparingly in DTOs |

---

## 6. API Response Wrapper

```json
{
  "success": true,
  "data": { ... },
  "message": null,
  "requestId": "uuid"
}
```

Use `ApiResponse<T>` consistently.

---

## 7. Prohibited

- Exposing JPA entities in REST responses
- DTOs with 20+ fields without decomposition
- Circular DTO references
