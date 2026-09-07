import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from './core/auth/auth.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  /** Inicial do nome para o avatar. */
  get inicial(): string {
    const nome = this.auth.displayName?.trim();
    return nome ? nome.charAt(0).toUpperCase() : '?';
  }

  /** Rótulo amigável do papel do usuário. */
  get papelLabel(): string {
    if (this.auth.isCoordenador) {
      return 'Coordenador';
    }
    if (this.auth.isAluno) {
      return 'Aluno';
    }
    return 'Visitante';
  }

  /** Título da página atual exibido na barra superior. */
  get topbarTitulo(): string {
    const url = this.router.url;
    if (url.startsWith('/coordenador')) {
      return 'Gestão de aulas';
    }
    if (url.startsWith('/aluno')) {
      return 'Minhas aulas';
    }
    return 'Início';
  }
}
