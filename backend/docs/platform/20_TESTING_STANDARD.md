# GPS-001 — 20 Testing Standard

---

## 1. Test Stack

| Tool | Purpose |
|------|---------|
| JUnit 5 | Test framework |
| Mockito | Mocking dependencies |
| AssertJ | Fluent assertions |
| Spring MockMvc | Controller tests |
| Spring Boot Test | Integration tests (selective) |

---

## 2. Test Location

```
govos-domain/src/test/java/com/govos/{context}/
govos-api/src/test/java/com/govos/api/{context}/
```

Mirror main package structure.

---

## 3. Test Naming

```java
class SearchIndexServiceImplTest {
    @Test
    void shouldCreateIndexWhenRequestValid() { }
    
    @Test
    void shouldThrowNotFoundWhenIdMissing() { }
}
```

Pattern: `should{Expected}When{Condition}`

---

## 4. Unit Test Rules

| Rule | Detail |
|------|--------|
| **Mock dependencies** | Repository, external services |
| **No Spring context** | Pure unit tests unless `@ExtendWith(MockitoExtension.class)` |
| **One assertion focus** | Test one behavior per method |
| **Fixtures** | Shared test data in `{Context}TestFixtures` class |

---

## 5. Controller Tests

```java
@ExtendWith(MockitoExtension.class)
class SearchControllerTest {
    @Mock SearchApplicationService service;
    MockMvc mockMvc; // standaloneSetup
}
```

- Mock application service, not domain
- Verify HTTP status, JSON path, security annotations (reflection tests)

---

## 6. Integration Tests

- Name: `{Feature}IntegrationTest`
- Use `@SpringBootTest` or slice tests sparingly (slow)
- Product integration: mock platform services or test containers

---

## 7. JaCoCo Coverage Gates

Configured per module in `pom.xml`:

| Layer | Minimum (typical) |
|-------|-------------------|
| Service impl | 90% line |
| Validator | 90% line |
| Mapper | 90% line |
| Overall module | 85% line |

Run: `mvn verify` (not just `test`)

---

## 8. Fixture Strategy

```java
public final class SrhTestFixtures {
    public static final UUID INDEX_ID = UUID.fromString("...");
    public static SearchIndex indexEntity() { ... }
}
```

- Centralize UUIDs and sample entities
- Immutable fixture builders
- No shared mutable state between tests

---

## 9. What to Test

| Layer | Focus |
|-------|-------|
| Validator | All business rules, edge cases |
| Service | Lifecycle, exceptions, orchestration |
| Mapper | Complex field mappings |
| Controller | HTTP contract, validation errors |
| Security | Permission annotations |

---

## 10. Prohibited

- Tests depending on execution order
- `@Disabled` without ticket reference
- Testing framework code (Spring internals)
- Committing test secrets or real API keys

---

## 11. CI Requirement

```
mvn -pl govos-api -am test
→ BUILD SUCCESS
```

All modules must pass before merge.
