# Frontend — Guia de leitura rápida

O frontend é a **parte visual** — as telas que a pessoa usa no navegador.
Ele conversa com o backend para mostrar dados e enviar ações (como criar uma
aula ou se matricular).

## O que o usuário vê

**Tela de entrada (Home):**
- Botão de **login** (feito pelo Keycloak).
- Depois de logar, atalhos e informações da pessoa (nome, e-mail, perfil).

**Para o Coordenador — "Gestão de aulas":**
- Lista de aulas com **filtros** (disciplina, professor, dia, curso, só com vaga).
- Botão **Nova aula**: cria aula com disciplina, professor, horário, vagas e
  os **cursos autorizados** (quem pode se matricular).
- Botões para **editar** e **excluir** (com pedido de confirmação).

**Para o Aluno — "Minhas aulas":**
- Catálogo de aulas disponíveis com filtros.
- Botão **Matricular** em uma aula (mostra quantas vagas restam).
- Lista das **próprias matrículas**.

## Como é organizado por dentro

- **Componentes** — cada pedaço da tela (tela de login, tabela, formulário...).
- **Serviços** — código que fala com o backend (buscar aulas, matricular...).
- **Rotas protegidas** — só acessa cada tela quem tem o perfil certo.
- **Login** — o Keycloak cuida da autenticação; o frontend envia o "token" do
  usuário ao backend em cada pedido.

## Tecnologias usadas

- **Angular 22** (framework de interfaces).
- **Nx** (organização e build do projeto).
- **RxJS** (tratamento de dados assíncronos).
- **PrimeNG** (biblioteca de componentes visuais, como etiquetas de status).
- **Keycloak-Angular** (integração com o login).

## Rodando o frontend

Normalmente você usa o **Docker** (veja o [backend.md](backend.md)), que já
sobe o frontend junto.

Para desenvolvimento, em outro terminal, depois de subir backend + banco:

```sh
cd desafio-frontend
npx nx serve frontend
```

Abra **http://localhost:4200**.

## Testes

O frontend tem **11 testes** que checam serviços, login e as telas principais.

```sh
cd desafio-frontend
npx nx test frontend
```