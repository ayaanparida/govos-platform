# GovOS Security

JWT infrastructure, authentication core, and servlet security for the GovOS Enterprise Government Platform.

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
| **HTTP security (Phase 3)** | **Done** | Stateless filter chain, JWT filter, 401/403 handlers, `@CurrentUser` |
| Exception hierarchy | Done | Security + JWT exception types |

## Package Structure

```
com.govos.security
├── annotation         # @CurrentUser controller parameter injection
├── audit              # SecurityAuditPublisher (AUD bridge)
├── config             # Properties, filter chain, auto-configuration
├── constant           # Shared security constants
├── exception          # SecurityException hierarchy
├── filter             # JwtAuthenticationFilter, SecurityRequestLoggingFilter
├── handler            # AuthenticationEntryPoint, AccessDeniedHandler, API envelope
├── jwt                # JWT factory, provider, validator, claims, principal, authentication
├── matcher            # PublicEndpointMatcher
├── model              # AuthenticationRequest, AuthenticationResult
├── password           # Password policy, credential resolver, BCrypt service
├── provider           # GovosUserPrincipal, GovosUserDetailsService
├── resolver           # JwtAuthenticationConverter, CurrentUserArgumentResolver
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

## Security Lifecycle

```
Application startup
  → SecurityAutoConfiguration (META-INF/spring auto-config)
  → SecurityFilterChainConfiguration builds SecurityFilterChain
  → Stateless session, CSRF disabled, CORS enabled

HTTP request
  → SecurityRequestLoggingFilter (request ID, timing)
  → JwtAuthenticationFilter (Bearer token → JwtAuthentication)
  → Spring Security authorization (public vs authenticated)
  → Controller (@CurrentUser injects JwtPrincipal)
  → SecurityRequestLoggingFilter logs status + duration
```

## JWT Flow

```
Login (govos-api REST — next sprint)
  → AuthenticationServiceImpl validates credentials
  → JwtTokenProviderImpl.createAccessToken(GovosUserPrincipal, sessionId)
  → JwtTokenFactory signs HS512 JWT (15 min TTL)
  → RefreshTokenServiceImpl persists SHA-256 hash of opaque UUID refresh token

Authenticated request
  → Authorization: Bearer <access-token>
  → JwtAuthenticationFilter extracts bearer token
  → JwtAuthenticationConverter validates via JwtTokenFactory.parseClaims
  → JwtTokenFactory.toPrincipal → JwtAuthentication in SecurityContextHolder
  → Request continues; context cleared after response

Invalid / expired token
  → JwtException → GovosAuthenticationEntryPoint → 401 ApiResponse envelope

Authenticated but not authorized
  → GovosAccessDeniedHandler → 403 ApiResponse envelope

Logout
  → LogoutServiceImpl revokes refresh token hash via IDM
```

## Authentication Process

1. **Public endpoints** — matched by `PublicEndpointMatcher` (no JWT required):
   - `/actuator/**`
   - `/swagger-ui/**`, `/v3/api-docs/**`
   - `POST /api/v1/auth/login`, `POST /api/v1/auth/refresh`, `POST /api/v1/auth/logout`
2. **Protected endpoints** — require authenticated `JwtAuthentication` in the security context.
3. **Missing token** — Spring Security invokes `GovosAuthenticationEntryPoint` (401).
4. **Invalid token** — filter catches `JwtException` and invokes entry point (401).
5. **Controller injection** — `@CurrentUser JwtPrincipal user` resolved by `CurrentUserArgumentResolver`.

## Request Lifecycle

| Step | Component | Action |
|------|-----------|--------|
| 1 | `SecurityRequestLoggingFilter` | Capture start time, propagate `X-Request-ID` |
| 2 | `JwtAuthenticationFilter` | Parse `Authorization: Bearer`, validate JWT |
| 3 | `SecurityFilterChain` | Enforce public vs authenticated rules |
| 4 | Controller | Business logic; `@CurrentUser` available |
| 5 | `GovosAuthenticationEntryPoint` / `GovosAccessDeniedHandler` | JSON error envelope on failure |
| 6 | `SecurityRequestLoggingFilter` | Log request ID, username, URI, status, duration |

Log format:

```
requestId=<id> username=<user|anonymous> uri=<path> status=<code> durationMs=<ms>
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
| Client exposure | Raw token returned once at login |
| Persistence | SHA-256 hash only in `idm_refresh_token` |
| Never stored | Plaintext or BCrypt hash of refresh token |
| Rotation | REST orchestration in `govos-api` |
| Session limit | `session.max-per-user` (default 5) |

## Integration with govos-api

`govos-security` registers via `com.govos.security.config.SecurityAutoConfiguration`.

The main application (`com.govos.GovosApplication`) component-scans `com.govos.security.*` when the module is on the classpath.

**Note:** Remove Spring Boot default security auto-config exclusions in `govos-api` `application.yml` only if they block the GovOS filter chain. GovOS uses its own `SecurityFilterChain` bean; ensure `spring.autoconfigure.exclude` does not list `com.govos.security.config.SecurityAutoConfiguration`.

Wire auth REST endpoints and `@GetMapping("/me")` with `@CurrentUser JwtPrincipal` in a follow-up `govos-api` sprint.

## Testing

JaCoCo gate (≥ 80%) on authentication service implementations.

Phase 3 tests cover:

- `JwtAuthenticationFilter` — public skip, valid token, invalid token, missing header
- `SecurityFilterChainConfiguration` — public vs protected URLs, `@CurrentUser` resolution
- `GovosAuthenticationEntryPoint` / `GovosAccessDeniedHandler` — JSON error envelopes
- `CurrentUserArgumentResolver` — parameter support and principal resolution

Run: `mvnw -pl govos-security verify`

## Implementation Roadmap

| Phase | Scope |
|-------|-------|
| 1 | Module skeleton, properties, BCrypt encoder, UserDetails adapter |
| 2 | Authentication core, refresh metadata, logout, AUD bridge |
| 3 | JWT infrastructure + HTTP security filter chain, handlers, `@CurrentUser` |
| Next | Wire auth REST in `govos-api`, `@PreAuthorize`, `GovosAuditorAware` |
| Next | Integration tests, production hardening |

## Out of Scope (Current Sprint)

- Auth REST endpoint implementation (501 contracts in `govos-api`)
- `AuthenticationManager`
- Method-level security (`@PreAuthorize`)
- OAuth2, SSO, MFA, Keycloak

## Document History

| Version | Date | Changes |
|---------|------|---------|
| 0.1.0 | 2026-07-17 | Phase 1 module foundation |
| 0.2.0 | 2026-07-17 | Phase 2 authentication core |
| 0.3.0 | 2026-07-17 | JWT infrastructure (JJWT HS512) |
| 0.4.0 | 2026-07-17 | Phase 3 HTTP security — filter chain, handlers, `@CurrentUser` |
