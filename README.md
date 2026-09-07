# desafio-fullstack

Sistema de administração acadêmica: coordenação gerencia o catálogo de aulas
(CRUD) e alunos se matriculam em aulas, com autenticação via Keycloak.

## Como funciona

```
Angular (Nx)  ──►  Quarkus REST API  ──►  PostgreSQL
      │                     │
      └────────── Keycloak (OIDC/JWT) ──┘
```

- **Aluno** (`aluno*@email.com`): consulta o catálogo de aulas, aplica filtros
  (disciplina/professor/dia) e se matricula. Vê as próprias matrículas com a
  ocupação de cada aula.
- **Coordenador** (`coordenador*@email.com`): cria, edita e exclui aulas. A
  criação/edição valida que o professor leciona a disciplina e que não há
  conflito de horário; a exclusão só é permitida se a aula não tiver
  matrículas.

| Componente | Tecnologia | Diretório |
|---|---|---|
| Frontend | Angular 22, Nx, Keycloak-Angular | `desafio-frontend/` |
| Backend | Quarkus 3, Java 21, Hibernate (Panache) | `desafio-backend/` |
| Identidade | Keycloak 24 (realm import) | `desafio-keycloak/` |
| Orquestração | Docker Compose | `desafio-completo/` |

## Pré-requisitos

Java 21, Maven 3.9+, Node.js 22+, npm, Docker e Docker Compose.

## Executando

### Infraestrutura (PostgreSQL + Keycloak)

```sh
cd desafio-completo
docker compose up -d postgres keycloak
```

Aguarde o Keycloak ficar saudável (healthcheck). Em seguida, em dois terminais:

```sh
# Backend (Quarkus dev mode, porta 8080)
cd desafio-backend
./mvnw quarkus:dev

# Frontend (Angular dev server, porta 4200)
cd desafio-frontend
npx nx serve frontend
```

Acesse http://localhost:4200, clique em **Entrar** e use um usuário abaixo.

> Alternativa: `./start-desafio.sh` (em `desafio-completo/`) sobe a infraestrutura
> e inicia backend/frontend em modo dev. Para tudo em containers:
> `docker compose up --build`.

## Portas e acessos

| Serviço | URL | Credenciais |
|---|---|---|
| Frontend | http://localhost:4200 | — |
| Backend | http://localhost:8080 | — |
| Swagger | http://localhost:8080/q/swagger-ui | — |
| Keycloak | http://localhost:8081 | `admin` / `admin` |
| PostgreSQL | http://localhost:5432 | `desafio` / `desafio123` |

## Usuários

| Usuário | Papel | Senha |
|---|---|---|
| `coordenador1..3@email.com` | `coordenador` | `123456` |
| `aluno1..5@email.com` | `aluno` | `123456` |

## Testes

```sh
# Backend: unit + integração (@QuarkusTest). Requer PostgreSQL em execução.
cd desafio-backend
./mvnw test

# Frontend
cd desafio-frontend
npx nx test frontend
```

CI (GitHub Actions) roda build + testes de backend e frontend a cada push/PR
para `main` e `development`.

## Licença

MIT