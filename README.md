# desafio-fullstack

Full-stack academic administration system for managing courses, disciplines,
professors, schedules, coordinators, and students, secured with Keycloak.

> **Status:** in progress. Infrastructure, identity, database schema,
> backend domain model, and the enrollment business logic are in place.
> The remaining backend endpoints, authorization, and the Angular UI are
> still to be implemented.

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

#### API (`resource/`, `dto/`)
- `MatriculaResource` (`POST /matriculas`) with request/response DTOs
  using Bean Validation.

#### Error handling (`exception/`)
- Centralized `ExceptionMapper`s for business errors, invalid arguments,
  and validation failures, returning a standardized error payload.

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
- [ ] Implement `AulaService` (CRUD with creation/editing rules).
- [ ] Create REST resources for `Aula` and listing/filtering endpoints.
- [ ] Protect endpoints with role-based access control (`@RolesAllowed`).
- [ ] Document endpoints with Swagger/OpenAPI annotations.
- [ ] Add unit and integration tests (especially enrollment concurrency).

### Frontend
- [ ] Build API services with `HttpClient`.
- [ ] Implement `AuthGuard` and `RoleGuard` for protected routes.
- [ ] Create enrollment screen (student) and class management screens
      (coordinator).
- [ ] Apply Nx library structure and RxJS patterns.

### Infrastructure
- [ ] Configure test containers / test profile for backend integration
      tests (currently tests require live PostgreSQL + Keycloak).

## CI

GitHub Actions (`ci.yml`) runs on every push/PR to `main` and
`development`: backend build + unit tests (JDK 21) and frontend
build + tests (Node.js 22).

## License

MIT
```