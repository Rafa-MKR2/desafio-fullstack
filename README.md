# desafio-fullstack

Full-stack academic administration system for managing courses, disciplines,
professors, schedules, aulas, and enrollments, secured with Keycloak. Students
can browse aulas and enroll; coordinators manage the aula catalog (CRUD).

> **Status:** in progress. Backend and frontend are functionally complete and
> covered by automated tests. The work lives on two feature branches not yet
> merged into `development`: `feature/frontend` (all frontend screens, auth and
> services) and `fix/backend-oidc` (two small fixes that make real Keycloak
> logins work end to end).

## Architecture

```
Angular (Nx)  ──►  Quarkus REST API  ──►  PostgreSQL
     │                    │
     └──────── Keycloak (OIDC/JWT) ──┘
```

| Component | Technology | Directory |
|-----------|------------|-----------|
| Frontend  | Angular 22, Nx 23, Keycloak-Angular | `desafio-frontend/` |
| Backend   | Quarkus 3, Java 21, Hibernate (Panache) | `desafio-backend/` |
| Identity  | Keycloak 24 (realm import) | `desafio-keycloak/` |
| Orchestration | Docker Compose + scripts | `desafio-completo/` |

## Repository Structure

```
desafio-fullstack/
├── desafio-backend/    # Quarkus REST API (Java/Kotlin + Maven)
├── desafio-frontend/   # Angular SPA (Nx monorepo)
├── desafio-keycloak/   # Keycloak realm definition (realm.json)
└── desafio-completo/   # Docker Compose, DB seed and dev scripts
```

## What Is Already Done

### Infrastructure (`desafio-completo/`)
- `docker-compose.yml` orchestrating four services with health checks:
  PostgreSQL, Keycloak, backend, and frontend.
- Keycloak advertises its public URL (`KC_HOSTNAME_URL: http://localhost:8081`)
  so the OIDC issuer keeps the correct port — without it Keycloak announces
  `http://localhost` (port 80) and token validation fails.
- `init.sql` normalized schema and seed data: 15 disciplines,
  5 professors, 9 schedules, 9 courses, 3 coordinators, and 5 students,
  plus `aula`, `matricula`, and `professor_disciplina` tables.
- `start-desafio.sh` development bootstrap script (brings up Docker
  infrastructure and starts backend/frontend in dev mode).

### Identity (`desafio-keycloak/`)
- `realm.json` defining the `desafio` realm, realm roles
  (`coordenador`, `aluno`), and clients (`backend`, `frontend`).
- 8 users (3 coordinators, 5 students), default password `123456`.

### Backend (`desafio-backend/`)
- Quarkus project configured with PostgreSQL, OIDC, CORS and OpenAPI.
- OIDC wired to Keycloak (client `backend`). Two local-dev settings worth
  knowing:
  - `quarkus.oidc.roles.role-claim-path=realm_access/roles` — nested claims
    use `/` as the separator; the common `realm_access.roles` form silently
    fails to resolve roles.
  - Audience verification is disabled because the Keycloak 24 realm does not
    emit an `aud` claim on tokens.
- 8 JPA entities with relationships, Panache repositories, and business
  services (`MatriculaService`, `AulaService`, `HorarioUtil`) enforcing
  capacity, duplicate enrollment, and schedule-conflict rules.
- REST resources (`/aulas`, `/matriculas`, read-only catalogs), DTOs with
  Bean Validation, role-based access via Keycloak realm roles, centralized
  exception mappers (400/404/409), and OpenAPI/Swagger annotations.
- Tests: Mockito unit tests plus a `@QuarkusTest` integration suite (OIDC
  disabled, identities via `@TestSecurity`) exercising the REST flows, RBAC
  and enrollment concurrency against a dedicated `desafio_test` database
  reseeded per test by `TestDataSeeder` — **50 tests passing** (requires a
  running PostgreSQL, as in CI).

