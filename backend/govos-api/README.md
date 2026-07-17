# GovOS API

Platform API foundation for the GovOS Enterprise Government Platform.

## Purpose

The `govos-api` module is the **HTTP entry point** of the modular monolith. It hosts REST controllers, cross-cutting API infrastructure, and the Spring Boot application bootstrap.

## Architecture

```
Client Request
     ↓
CorrelationIdFilter (X-Request-ID)
     ↓
SecurityRequestLoggingFilter
     ↓
JwtAuthenticationFilter (Bearer JWT)
     ↓
Controller
     ↓
Security / Domain Services
     ↓
ApiResponse<T> / PageResponse<T>
     ↓
GlobalExceptionHandler → ErrorResponse
```

## Module Location

| Artifact | Package | Responsibility |
|----------|---------|----------------|
| `govos-api` | `com.govos.api` | REST layer, API infrastructure, Boot entry point |
| `govos-domain` | `com.govos.*` | Business logic and persistence |
| `govos-security` | `com.govos.security` | Authentication / authorization, JWT filter chain |
| `govos-infrastructure` | `com.govos.infrastructure` | JPA, Flyway, technical config |

## Package Structure

```
com.govos.api.common
├── advice         # GlobalExceptionHandler
├── config         # OpenApiConfiguration, ApiWebConfiguration
├── exception      # EntityNotFoundException, BusinessException
├── filter         # CorrelationIdFilter
├── pagination     # PageResponse, PageMapper, SortParser
├── response       # ApiResponse, ErrorResponse, ValidationError
├── util           # ApiConstants, RequestContextUtils
└── validation     # PaginationRequest

com.govos.api.auth
├── controller     # AuthController
├── service        # AuthApplicationService
├── request        # LoginRequest, RefreshTokenRequest, LogoutRequest
├── response       # LoginResponse, AuthUserResponse, LogoutResponse, CurrentUserResponse
└── mapper         # AuthMapper

com.govos.api.platform
├── controller     # PlatformController
├── service        # PlatformApplicationService
├── response       # PlatformInfoResponse, PlatformVersionResponse, ModuleResponse, BuildResponse, HealthResponse
├── mapper         # PlatformMapper
└── config         # PlatformProperties, PlatformConfiguration

com.govos.api.cmp
├── controller     # ComplaintController
├── application    # ComplaintApplicationService, ComplaintApplicationServiceImpl
├── workflow       # ComplaintWorkflowIntegration (CMP-012 WRK sync)
├── notification   # ComplaintNotificationIntegration (CMP-013 NTF sync)
├── audit          # ComplaintAuditIntegration (CMP-014 AUD sync)
├── search         # ComplaintSearchIntegration (CMP-015 SRH sync)
├── request        # AssignComplaintRequest, RejectComplaintRequest, ...
├── response       # Reuses com.govos.cmp.dto (see ComplaintApiResponses)
└── mapper         # ComplaintApiMapper
```

## Authentication API

Platform authentication is wired to `govos-security` services. Controllers delegate to `AuthApplicationService`; no business logic lives in controllers.

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/v1/auth/login` | Public | Credential login — returns JWT + refresh token |
| `POST` | `/api/v1/auth/refresh` | Public | Rotate refresh token and issue new access token |
| `POST` | `/api/v1/auth/logout` | Public | Revoke refresh token and close session |
| `GET` | `/api/v1/auth/me` | Bearer JWT | Current authenticated user profile |

### Login

**Request**

```json
{
  "username": "jdoe",
  "password": "Secret123!"
}
```

**Response**

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
    "expiresIn": 900,
    "tokenType": "Bearer",
    "user": {
      "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "username": "jdoe",
      "email": "john.doe@gov.example",
      "roles": ["OFFICER"],
      "permissions": ["idm:user:read"]
    }
  },
  "requestId": "req-123"
}
```

### Refresh

**Request**

```json
{
  "refreshToken": "f47ac10b-58cc-4372-a567-0e02b2c3d479"
}
```

**Response** — same shape as login (`LoginResponse`).

### Logout

**Request**

```json
{
  "refreshToken": "f47ac10b-58cc-4372-a567-0e02b2c3d479"
}
```

**Response**

```json
{
  "success": true,
  "data": {
    "message": "Session revoked successfully"
  }
}
```

### Current user (`GET /me`)

Requires header:

```
Authorization: Bearer <accessToken>
```

**Response**

```json
{
  "success": true,
  "data": {
    "authenticated": true,
    "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "username": "jdoe",
    "email": "john.doe@gov.example",
    "roles": ["OFFICER"],
    "permissions": ["idm:user:read", "idm:user:write"]
  }
}
```

### Authentication workflow

```
POST /login
  → AuthApplicationService.login()
  → AuthenticationService.authenticate()
  → JwtTokenProvider.createAccessToken()
  → RefreshTokenService.createRefreshToken()
  → LoginResponse

POST /refresh
  → RefreshTokenService.rotateRefreshToken()
  → JwtTokenProvider.createAccessToken()
  → LoginResponse

POST /logout
  → LogoutService.logout()

GET /me
  → @CurrentUser JwtPrincipal
  → AuthApplicationService.currentUser()
  → CurrentUserResponse
```

## Platform Administration APIs

