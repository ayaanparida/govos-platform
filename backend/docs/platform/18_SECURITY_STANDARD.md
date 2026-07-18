# GPS-001 — 18 Security Standard

---

## 1. Authentication

- **JWT Bearer tokens** on all protected endpoints
- Issued by GovOS auth module (`/api/v1/auth/login`)
- Validated by `JwtAuthenticationFilter` in `govos-security`
- Token contains: subject (username), authorities, expiry

---

## 2. Authorization (RBAC)

| Layer | Mechanism |
|-------|-----------|
| Endpoint | `@PreAuthorize("hasAuthority('CTX_ACTION')")` |
| Method security | `@EnableMethodSecurity` on module config |
| Default | Authenticated JWT required even without explicit permission |

Permission naming: `{CONTEXT}_{ACTION}` — see [05_NAMING_CONVENTIONS.md](./05_NAMING_CONVENTIONS.md)

---

## 3. Permission Examples

| Module | Permissions |
|--------|-------------|
| SRH | `SRH_MONITOR`, `SRH_ADMIN`, `SRH_REINDEX` |
| CMP | `CMP_READ`, `CMP_WRITE`, `CMP_ADMIN` (per module definition) |

Assign permissions via IDM module (platform responsibility).

---

## 4. Secret Management

| Secret | Storage |
|--------|---------|
| JWT signing key | Platform security config / secret manager |
| DB password | Environment / K8s Secret |
| API keys (OpenAI, etc.) | Environment — never in git |
| OpenSearch credentials | `GOVOS_SEARCH_USERNAME/PASSWORD` |

**Never** commit secrets. Use `${ENV_VAR}` in yaml.

---

## 5. TLS

| Connection | Requirement |
|------------|-------------|
| Client → API | HTTPS at ingress |
| API → PostgreSQL | TLS recommended in prod |
| API → OpenSearch | `ssl: true` in prod |
| API → external AI | HTTPS (provider default) |

---

## 6. Audit Logging

- Security filter logs: username, URI, status, duration (no body)
- Business audit: AUD module — separate from operational logs
- Admin mutations should be auditable (product/platform responsibility)

---

## 7. PII Handling

| Rule | Detail |
|------|--------|
| **Minimize logging** | No PII in application logs |
| **UUID identifiers** | Prefer UUID over national IDs in logs |
| **Search/query text** | Never log (SRH standard) |
| **API keys** | Never log |
| **Error responses** | No internal paths, SQL, or secrets |

---

## 8. Multi-Tenancy Security

- Enforce `organizationId` on data access
- JWT may carry organization context — validate at service layer
- Admin cross-tenant access requires explicit admin permission

---

## 9. CORS & CSRF

- API is stateless JWT — CSRF not applicable for REST
- CORS configured at platform level for known frontend origins

---

## 10. Prohibited

- Hardcoded credentials
- `@PreAuthorize` bypass or disabled security in prod
- Returning stack traces to clients
- Permissive CORS (`*`) in production
