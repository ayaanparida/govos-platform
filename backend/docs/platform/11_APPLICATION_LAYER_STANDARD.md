# GPS-001 — 11 Application Layer Standard

---

## 1. Purpose

Application services in `govos-api` provide the **orchestration boundary** between REST controllers and domain services.

```
Controller → ApplicationService → DomainService(s)
```

---

## 2. Naming & Location

```
com.govos.api.{context}.application.{Context}ApplicationService
com.govos.api.{context}.application.{Context}ApplicationServiceImpl
```

Examples: `SearchApplicationService`, `ComplaintApplicationService`

---

## 3. Responsibilities

| Do | Don't |
|----|-------|
| Delegate to domain services | Implement business rules |
| Coordinate multi-service operations | Access repositories directly |
| Cross-context orchestration (via ACL) | Expose domain entities |
| Enforce API-level transaction boundaries | Handle HTTP status codes |

---

## 4. Cross-Context Integration

Products integrate with platform via application services:

```java
@Service
public class ComplaintSearchIntegrationImpl {
    private final SearchApplicationService searchApplicationService;
    // translate CMP → SRH DTOs
}
```

**Never** inject `SearchIndexService` (domain) from product code.

---

## 5. Transaction Strategy

- Application service methods match use-case boundaries
- `@Transactional` on write operations that span multiple domain calls
- Read operations: `@Transactional(readOnly = true)` on application service class

---

## 6. Interface Segregation

- One application service interface per bounded context
- Methods map to use cases / controller operations
- Keep interface stable — additive changes only post v1.0

---

## 7. Testing

- Mock domain services in application service tests
- Verify orchestration and delegation, not business rules (tested in domain)

---

## 8. Prohibited

- Application services in `govos-domain`
- Controllers calling domain services directly (bypass application layer)
- Application service depending on another context's domain service
