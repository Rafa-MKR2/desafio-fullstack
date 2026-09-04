# desafio-fullstack

Full-stack academic administration system for managing courses, disciplines,
professors, schedules, coordinators, and students, secured with Keycloak.

> **Status:** in progress. Infrastructure, identity, database schema,
> backend domain model, enrollment and aula business logic, REST
> resources (including read-only catalogs and "my enrollments" for
> students), role-based authorization, and unit + integration tests are
> in place. The Angular UI is still to be implemented.

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
- OIDC wired to Keycloak (client `backend`).

#### Domain model (`entity/`)
- `Disciplina`, `Professor` (many-to-many), `Horario`, `Curso`,
  `Coordenador`, `Aluno`, `Aula`, and `Matricula` JPA entities with
  proper relationships.
- `Aula` uses an optimistic `@Version` field for concurrency control.

#### Data access (`repository/`)
- `PanacheRepository` implementations for all eight entities.

#### Business logic (`service/`)
- `MatriculaService` implements enrollment rules: capacity control
  (pessimistic lock), duplicate enrollment prevention, and schedule
  conflict detection (day + time range overlap).
- `AulaService` implements class CRUD rules: reference validation,
  professor schedule conflict, vacancies never below enrolled count,
  and delete protection when enrollments exist.

#### API (`resource/`, `dto/`)
- `MatriculaResource` (`/matriculas`): `POST` enrolls the authenticated
  student, resolved from the JWT (`preferred_username` → e-mail), so
  students can only enroll themselves; `GET` lists the student's own
  enrollments and `GET /aulas` lists the aulas they are enrolled in
  (with occupancy).
- `AulaResource` (`/aulas`): `POST`, `GET` (with `disciplinaId`,
  `professorId`, `diaSemana` filters), `GET/{id}`, `PUT/{id}`, `DELETE/{id}`.
- `CatalogoResource` (`/disciplinas`, `/professores`, `/horarios`,
  `/cursos`): read-only catalogs available to `aluno` and `coordenador`
  for the enrollment screen.
- Request/response DTOs using Bean Validation, documented with OpenAPI
  annotations (`@Schema`, `@Operation`, `@APIResponse`).

#### Authorization
- `@RolesAllowed` via Keycloak realm roles: `coordenador` manages aulas
  (write), `aluno` and `coordenador` can list/search aulas, and only
  `aluno` can enroll.

#### Error handling (`exception/`)
- Centralized `ExceptionMapper`s for business errors (409), invalid
  arguments (400), validation failures (400), and missing resources
  (404), returning a standardized error payload.

#### Tests (`src/test/`)
- Mockito unit tests for `MatriculaService` and `AulaService` covering the
  business rules, with no live database required.
- Quarkus integration suite (`@QuarkusTest`, OIDC disabled, identities via
  `@TestSecurity`) exercising `AulaResource`, `MatriculaResource`,
  `CatalogoResource`, RBAC, and enrollment concurrency against a dedicated
  `desafio_test` database reseeded per test by `TestDataSeeder`.

### Frontend (`desafio-frontend/`)
- Angular application bootstrapped with Keycloak integration
  (`provideKeycloak`, silent SSO).
- Nginx reverse proxy for `/api/` in Docker builds.
- Currently only the default Nx welcome screen; no routes or screens yet.

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

### Option 2: local development

```sh
# 1. Infrastructure (PostgreSQL + Keycloak)
cd desafio-completo
./start-desafio.sh

# Or manually:
docker compose up -d postgres keycloak
```

Then, in separate terminals:

```sh
# Backend (Quarkus dev mode)
cd desafio-backend
./mvnw quarkus:dev

# Frontend (Angular dev server)
cd desafio-frontend
npx nx serve frontend
```

## Keycloak Users

| User                          | Role         | Password |
|-------------------------------|--------------|----------|
| `coordenador1..3@email.com`   | `coordenador` | `123456` |
| `aluno1..5@email.com`         | `aluno`      | `123456` |

## Next Steps / To-Do

### Backend
- [x] Implement `AulaService` (CRUD with creation/editing rules).
- [x] Create REST resources for `Aula` and listing/filtering endpoints.
- [x] Protect endpoints with role-based access control (`@RolesAllowed`).
- [x] Document endpoints with Swagger/OpenAPI annotations.
- [x] Add unit tests for the business rules.
- [x] Add integration tests (especially enrollment concurrency) with a
      test profile / seed data.
- [x] Expose read-only catalogs (`/disciplinas`, `/professores`,
      `/horarios`, `/cursos`) and the authenticated student's enrollments
      (`GET /matriculas`, `GET /matriculas/aulas`).

### Frontend
- [ ] Build API services with `HttpClient`.
- [ ] Implement `AuthGuard` and `RoleGuard` for protected routes.
- [ ] Create enrollment screen (student) and class management screens
      (coordinator).
- [ ] Apply Nx library structure and RxJS patterns.

### Infrastructure
- [x] Configure a dedicated `desafio_test` database for backend
      integration tests (OIDC disabled; still requires live PostgreSQL).

## CI

GitHub Actions (`ci.yml`) runs on every push/PR to `main` and
`development`: backend build + unit tests (JDK 21, against a PostgreSQL
service container) and frontend build + tests (Node.js 22).

## License

MIT
```