# GPS-001 — 08 Entity Standard

---

## 1. Base Class

All persistent entities extend `com.govos.common.entity.AuditableEntity`:

- `id` (UUID)
- `code` (optional business key)
- `active`, `deleted`
- `version` (@Version optimistic locking)
- `createdBy`, `createdDate`, `updatedBy`, `updatedDate`

---

## 2. JPA Annotations

```java
@Entity
@Table(name = "srh_search_index", schema = "govos")
public class SearchIndex extends AuditableEntity {
```

- Explicit `@Table(name, schema)` — never rely on defaults
- `@Column(name = "...")` explicit snake_case mapping
- `@Enumerated(EnumType.STRING)` for enums

---

## 3. No Lombok on Entities

**Entities use manual getters/setters.** Rationale:

- JPA proxy compatibility
- Explicit audit field visibility
- No hidden `@Data` equals/hashCode issues

Lombok permitted in DTOs and test classes only with team approval.

---

## 4. Relationships

| Rule | Detail |
|------|--------|
| Within aggregate | `@OneToMany` with `cascade` carefully scoped |
| Cross aggregate (same context) | Lazy fetch default |
| Cross bounded context | **Forbidden** — UUID column only |

---

## 5. UUID Primary Keys

- Type: `java.util.UUID`
- Generated: DB default `gen_random_uuid()` or application-assigned
- Never use Long/Integer auto-increment for domain entities

---

## 6. Optimistic Locking

- `@Version` on `version` column (inherited from AuditableEntity)
- Services catch `OptimisticLockException` and translate to domain exception

---

## 7. Soft Delete

Entities implement soft delete via `deleted` flag:

- Repository queries: `findByIdAndDeletedFalse`
- Service methods: `softDelete()`, `restore()`
- Never `@SQLDelete` physical delete

---

## 8. Entity Documentation

- Javadoc on aggregate root describing bounded context ownership
- Reference sprint ID (e.g. SRH-002) in class comment

---

## 9. Prohibited

- Entities in `govos-api`
- Business logic beyond simple state checks in entity
- `@Data`, `@Builder` on JPA entities
- Bi-directional associations without mappedBy ownership clarity
