# GovOS Security

JWT infrastructure and authentication core for the GovOS Enterprise Government Platform.

## Purpose

The `govos-security` module is the **cross-cutting security layer** that turns IDM identity data into a verifiable Spring Security context for every request.

**Design principle:** IDM stores *who exists and what they may do*; Security decides *who is calling and whether the call is allowed*.

Architecture reference: `govos-architecture/docs/backend/security.md`

## Responsibilities

| Area | Status | Description |
|------|--------|-------------|
| Configuration properties | Done | JWT secret, TTL, issuer, clock skew, lockout, session limits |
| Password encoding | Done | BCrypt `PasswordEncoder` bean + `BCryptPasswordEncoderService` |
| UserDetails adapter | Done | `GovosUserDetailsService` loads users via IDM services |
| Authentication core | Done | `AuthenticationServiceImpl` — credential validation, lockout, audit |
| Refresh token metadata | Done | `RefreshTokenServiceImpl` — SHA-256 hashed persistence via IDM |
| Logout orchestration | Done | `LogoutServiceImpl` — revoke tokens, login history, audit events |
| JWT infrastructure | Done | HS512 access tokens via JJWT; validate, extract claims, authorities |
| Exception hierarchy | Done | Security + JWT exception types |

## Package Structure

```
com.govos.security
├── audit              # SecurityAuditPublisher (AUD bridge)
├── config             # SecurityProperties, module configuration beans
├── constant           # Shared security constants
├── exception          # SecurityException hierarchy
├── jwt                # JWT factory, provider, validator, claims, principal, authentication
├── model              # AuthenticationRequest, AuthenticationResult
├── password           # Password policy, credential resolver, BCrypt service
├── provider           # GovosUserPrincipal, GovosUserDetailsService
├── service            # Authentication, refresh, logout orchestration
└── util               # Authority helpers, refresh token hashing
```

## Configuration

Properties prefix: `govos.security`

| Property | Default | Description |
|----------|---------|-------------|
| `jwt.secret` | *(required)* | HS512 signing secret (min 64 bytes) |
| `jwt.issuer` | `govos` | JWT issuer claim |
| `jwt.access-token-ttl` | `15m` | Access token lifetime |
| `jwt.refresh-token-ttl` | `7d` | Refresh token lifetime |
| `jwt.clock-skew` | `60s` | Validation clock skew tolerance |
| `jwt.permission-embed-threshold` | `50` | Max permissions embedded in JWT |
| `password.bcrypt-strength` | `12` | BCrypt log rounds |
| `password.max-age` | `90d` | Password expiry policy |
| `lockout.max-attempts` | `5` | Failed logins before lock |
| `session.max-per-user` | `5` | Max concurrent refresh tokens |

Example:

```yaml
govos:
  security:
    jwt:
      secret: ${GOVOS_JWT_SECRET}
      issuer: govos
      access-token-ttl: 15m
      refresh-token-ttl: 7d
      clock-skew: 60s
    password:
      bcrypt-strength: 12
      max-age: 90d
    lockout:
      max-attempts: 5
    session:
      max-per-user: 5
```

## JWT Lifecycle

```
Login (Phase 3 wiring)
  → AuthenticationServiceImpl validates credentials
  → JwtTokenProviderImpl.createAccessToken(GovosUserPrincipal, sessionId)
  → JwtTokenFactory signs HS512 JWT (15 min TTL)
  → RefreshTokenServiceImpl persists SHA-256 hash of opaque UUID refresh token

Authenticated request (Phase 3 filter)
  → JwtTokenValidatorImpl.validateAccessToken(token)
  → JwtClaimsExtractorImpl extracts userId, roles, permissions
  → JwtTokenFactory.toPrincipal → JwtAuthentication in SecurityContext

Logout
  → LogoutServiceImpl revokes refresh token hash via IDM
```

## Access Token Claims

| Claim | Description |
|-------|-------------|
| `sub` | User UUID |
| `userId` | User UUID (explicit) |
| `username` | Login identifier |
| `roles` | Role codes (without `ROLE_` prefix) |
| `permissions` | Permission codes (omitted when count > threshold) |
| `session_id` | Audit session correlation |
| `iat` / `exp` | Issued at / expiry |
| `jti` | Unique token id |
| `iss` | Issuer (`govos.security.jwt.issuer`) |

## Signing & Validation

- **Library:** JJWT (`io.jsonwebtoken`) 0.12.x
- **Algorithm:** HS512
- **Secret:** `govos.security.jwt.secret` (minimum 64 bytes)
- **Clock skew:** 60 seconds default (`govos.security.jwt.clock-skew`)
- **Exceptions:** `JwtExpiredException`, `JwtInvalidSignatureException`, `JwtMalformedTokenException`

## Refresh Token Strategy

| Aspect | Policy |
|--------|--------|
| Format | Random UUID (opaque) |
| Client exposure | Raw token returned once at login (Phase 3 REST) |
| Persistence | SHA-256 hash only in `idm_refresh_token` |
| Never stored | Plaintext or BCrypt hash of refresh token |
| Rotation | Phase 3 REST orchestration |
| Session limit | `session.max-per-user` (default 5) |

## Testing

JaCoCo gate (≥ 80%) on authentication service implementations.

JWT unit tests cover token generation, expiry, invalid signature, malformed tokens, claims extraction, and authority mapping.

Run: `mvnw -pl govos-security verify`

## Implementation Roadmap

| Phase | Scope |
|-------|-------|
| 1 | Module skeleton, properties, BCrypt encoder, UserDetails adapter |
| 2 | Authentication core, refresh metadata, logout, AUD bridge |
| **3 (JWT infra — current)** | JJWT HS512, provider/validator/extractor, JwtPrincipal |
| Next | `JwtAuthenticationFilter`, `SecurityFilterChain`, wire auth REST |
| Next | `@PreAuthorize`, `GovosAuditorAware` |
| Next | Integration tests, production hardening |

## Out of Scope (Current Sprint)

- `SecurityFilterChain` and servlet filters
- Auth REST endpoint implementation (501 contracts in `govos-api`)
- `AuthenticationManager`
- Method-level security (`@PreAuthorize`)
- OAuth2, SSO, MFA

## Document History

| Version | Date | Changes |
|---------|------|---------|
| 0.1.0 | 2026-07-17 | Phase 1 module foundation |
| 0.2.0 | 2026-07-17 | Phase 2 authentication core |
| 0.3.0 | 2026-07-17 | JWT infrastructure (JJWT HS512) |