### Frontend (`desafio-frontend/`) — branch `feature/frontend`
- Angular 22 standalone app (signals + `@if`/`@for` control flow) with Nx.
- Keycloak integration via `keycloak-angular` (`provideKeycloak`,
  `withAutoRefreshToken`, silent SSO) and a Bearer-token interceptor scoped to
  the API base URL.
- Auth facade (`AuthService`) and functional guards: `authGuard` (login
  redirect) and `hasAnyRole(...)` for role-protected routes.
- Role-based lazy routes: `/` → home; `/aluno` → `minhas-aulas`;
  `/coordenador` → `gestao-aulas`.
- Screens:
  - **Home** (`inicio`): public login button, then role-aware shortcuts
    (student/coordinator) and logout.
  - **Student — "Matrícula em aulas"** (`minhas-aulas`): lists the student's
    own aulas, catalog filters (disciplina/professor/dia), enrollment with
    occupancy info and backend error feedback.
  - **Coordinator — "Gestão de aulas"** (`gestao-aulas`): full aula CRUD —
    list with filters, create/edit form (professors filtered by the selected
    discipline, vagas validation aligned with backend rules) and two-click
    delete confirmation.
- Shared `HttpClient` services (`AulaService`, `CatalogoService`,
  `MatriculaService`), typed models mirroring the backend DTOs, and
  `environments` (dev/prod via `fileReplacements`).
- Tests: **11 passing** (auth service, aula service, app shell, student and
  coordinator component specs). `nx build` succeeds (bundle is above the
  default 500 kB initial budget — mostly keycloak-js; expected).

## Prerequisites

- Java 21
- Maven 3.9+
- Node.js 22+ and npm
- Docker and Docker Compose

## Getting Started

### Option 1: full Docker stack

```sh
cd desafio-completo
docker compose up --build
```

| Service   | URL                            | Credentials        |
|-----------|--------------------------------|--------------------|
| Frontend  | http://localhost:4200          | —                  |
| Backend   | http://localhost:8080          | —                  |
| Swagger   | http://localhost:8080/q/swagger-ui | —              |
| Keycloak  | http://localhost:8081          | `admin` / `admin`  |
| PostgreSQL| http://localhost:5432          | `desafio` / `desafio123` |

> **Note:** the backend and frontend container services assume the Keycloak
> hostname is reachable as configured in `KC_HOSTNAME_URL`; prefer Option 2
> (host dev mode) for everyday work.

### Option 2: local development (recommended)

```sh
# 1. Infrastructure (PostgreSQL + Keycloak)
cd desafio-completo
./start-desafio.sh

# Or manually:
docker compose up -d postgres keycloak
```

Then, in separate terminals:

```sh
# Backend (Quarkus dev mode, port 8080)
cd desafio-backend
./mvnw quarkus:dev

# Frontend (Angular dev server, port 4200)
cd desafio-frontend
npx nx serve frontend
```

Wait for the Keycloak container to be healthy before starting the backend.
Open http://localhost:4200, click **Entrar** and log in with one of the
Keycloak users below.

## Keycloak Users

| User                          | Role         | Password |
|-------------------------------|--------------|----------|
| `coordenador1..3@email.com`   | `coordenador` | `123456` |
| `aluno1..5@email.com`         | `aluno`      | `123456` |

## Current Branches / Next Steps

- `fix/backend-oidc` — OIDC/Keycloak dev fixes described above; merge into
  `development` so real Keycloak logins work.
- `feature/frontend` — complete frontend; review and merge into `development`
  after (or together with) the backend fix.
- Optional polish afterwards: split/shrink the initial bundle to fit the
  Angular budget, and add a browser-level E2E smoke test of the login and
  enrollment flow.

## CI

GitHub Actions (`ci.yml`) runs on every push/PR to `main` and `development`:
backend build + tests (JDK 21, against a PostgreSQL service container, OIDC
disabled) and frontend build + tests (Node.js 22).

## License

MIT
