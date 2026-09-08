# Testes — Guia de leitura rápida

Os testes garantem que o sistema **funciona como esperado** e que mudanças
futuras não quebrem o que já está pronto. Este projeto tem testes no backend
e no frontend.

## Por que testar importa aqui

Algumas regras são críticas e precisam de garantia, por exemplo:
- **Concorrência de vagas**: duas pessoas tentam a última vaga ao mesmo tempo —
  o sistema deve deixar só uma entrar.
- **Choque de horário**: ninguém se matricula em duas aulas no mesmo horário.
- **Curso autorizado**: aluno não entra em aula de outro curso.
- **Isolamento**: coordenador não vê/edita aula de outro coordenador.

Os testes checam exatamente esses casos.

## Testes do Backend (62 testes)

Divididos em dois tipos:

1. **Testes de unidade** — testam uma parte isolada (ex.: a regra de choque de
   horário) sem precisar do banco real. São rápidos.
2. **Testes de integração** — testam o fluxo completo (pedido → regra → banco),
   simulando o login e usando um **banco de testes separado** (`desafio_test`),
   recriado a cada teste. Garantem que tudo funciona junto, incluindo:
   - concorrência de matrícula (duas requisições simultâneas),
   - controle de acesso por perfil,
   - validações e erros (ex.: aula cheia → erro claro).

### Como rodar

```sh
cd desafio-backend
./mvnw test
```

> Precisa do **PostgreSQL** rodando (veja o [backend.md](backend.md)). O teste
> usa um banco próprio e não toca no banco de desenvolvimento.

## Testes do Frontend (11 testes)

Testam as partes visuais e de comportamento:
- serviço de **login/autenticação**,
- serviços que falam com o backend,
- as telas principais (aluno e coordenador),
- ações importantes (criar aula, matricular, excluir com confirmação).

### Como rodar

```sh
cd desafio-frontend
npx nx test frontend
```

## Automatização (CI)

O projeto usa **GitHub Actions** para rodar build + testes automaticamente a
cada atualização nos branches `main` e `development`. Ou seja, os testes são
executados sozinhos toda vez que algo muda — se algo quebrar, o próprio
sistema avisa antes de publicar.

## Resumo

| Onde | Quantidade | O que cobre |
|---|---|---|
| Backend | 62 | regras de negócio, concorrência, acesso, validações |
| Frontend | 11 | login, serviços e telas principais |

Testes rodam localmente ou de forma automática (CI).