# GPS-001 — 13 Mapper Standard

---

## 1. Technology

**MapStruct 1.6.x** — compile-time mapping, no reflection.

---

## 2. Interface Pattern

```java
@Mapper(componentModel = "spring")
public interface SearchIndexMapper {
    SearchIndexDto toDto(SearchIndex entity);
    SearchIndex toEntity(SearchIndexCreateRequest request);
    void updateEntity(@MappingTarget SearchIndex entity, SearchIndexUpdateRequest request);
}
```

---

## 3. Location

`com.govos.{context}.mapper`

---

## 4. Rules

| Rule | Detail |
|------|--------|
| **Spring component** | `componentModel = "spring"` |
| **One mapper per aggregate** | `SearchIndexMapper`, `ComplaintMapper` |
| **No business logic** | Mapping only; use `@AfterMapping` for simple defaults only |
| **Explicit mappings** | `@Mapping` for non-obvious field names |
| **Ignore unmapped** | Configure `unmappedTargetPolicy = ERROR` in processor config |

---

## 5. Update Pattern

Use `@MappingTarget` for partial updates:

```java
void updateEntity(@MappingTarget SearchIndex entity, SearchIndexUpdateRequest request);
```

Never replace entity reference — mutate in place.

---

## 6. Enum Mapping

- Same enum name: automatic
- DTO string ↔ entity enum: explicit `@Mapping` or default methods

---

## 7. Testing

- Mapper unit tests for complex mappings
- Simple mappings verified via service tests

---

## 8. Prohibited

- Manual mapping duplicated across services when MapStruct suffices
- Business validation in mappers
- Mapper dependencies on repositories
