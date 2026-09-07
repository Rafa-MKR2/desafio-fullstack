# desafio-frontend

Frontend do **Sistema Acadêmico** (ver `../README.md` para o projeto completo).
SPA Angular 22 em um workspace Nx, com autenticação via Keycloak
(`keycloak-angular`) e telas para os papéis `aluno` e `coordenador`.

## Stack

- Angular 22 (componentes standalone, signals, control flow `@if`/`@for`)
- Nx 23 (app `frontend`), Vite + Vitest
- `keycloak-angular` 22 / `keycloak-js` (silent SSO, auto-refresh de token)
- Estilos próprios (CSS puro com variáveis em `app.css`)

## Estrutura

```
apps/frontend/src/
├── app/
│   ├── app.config.ts        # providers: router, HttpClient, Keycloak, guards
│   ├── app.routes.ts        # rotas lazy com guards por role
│   ├── core/auth/           # AuthService + guards (authGuard, hasAnyRole)
│   ├── shared/models/       # tipos espelhando os DTOs do backend
│   ├── shared/services/     # AulaService, CatalogoService, MatriculaService
│   └── features/
│       ├── inicio/          # página inicial (login + atalhos por perfil)
│       ├── aluno/minhas-aulas/     # tela do aluno (matrículas)
│       └── coordenador/gestao-aulas/ # tela do coordenador (CRUD de aulas)
├── environments/            # environment.ts / environment.prod.ts (fileReplacements)
└── public/silent-check-sso.html
```

## Requisitos

- Node.js 22+ e npm
- Backend, PostgreSQL e Keycloak rodando (ver `../desafio-completo` e o
  `README.md` principal)

## Comandos

```sh
npm install

# Dev server (porta 4200)
npx nx serve frontend

# Testes (Vitest)
npx nx test frontend

# Build de produção (usa environment.prod.ts via fileReplacements)
npx nx build frontend
```

## Configuração

- **Endereços** (API e Keycloak) ficam em `src/environments/environment.ts`;
  em produção o build troca para `environment.prod.ts`.
- O interceptor anexa o `Authorization: Bearer` **apenas** para requisições ao
  `environment.apiUrl`.
- As rotas são protegidas por `authGuard` (login) e `hasAnyRole(...)` (papel);
  usuário logado sem o papel é redirecionado para `/inicio`.
- As telas conversam com o backend pelos serviços em `shared/services` e
  exibem as mensagens de erro padronizadas da API.
