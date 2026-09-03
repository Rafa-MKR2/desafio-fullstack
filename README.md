# desafio-fullstack

Full-stack academic administration system for managing courses, disciplines,
professors, schedules, coordinators, and students, secured with Keycloak.

> **Status:** foundation / scaffolding. The infrastructure, identity
> management, database schema, and seed data are in place. The domain
> entities, REST endpoints, and UI screens are still to be implemented.

## Architecture

```
Angular (Nx)  ──►  Quarkus REST API  ──►  PostgreSQL
     │                    │
     └──────── Keycloak (OIDC/JWT) ──┘
```

| Component | Technology | Directory |
|-----------|------------|-----------|
| Frontend  | Angular 22, Nx 23, Keycloak-Angular | `desafio-frontend/` |
| Backend   | Quarkus 3, Kotlin 21, Hibernate (Panache) | `desafio-backend/` |
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
- `init.sql` seed data: 15 disciplines, 5 professors, 9 schedules,
  9 courses, 3 coordinators, and 5 students.
- `start-desafio.sh` development bootstrap script (brings up Docker
  infrastructure and starts backend/frontend in dev mode).

### Identity (`desafio-keycloak/`)
- `realm.json` defining the `desafio` realm, realm roles
  (`coordenador`, `aluno`), and clients (`backend`, `frontend`).
- 8 users (3 coordinators, 5 students), default password `123456`.

### Backend (`desafio-backend/`)
- Quarkus project configured with PostgreSQL, OIDC, CORS and OpenAPI.
- OIDC wired to Keycloak (client `backend`).
- Currently contains only boilerplate code (`GreetingResource`, `MyEntity`).

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

- [ ] Implement backend domain entities (Disciplina, Professor, Horário,
      Curso, Coordenador, Aluno) and REST resources.
- [ ] Protect endpoints with role-based access control via OIDC.
- [ ] Build Angular routes, components and services consuming the API.
- [ ] Add unit and integration tests for both backend and frontend.

## License

MIT
```