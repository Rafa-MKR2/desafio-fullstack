# Backend — Guia de leitura rápida

O backend é a parte que **não aparece na tela**. Ele recebe os pedidos do
navegador, aplica as regras do sistema e guarda os dados no banco.

## O que ele faz

- **Guarda os dados** (alunos, professores, cursos, aulas, matrículas) no
  banco de dados **PostgreSQL**.
- **Aplica as regras** automaticamente, por exemplo:
  - Aluno só se matricula se a aula for liberada para o **curso** dele.
  - Não matricula se a aula **estiver cheia**.
  - Não matricula se **der choque de horário** com outra aula.
  - Coordenador só vê e edita as **próprias** aulas.
  - Professor não pode ter duas aulas no mesmo horário.
- **Exige login**: cada pedido vem com um login válido (feito pelo Keycloak)
  e o backend verifica o perfil (coordenador ou aluno).

## Como é organizado por dentro

Ele segue uma organização em camadas (como uma cozinha organizada):

- **Recursos (Resource)** — a "porta de entrada" que recebe os pedidos.
- **Serviços (Service)** — onde estão as regras e a lógica.
- **Repositórios (Repository)** — quem fala com o banco de dados.

Isso deixa o código claro e fácil de manter.

## Tecnologias usadas

- **Java 21** + **Quarkus** (framework de aplicação web).
- **PostgreSQL** (banco de dados).
- **Hibernate/Panache** (ponte entre o código e o banco).
- **Keycloak** (autenticação/login).

---

## Rodando com Docker (tudo junto)

Com Docker, você sobe o banco, a autenticação, o backend e o frontend com um
comando. É a forma mais fácil para testar o projeto inteiro.

```sh
cd desafio-completo
docker compose up --build
```

Isso inicia os serviços:

| Serviço | O que é | Endereço |
|---|---|---|
| Frontend | as telas | http://localhost:4200 |
| Backend | a API | http://localhost:8080 |
| Documentação da API | Swagger | http://localhost:8080/q/swagger-ui |
| Keycloak | login | http://localhost:8081 |
| PostgreSQL | banco de dados | porta 5432 |

> Login do Keycloak (admin): usuário `admin`, senha `admin`.

### Usuários para testar

| Usuário | Perfil | Senha |
|---|---|---|
| `coordenador1@email.com` | Coordenador | `123456` |
| `aluno1@email.com` | Aluno | `123456` |

---

## Rodando o backend sozinho (para quem desenvolve)

Você pode rodar só o banco e o login com Docker e o backend direto na sua máquina:

```sh
# 1) Banco + Keycloak (em segundo plano)
cd desafio-completo
docker compose up -d postgres keycloak

# 2) Backend em modo desenvolvimento
cd desafio-backend
./mvnw quarkus:dev
```

Aguarde o Keycloak ficar pronto (o backend precisa dele para validar o login).

---

## Testes

O backend tem **62 testes** que checam as regras (inclusive casos de
concorrência, quando duas pessoas tentam a mesma vaga ao mesmo tempo).

```sh
cd desafio-backend
./mvnw test
```

> Precisam do PostgreSQL rodando (Docker).