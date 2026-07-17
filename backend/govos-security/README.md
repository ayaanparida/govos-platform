# GovOS Security

Phase 2 — Authentication core for the GovOS Enterprise Government Platform.

## Purpose

The `govos-security` module is the **cross-cutting security layer** that turns IDM identity data into a verifiable Spring Security context for every request.

**Design principle:** IDM stores *who exists and what they may do*; Security decides *who is calling and whether the call is allowed*.

Architecture reference: `govos-architecture/docs/backend/security.md`

## Responsibilities

| Area | Status | Description |
|------|--------|-------------|
| Configuration properties | Done | JWT TTL, BCrypt strength, password max age, lockout, session limits |
| Password encoding | Done | BCrypt `PasswordEncoder` bean + `BCryptPasswordEncoderService` |
| UserDetails adapter | Done | `GovosUserDetailsService` loads users via IDM services |
| Authentication core | Done | `AuthenticationServiceImpl` — credential validation, lockout, audit |
| Refresh token metadata | Done | `RefreshTokenServiceImpl` — SHA-256 hashed persistence via IDM |
| Logout orchestration | Done | `LogoutServiceImpl` — revoke tokens, login history, audit events |
| JWT contracts | Pending | Interfaces only — implementation in Phase 3 |
| Exception hierarchy | Done | Domain security exceptions for REST mapping |

## Module Location

| Artifact | Package | Responsibility |
|----------|---------|----------------|
| `govos-security` | `com.govos.security` | Security orchestration, adapters, configuration |
| `govos-domain` | `com.govos.idm` / `com.govos.audit` | Identity and audit source of truth |
| `govos-api` | `com.govos.api.auth` (future) | Auth REST controllers |

## Package Structure

```
com.govos.security
├── audit              # SecurityAuditPublisher (AUD bridge)
├── config             # SecurityProperties, module configuration beans
├── constant           # JWT claim names, role prefix, config prefix
├── exception          # SecurityException hierarchy
├── jwt                # JwtTokenProvider, JwtTokenValidator, JwtClaimsExtractor (interfaces)
├── model              # AuthenticationRequest, AuthenticationResult, AuthenticationFailureReason
├── password           # PasswordEncoderService, policy validator, credential resolver
├── provider           # GovosUserPrincipal, GovosUserDetailsService
├── service            # Authentication, refresh, logout interfaces + implementations
└── util               # Authority helpers, refresh token hashing, login history logout
```

## Dependencies

```
govos-security
  ├── govos-domain    (IDM + AUD services)
  └── govos-common    (shared entity base types)

Forbidden:
  govos-security → govos-infrastructure
  govos-domain   → govos-security
```

## Configuration

Properties prefix: `govos.security`

| Property | Default | Description |
|----------|---------|-------------|
| `jwt.access-token-ttl` | `15m` | Access token lifetime (Phase 3) |
| `jwt.refresh-token-ttl` | `7d` | Refresh token lifetime |
| `jwt.permission-embed-threshold` | `50` | Max permissions embedded in JWT |
| `password.bcrypt-strength` | `12` | BCrypt log rounds |
| `password.max-age` | `90d` | Password expiry policy |
| `lockout.max-attempts` | `5` | Failed logins before lock |
| `session.max-per-user` | `5` | Max concurrent refresh tokens |

## Authentication Flow (Phase 2)

```
AuthenticationRequest
  → UserService.getByUsername
  → account enabled / not locked checks
  → PasswordHistoryService (latest hash + expiry)
  → BCryptPasswordEncoder.matches
  → on success: reset attempts, update lastLogin, LoginHistory, AuditSession, AuditEvent
  → on failure: increment attempts, lock at threshold, LoginHistory, AuditEvent
  → AuthenticationResult (principal + session metadata, NO JWT)
```

Password verification uses the latest `PasswordHistory` entry (IDM `UserDto` does not expose `passwordHash`).

## Refresh Token Flow (Phase 2)

- Generates opaque token internally (not returned — Phase 3 will expose via REST)
- Persists SHA-256 hash via `com.govos.idm.service.RefreshTokenService`
- Enforces `session.max-per-user` by revoking oldest active token

## Logout Flow (Phase 2)

- Revokes refresh token(s) via IDM
- Closes open login history session (completion record via `LoginHistoryService.record`)
- Publishes `USER_LOGOUT` audit event via `AuditEventService`

## Testing

JaCoCo gate (≥ 80%) on:

- `AuthenticationServiceImpl`
- `RefreshTokenServiceImpl`
- `LogoutServiceImpl`

Run: `mvnw -pl govos-security verify`

## Implementation Roadmap

| Phase | Scope |
|-------|-------|
| 1 | Module skeleton, properties, BCrypt encoder, UserDetails adapter, interfaces, exceptions |
| **2 (current)** | Authentication core, refresh metadata, logout, AUD bridge, unit tests |
| 3 | JWT issue/validate, `JwtAuthenticationFilter`, `SecurityFilterChain` |
| 4 | Auth REST controllers in `govos-api`, `@PreAuthorize` |
| 5 | `GovosAuditorAware`, NTF security alerts |
| 6 | Integration tests, production hardening |

## Out of Scope (Phase 2)

- JWT access token generation
- REST endpoints (`/auth/login`, `/auth/refresh`, `/auth/logout`)
- Servlet filters and `SecurityFilterChain`
- `AuthenticationManager`
- Method-level security (`@PreAuthorize`)
- Spring Security HTTP configuration
- Modifications to `govos-domain`
- OAuth2, SSO, MFA, API keys

## Document History

| Version | Date | Changes |
|---------|------|---------|
| 0.1.0 | 2026-07-17 | Phase 1 module foundation |
| 0.2.0 | 2026-07-17 | Phase 2 authentication core |
