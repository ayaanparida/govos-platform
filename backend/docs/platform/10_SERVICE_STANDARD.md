# GPS-001 — 10 Service Standard

---

## 1. Domain Service Pattern

```java
public interface SearchIndexService {
    SearchIndexDto create(SearchIndexCreateRequest request);
    SearchIndexDto getById(UUID id);
    void softDelete(UUID id);
}
```

```java
@Service
@Transactional
public class SearchIndexServiceImpl implements SearchIndexService {
    // constructor injection only
}
```

---

## 2. Responsibilities

Domain services **own**:

- Business rule enforcement
- Aggregate lifecycle (create, update, soft delete, restore)
- Coordination of repository + validator + mapper
- Domain exception translation

Domain services **do not**:

- Handle HTTP concerns
- Know about JWT or permissions
- Call another context's repository directly

---

## 3. Constructor Injection

- All dependencies `private final`
- Single constructor (no `@Autowired` on fields)
- Inject interfaces, not implementations where possible

---

## 4. Transaction Boundaries

| Operation | Transaction |
|-----------|-------------|
| Write (create, update, delete) | `@Transactional` |
| Read (get, list) | `@Transactional(readOnly = true)` at class or method level |
| Cross-resource write | Single transaction in application service or orchestrating domain service |

Default: `@Transactional` on service impl class; override read methods with `readOnly = true`.

---

## 5. Validation Flow

```
Request DTO → Validator.validate() → Business logic → Entity → Repository → Response DTO
```

Validators throw typed validation exceptions before persistence.

---

## 6. Mapping

- Entity ↔ DTO via MapStruct mappers
- Never expose entities outside domain layer
- Application layer receives/returns DTOs only

---

## 7. Service Granularity

- One service interface per aggregate or cohesive aggregate group
- Split when class exceeds ~300 lines or mixed responsibilities

---

## 8. Prohibited

- Static utility services with mutable state
- `@Autowired` field injection
- Business logic in repositories or mappers
- Direct HTTP client calls without adapter abstraction
