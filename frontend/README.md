# GovOS Frontend Workspace

Angular 20 monorepo for GovOS portal applications. Uses standalone components, the Vite-based application builder (`@angular/build:application`), SCSS, Angular Material, and OpenAPI-generated HTTP clients.

## Workspace Structure

```
frontend/
├── openapi/govos-api.yaml          # OpenAPI spec (auth + platform)
├── projects/
│   ├── govos-admin/                # Admin portal (primary app)
│   ├── govos-citizen/              # Citizen portal shell
│   ├── govos-officer/              # Officer portal shell
│   ├── govos-collector/            # Collector portal shell
│   ├── shared/                     # Environment & core providers
│   ├── shared-api/                 # OpenAPI-generated services
│   ├── shared-auth/                # JWT auth, interceptor, guard
│   └── shared-ui/                  # Admin layout & theme
```

## Prerequisites

- Node.js 20+
- npm
- GovOS backend running at `http://localhost:8080`

## Quick Start

```bash
cd frontend
npm install
npm run start:admin
```

Open `http://localhost:4200`. Unauthenticated users are redirected to `/auth/login`.

## Scripts

| Script | Description |
|--------|-------------|
| `npm run start:admin` | Serve admin portal |
| `npm run start:citizen` | Serve citizen portal shell |
| `npm run start:officer` | Serve officer portal shell |
| `npm run start:collector` | Serve collector portal shell |
| `npm run build` | Production build of govos-admin |
| `npm run build:all` | Build all four applications |
| `npm run generate:api` | Regenerate OpenAPI client from `openapi/govos-api.yaml` |

## Libraries

### `shared`

Core environment configuration via `provideGovosCore(environment)`.

### `shared-api`

OpenAPI-generated Angular services. **Do not hand-write HTTP services** — regenerate from the backend spec:

```bash
npm run generate:api
```

Provides `AuthenticationApiService`, `PlatformApiService`, and related models. Wired via `provideGovosApi(basePath)`.

### `shared-auth`

JWT authentication layer:

- `TokenStorageService` — persists access/refresh tokens in localStorage
- `AuthStore` — Angular signals for session state
- `GovosAuthService` — login, refresh, logout, load current user (uses generated API)
- `jwtInterceptor` — attaches Bearer token; refreshes on 401 and retries
- `authGuard` — protects routes; attempts refresh when session is stale

Register with `provideGovosAuth()`.

### `shared-ui`

Government enterprise theme and admin shell:

- Toolbar, sidebar, breadcrumb, footer
- Notification panel and profile menu
- Responsive layout with Material sidenav
- Dark-mode-ready CSS variables (`data-theme="dark"` on `<html>`)

Register with `provideGovosUi()`.

## Admin Portal

### Routing (lazy-loaded)

| Route | Component | Guard |
|-------|-----------|-------|
| `/auth/login` | Login | — |
| `/dashboard` | Platform dashboard | `authGuard` |

The admin layout (`AdminLayoutComponent`) wraps authenticated routes.

### Dashboard

Displays platform data from generated `PlatformApiService`:

- Platform information
- Health status
- Version & build
- Registered modules

## Authentication Flow

1. User submits credentials on the login page.
2. `GovosAuthService` calls `POST /api/v1/auth/login`.
3. Tokens are stored; the interceptor attaches the JWT to subsequent requests.
4. On 401, the interceptor calls `POST /api/v1/auth/refresh` and retries once.
5. `authGuard` blocks unauthenticated navigation and redirects to login.

## Theme & Dark Mode

Global styles import the GovOS theme from `shared-ui` and Angular Material's azure-blue prebuilt theme. Toggle dark mode by setting `data-theme="dark"` on the document root and applying `govos-theme-dark` to `<body>`.

## Path Aliases

TypeScript path mappings in `tsconfig.json`:

- `shared` → `projects/shared/src/public-api.ts`
- `shared-api` → `projects/shared-api/src/public-api.ts`
- `shared-auth` → `projects/shared-auth/src/public-api.ts`
- `shared-ui` → `projects/shared-ui/src/public-api.ts`

## Out of Scope (this foundation)

Complaint management, full citizen/officer/collector portals, charts, AI features, and global search are not included in this workspace foundation.
