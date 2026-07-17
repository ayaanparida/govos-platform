# GovOS API

Platform API foundation for the GovOS Enterprise Government Platform.

## Purpose

The `govos-api` module is the **HTTP entry point** of the modular monolith. It hosts REST controllers, cross-cutting API infrastructure, and the Spring Boot application bootstrap.

This sprint delivers the **API foundation only** — standardized responses, error handling, pagination, correlation IDs, and OpenAPI documentation. Business controllers are deferred to domain REST sprints.

## Architecture

```
Client Request
     ↓
CorrelationIdFilter (X-Request-ID)
     ↓
Controller (future)
     ↓
Domain Service (govos-domain)
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
| `govos-security` | `com.govos.security` | Authentication / authorization (Phase 3 JWT) |
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
├── request        # LoginRequest, RefreshTokenRequest, LogoutRequest
├── response       # LoginResponse, RefreshTokenResponse, LogoutResponse, CurrentUserResponse
└── mapper         # AuthMapper
```

## Authentication API

Frozen REST contracts for platform authentication (`com.govos.api.auth`). Security services are wired via constructor injection; token issuance and enforcement activate in Security Phase 3.

| Method | Path | Status | Description |
|--------|------|--------|-------------|
| `POST` | `/api/v1/auth/login` | **501** | Credential login — contract only |
| `POST` | `/api/v1/auth/refresh` | **501** | Refresh access token — contract only |
| `POST` | `/api/v1/auth/logout` | **501** | Revoke refresh session — contract only |
| `GET` | `/api/v1/auth/me` | **200** | Placeholder unauthenticated profile |

### Login request

```json
{
  "username": "jdoe",
  "password": "Secret123!"
}
```

### Login response (future — Phase 3)

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "sessionId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
  }
}
```

### Current user placeholder (`GET /me`)

```json
{
  "success": true,
  "data": {
    "authenticated": false,
    "userId": null,
    "username": null,
    "email": null,
    "roles": [],
    "permissions": []
  },
  "message": "Unauthenticated placeholder until JWT Phase 3"
}
```

Controllers delegate exclusively to `govos-security` service interfaces (`AuthenticationService`, `RefreshTokenService`, `LogoutService`). Spring Security servlet auto-configuration is **disabled** until JWT Phase 3 to avoid an implicit filter chain.

## API Philosophy

- **Versioned URLs** — all business endpoints under `/api/v1`
- **Consistent envelopes** — success payloads wrapped in `ApiResponse<T>`
- **Explicit errors** — structured `ErrorResponse` with code, message, path, request ID
- **Correlation** — every request carries `X-Request-ID` for tracing
- **Thin controllers** — orchestration only; business rules stay in `govos-domain`
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
  "path": "/api/v1/example",
  "timestamp": "2026-07-17T17:30:00Z",
  "requestId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "errors": [
    { "field": "code", "message": "must not be blank", "rejectedValue": null }
  ]
}
```

## Error Handling

| Exception | HTTP Status | Code |
|-----------|-------------|------|
| `MethodArgumentNotValidException` | 400 | `VALIDATION_ERROR` |
| `ConstraintViolationException` | 400 | `CONSTRAINT_VIOLATION` |
| `IllegalArgumentException` | 400 | `INVALID_ARGUMENT` |
| `EntityNotFoundException` | 404 | `NOT_FOUND` |
| `BusinessException` | 422 | custom or `BUSINESS_ERROR` |
| Unhandled `Exception` | 500 | `INTERNAL_ERROR` |

## Pagination

List endpoints use Spring Data `Page` mapped via `PageMapper`:

```java
Page<UserDto> page = userService.findAll(pageable);
return ApiResponse.ok(PageMapper.toPageResponse(page));
```

Sort query parameter format (via `SortParser`):

```
?sort=createdDate,desc&sort=code,asc
```

Or semicolon-separated: `createdDate,desc;code,asc`

`PageResponse` fields: `content`, `page`, `size`, `totalElements`, `totalPages`, `sort`

## Versioning

| Constant | Value |
|----------|-------|
| `ApiConstants.API_VERSION` | `v1` |
| `ApiConstants.BASE_PATH` | `/api/v1` |

Configured in `application.yml` under `govos.api.version` and `govos.api.base-path`.

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

## OpenAPI / JWT Placeholder

`OpenApiConfiguration` declares a Bearer JWT security scheme for documentation purposes. **No JWT enforcement** is active in this sprint — Security Phase 3 will wire the filter chain.

## Out of Scope (This Sprint)

- JWT token issuance and validation
- `SecurityFilterChain` / request authentication enforcement
- Business / domain REST controllers
- Complaint module APIs
- Angular frontend

## Implementation Roadmap

| Phase | Scope |
|-------|-------|
| Foundation | Response envelopes, errors, pagination, correlation, OpenAPI |
| **Auth contracts (current)** | `/api/v1/auth/**` frozen REST surface (501 + `/me` placeholder) |
| Security Phase 3 | JWT, filter chain, wire auth endpoints to services |
| Next | MDM / IDM / ORG read APIs |
| Next | Complaint module APIs |

## Document History

| Version | Date | Changes |
|---------|------|---------|
| 0.1.0 | 2026-07-17 | Platform API foundation |
| 0.2.0 | 2026-07-17 | Authentication REST API contracts |
