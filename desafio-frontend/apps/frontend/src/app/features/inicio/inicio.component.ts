import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

/** Página inicial: login público ou atalhos por perfil quando autenticado. */
@Component({
  selector: 'app-inicio',
  imports: [RouterLink],
  template: `
    <section class="inicio">
      @if (!auth.authenticated) {
        <div class="cartao cartao-centro">
          <h1>Sistema Acadêmico</h1>
          <p>
            Acesse com a sua conta do Keycloak para consultar aulas, realizar
            matrículas (aluno) ou gerenciar o catálogo de aulas (coordenador).
          </p>
          <button class="botao botao-primario" type="button" (click)="auth.login()">
            Entrar
          </button>
        </div>
      } @else {
        <div class="cartao">
          <h1>Olá, {{ auth.displayName || auth.email }} 👋</h1>
          <p class="perfil">Perfil: {{ auth.roles.join(', ') }}</p>
          <div class="grade">
            @if (auth.isAluno) {
              <a class="cartao cartao-link" routerLink="/aluno">
                <strong>Minhas aulas e matrículas</strong>
                <span>Ver aulas disponíveis, matricular-se e acompanhar as próprias aulas</span>
              </a>
            }
            @if (auth.isCoordenador) {
              <a class="cartao cartao-link" routerLink="/coordenador">
                <strong>Gestão de aulas</strong>
                <span>Criar, editar e excluir aulas do catálogo</span>
              </a>
            }
          </div>
          <button class="botao" type="button" (click)="auth.logout()">Sair</button>
        </div>
      }
    </section>
  `,
})
export class InicioComponent {
  auth = inject(AuthService);
}
