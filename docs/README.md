# Documentação do Projeto

Este sistema é uma **plataforma acadêmica** onde:
- **Coordenadores** criam e organizam as aulas.
- **Alunos** consultam aulas e se matriculam.

Tudo protegido por **login** (autenticação), então cada pessoa só vê o que lhe diz respeito.

## Como o projeto está dividido

| Parte | O que faz | Guia |
|---|---|---|
| **Backend** | O "cérebro" — processa as regras e guarda os dados | [backend.md](backend.md) |
| **Frontend** | As telas que a pessoa usa no navegador | [frontend.md](frontend.md) |
| **Docker** | A forma de rodar tudo junto com poucos comandos | veja a seção no [backend.md](backend.md) |
| **Testes** | Como o sistema se garante (backend e frontend) | [testing.md](testing.md) |

## Termos que você vai ver

- **Aula** — uma turma de uma disciplina em um dia/horário, com um professor e um número de vagas.
- **Curso** — o curso do aluno (ex.: Ciência da Computação). Uma aula pode ser liberada só para alguns cursos.
- **Matrícula** — quando um aluno se vincula a uma aula.
- **Login (Keycloak)** — sistema que cuida de usuários e senhas.

## Em 30 segundos

1. Suba o sistema com Docker.
2. Coordenador faz login e cria aulas.
3. Aluno faz login, escolhe uma aula e se matricula.
4. O backend garante as regras (curso autorizado, vaga disponível, sem choque de horário).