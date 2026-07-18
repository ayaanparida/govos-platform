# GPS-001 — 27 Code Review Checklist

Enterprise code review gate for all GovOS pull requests.

---

## Architecture & DDD

- [ ] Changes stay within correct bounded context package
- [ ] No cross-context repository or entity imports
- [ ] Application service used for cross-context calls (ACL pattern)
- [ ] No business logic in controllers
- [ ] No business logic in mappers or repositories
- [ ] Domain events (if added) are immutable records in `event` package
- [ ] New dependencies respect layering (domain does not depend on api)

---

## Security

- [ ] New admin endpoints have `@PreAuthorize` with correct permission
- [ ] No secrets, API keys, or credentials in code or yaml commits
- [ ] No PII or query text in logs
- [ ] JWT-required endpoints documented in OpenAPI
- [ ] Input validated (`@Valid` + business validator)
- [ ] Organization/tenant scoping enforced on data access

---

## Database & Flyway

- [ ] Schema changes only via new Flyway migration in `govos-infrastructure`
- [ ] Migration follows naming convention (`V{x}_{y}_{z}__description.sql`)
- [ ] Migration header block complete
- [ ] Standard audit columns on new tables
- [ ] Soft delete pattern (`deleted` boolean)
- [ ] No cross-context foreign keys
- [ ] Indexes on query/filter columns

---

## Entities & Persistence

- [ ] Entity extends `AuditableEntity`
- [ ] No Lombok on entities
- [ ] Explicit `@Table(schema = "govos")`
- [ ] UUID primary keys
- [ ] Repository queries filter `DeletedFalse`
- [ ] Optimistic locking respected

---

## Services & Transactions

- [ ] Constructor injection only
- [ ] `@Transactional` on write operations
- [ ] `readOnly = true` on read-heavy methods
- [ ] Typed exceptions (not generic RuntimeException)
- [ ] DTOs at boundary — entities not leaked

---

## REST API

- [ ] URI follows `/api/v1/{context}/...` convention
- [ ] Correct HTTP status codes (202 for async)
- [ ] `ApiResponse<T>` wrapper used
- [ ] Pagination for list endpoints
- [ ] OpenAPI annotations on new endpoints
- [ ] No breaking changes to existing v1 contracts

---

## Validation & Errors

- [ ] Jakarta Validation on request DTOs
- [ ] Business validator for domain rules
- [ ] GlobalExceptionHandler maps new exceptions
- [ ] Error responses include requestId

---

## Testing

- [ ] Unit tests for new service/validator logic
- [ ] Controller test for new endpoints
- [ ] Test naming: `should...When...`
- [ ] `mvn test` passes locally
- [ ] Coverage meets JaCoCo gates for touched packages

---

## Observability

- [ ] Metrics use standard naming convention
- [ ] Structured logging (key=value)
- [ ] MDC correlation preserved
- [ ] No sensitive data in metrics labels

---

## Configuration

- [ ] New properties under `govos.{context}.*`
- [ ] Defaults safe for local dev
- [ ] Env override documented
- [ ] Feature flags use `enabled` boolean

---

## Scheduler (if applicable)

- [ ] Jobs owned by correct context module
- [ ] Cron externalized in properties
- [ ] Retry policy configured
- [ ] Job history and metrics recorded
- [ ] Products do not schedule platform jobs

---

## AI Integration (if applicable)

- [ ] Provider abstraction used — no direct vendor SDK in products
- [ ] API keys via environment only
- [ ] Embedding version tracked
- [ ] No embeddings or source text logged

---

## Documentation

- [ ] Module README updated for sprint scope
- [ ] New permissions documented
- [ ] Configuration properties documented
- [ ] GPS-001 compliance considered

---

## Reviewer Sign-Off

| Field | Value |
|-------|-------|
| Reviewer | |
| Date | |
| GPS-001 compliant | Yes / No |
| Approved | Yes / No |
