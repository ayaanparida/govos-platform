# GPS-001 — 09 Repository Standard

---

## 1. Interface Pattern

```java
public interface SearchIndexRepository extends JpaRepository<SearchIndex, UUID> {
    Optional<SearchIndex> findByIdAndDeletedFalse(UUID id);
    Page<SearchIndex> findAllByDeletedFalse(Pageable pageable);
}
```

- Extend `JpaRepository<Entity, UUID>`
- Interface in `{context}.repository` package
- Spring Data generates implementation

---

## 2. Query Methods

| Pattern | Usage |
|---------|-------|
| `findByIdAndDeletedFalse` | Standard get |
| `findAllByDeletedFalse` | List active |
| `findBy{Field}AndDeletedFalse` | Filtered queries |
| `@Query` JPQL | Complex queries only when method naming insufficient |

---

## 3. Rules

| Rule | Detail |
|------|--------|
| **Soft delete filter** | All read queries include `DeletedFalse` unless admin |
| **No business logic** | Repositories return data only |
| **No cross-context joins** | Query one aggregate/table group |
| **Pagination** | Use `Pageable` for lists |
| **Custom impl** | `{Repo}Custom` + `{Repo}CustomImpl` if needed |

---

## 4. Transaction Boundaries

- Repositories participate in caller's transaction
- Do not annotate repositories with `@Transactional` except read-only hints on query methods if needed

---

## 5. Naming

- `{Aggregate}Repository` — one primary repository per aggregate root
- Supporting entities may share aggregate repository or have dedicated repo if justified

---

## 6. Prohibited

- Repository calls from controllers
- Repository in `govos-api`
- Native SQL without documented performance justification
- Returning entities to another bounded context's service