Operational metadata and health endpoints for platform administrators. All routes require JWT authentication.

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/v1/platform/info` | Runtime, build, database, and Flyway metadata |
| `GET` | `/api/v1/platform/version` | Semantic version and release metadata |
| `GET` | `/api/v1/platform/modules` | Registered modular monolith modules |
| `GET` | `/api/v1/platform/build` | Build and Git metadata from `BuildProperties` |
| `GET` | `/api/v1/platform/health` | Aggregated component health summary |

### Platform info

```json
{
  "success": true,
  "data": {
    "applicationName": "govos-api",
    "version": "0.1.0-SNAPSHOT",
    "environment": "local",
    "javaVersion": "21.0.11",
    "springBootVersion": "3.5.16",
    "database": "PostgreSQL",
    "flywayVersion": "1.7.0",
    "buildTime": "2026-07-17T18:00:00Z",
    "gitCommit": "c5af16c"
  }
}
```

### Platform health

```json
{
  "success": true,
  "data": {
    "database": "UP",
    "redis": "NOT_CONFIGURED",
    "minio": "NOT_CONFIGURED",
    "opensearch": "NOT_CONFIGURED",
    "disk": "UP",
    "memory": "UP",
    "uptime": "2h 15m 30s"
  }
}
```

Data sources:

- **Environment** — application name, active profiles, version
- **BuildProperties** — artifact, build time (via `spring-boot-maven-plugin` `build-info`)
- **GitProperties** — commit and branch when Git metadata is available
- **Flyway** — current schema version
- **HealthEndpoint** — database and disk actuator contributors
- **PlatformProperties** — release label, release date, module catalog (`govos.platform.*`)

## API Philosophy

- **Versioned URLs** — all business endpoints under `/api/v1`
- **Consistent envelopes** — success payloads wrapped in `ApiResponse<T>`
- **Explicit errors** — structured `ErrorResponse` with code, message, path, request ID
- **Correlation** — every request carries `X-Request-ID` for tracing
- **Thin controllers** — orchestration in application services; business rules in `govos-security` / `govos-domain`
- **OpenAPI first** — Swagger UI at `/swagger-ui.html`, spec at `/v3/api-docs`

## Response Standard

### Success

```json
{
  "success": true,
  "data": { },
  "message": null,
  "timestamp": "2026-07-17T17:30:00Z",
  "requestId": "f47ac10b-58cc-4372-a567-0e02b2c3d479"
}
```

### Error

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "path": "/api/v1/auth/login",
  "timestamp": "2026-07-17T17:30:00Z",
  "requestId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "errors": [
    { "field": "username", "message": "must not be blank", "rejectedValue": null }
  ]
}
```

## Error Handling

| Exception | HTTP Status | Code |
|-----------|-------------|------|
| `MethodArgumentNotValidException` | 400 | `VALIDATION_ERROR` |
| `ConstraintViolationException` | 400 | `CONSTRAINT_VIOLATION` |
| `IllegalArgumentException` | 400 | `INVALID_ARGUMENT` |
| `AuthenticationFailedException` | 401 | `UNAUTHORIZED` |
| `InvalidTokenException` / `RefreshTokenNotFoundException` | 401 | `INVALID_TOKEN` |
| `AccessDeniedException` | 403 | `FORBIDDEN` |
| `EntityNotFoundException` | 404 | `NOT_FOUND` |
| `BusinessException` | 422 | custom or `BUSINESS_ERROR` |
| Unhandled `Exception` | 500 | `INTERNAL_ERROR` |

## Configuration

JWT and session settings (`application.yml`):

```yaml
govos:
  security:
    jwt:
      secret: ${GOVOS_JWT_SECRET}
      issuer: govos
      access-token-ttl: 15m
      refresh-token-ttl: 7d
    session:
      max-per-user: 5
```

Spring Boot default security auto-configuration is excluded; GovOS `SecurityAutoConfiguration` from `govos-security` provides the stateless JWT filter chain.

## OpenAPI / JWT

`OpenApiConfiguration` declares a Bearer JWT security scheme. Authenticated endpoints (e.g. `GET /me`) require:

```
Authorization: Bearer <accessToken>
```

Obtain tokens via `POST /api/v1/auth/login` or `POST /api/v1/auth/refresh`.

## Dependencies

```
govos-api
  ├── govos-domain
  ├── govos-infrastructure
  ├── govos-security
  ├── spring-boot-starter-web
  ├── spring-boot-starter-validation
  ├── spring-boot-starter-actuator
  └── springdoc-openapi-starter-webmvc-ui
```

## Out of Scope (Current Sprint)

- Business / domain REST controllers (MDM, ORG, Complaint)
- Angular frontend

## Implementation Roadmap

| Phase | Scope |
|-------|-------|
| Foundation | Response envelopes, errors, pagination, correlation, OpenAPI |
| Auth contracts | `/api/v1/auth/**` REST surface |
| Security Phase 3 | JWT filter chain, `@CurrentUser` |
| **Auth integration** | Wire login, refresh, logout, `/me` to security services |
| **Platform admin (current)** | `/api/v1/platform/**` metadata, modules, build, health |
| Next | MDM / IDM / ORG read APIs |
| Next | Complaint module APIs |

## Document History

| Version | Date | Changes |
|---------|------|---------|
| 0.1.0 | 2026-07-17 | Platform API foundation |
| 0.2.0 | 2026-07-17 | Authentication REST API contracts |
| 0.3.0 | 2026-07-17 | Authentication API integration with security services |
| 0.4.0 | 2026-07-17 | Platform Administration APIs |
