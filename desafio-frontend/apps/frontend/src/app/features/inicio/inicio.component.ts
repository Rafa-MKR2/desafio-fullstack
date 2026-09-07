import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

/** Página inicial: login público ou dashboard com acesso às telas do papel. */
@Component({
  selector: 'app-inicio',
  imports: [RouterLink],
  template: `
    <section class="inicio">
      @if (!auth.authenticated) {
        <!-- Página de login -->
        <div class="cartao cartao-centro">
          <div class="inicio-logo">🎓</div>
          <h1>Bem-vindo ao Sistema Acadêmico</h1>
          <p class="inicio-descricao">
            Acesse com sua conta para consultar aulas, realizar matrículas
            ou gerenciar o catálogo de aulas.
          </p>
          <button class="botao botao-primario botao-grande" type="button" (click)="auth.login()">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
              <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/>
              <polyline points="10 17 15 12 10 7"/>
              <line x1="15" y1="12" x2="3" y2="12"/>
            </svg>
            Entrar
          </button>
          <p class="inicio-rodape">
            🔒 Acesso seguro e autenticado
          </p>
        </div>
      } @else {
        <!-- Dashboard do usuário autenticado -->
        <div class="inicio-bemvindo">
          <div class="inicio-avatar-container">
            <span class="inicio-avatar">{{ inicial }}</span>
          </div>
          <div class="inicio-saudacao">
            <h1>Olá, {{ auth.displayName || auth.email }}</h1>
            <p class="inicio-subtexto">
              O que você gostaria de fazer hoje?
            </p>
          </div>
        </div>

        <div class="grade">
          @if (auth.isAluno) {
            <a class="cartao cartao-link cartao-destaque" routerLink="/aluno">
              <div class="card-icone-wrapper" aria-hidden="true">
                <span class="card-icone">📘</span>
              </div>
              <div class="card-conteudo">
                <strong class="card-titulo">Minhas aulas</strong>
                <span class="card-descricao">Consultar aulas disponíveis, realizar e acompanhar matrículas</span>
                <span class="card-badge badge-info">Aluno</span>
              </div>
              <svg class="card-seta" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                <path d="M5 12h14M12 5l7 7-7 7"/>
              </svg>
            </a>
          }
          @if (auth.isCoordenador) {
            <a class="cartao cartao-link cartao-destaque" routerLink="/coordenador">
              <div class="card-icone-wrapper" aria-hidden="true">
                <span class="card-icone">🗂️</span>
              </div>
              <div class="card-conteudo">
                <strong class="card-titulo">Gestão de aulas</strong>
                <span class="card-descricao">Criar, editar e organizar o catálogo de aulas</span>
                <span class="card-badge badge-primario">Coordenador</span>
              </div>
              <svg class="card-seta" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                <path d="M5 12h14M12 5l7 7-7 7"/>
              </svg>
            </a>
          }
        </div>

        <!-- Informações adicionais do usuário -->
        <div class="inicio-info">
          <div class="cartao cartao-info">
            <div class="info-item">
              <span class="info-label">Email</span>
              <span class="info-valor">{{ auth.email }}</span>
            </div>
            @if (auth.displayName) {
              <div class="info-item">
                <span class="info-label">Nome</span>
                <span class="info-valor">{{ auth.displayName }}</span>
              </div>
            }
            <div class="info-item">
              <span class="info-label">Papel</span>
              <span class="info-valor">
                @if (auth.isAluno && auth.isCoordenador) {
                  <span class="badge badge-info">Aluno</span>
                  <span class="badge badge-primario">Coordenador</span>
                } @else if (auth.isAluno) {
                  <span class="badge badge-info">Aluno</span>
                } @else if (auth.isCoordenador) {
                  <span class="badge badge-primario">Coordenador</span>
                } @else {
                  <span class="badge">Visitante</span>
                }
              </span>
            </div>
          </div>
        </div>
      }
    </section>
  `,
  styles: [`
    :host {
      display: block;
      --shadow-card: 0 1px 3px rgba(0, 0, 0, 0.06), 0 1px 2px rgba(0, 0, 0, 0.04);
      --shadow-hover: 0 8px 25px rgba(0, 0, 0, 0.1);
      --shadow-lg: 0 10px 40px rgba(0, 0, 0, 0.08);
      --transition-smooth: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
      --radius-md: 12px;
      --radius-sm: 8px;
      --radius-full: 9999px;
    }

    .inicio {
      max-width: 800px;
      margin: 0 auto;
      padding: 2rem 1rem;
    }

    /* ---- Cartão de login ---- */
    .cartao {
      background: #fff;
      border-radius: var(--radius-md);
      padding: 2rem;
      box-shadow: var(--shadow-card);
      border: 1px solid rgba(0, 0, 0, 0.04);
      transition: var(--transition-smooth);
    }

    .cartao-centro {
      text-align: center;
      max-width: 480px;
      margin: 2rem auto;
      padding: 3rem 2.5rem;
      background: linear-gradient(135deg, #ffffff 0%, #fafbff 100%);
      border: 1px solid rgba(79, 70, 229, 0.08);
    }

    .cartao-centro:hover {
      box-shadow: var(--shadow-lg);
    }

    .inicio-logo {
      font-size: 4rem;
      line-height: 1;
      margin-bottom: 0.5rem;
      display: inline-block;
      animation: float 3s ease-in-out infinite;
    }

    @keyframes float {
      0%, 100% { transform: translateY(0); }
      50% { transform: translateY(-8px); }
    }

    .cartao-centro h1 {
      margin: 0.5rem 0 1rem;
      font-size: 1.75rem;
      font-weight: 800;
      letter-spacing: -0.025em;
      color: #1a1a2e;
    }

    .inicio-descricao {
      color: var(--cor-texto-suave, #6b7280);
      font-size: 1rem;
      line-height: 1.6;
      margin-bottom: 2rem;
      max-width: 400px;
      margin-left: auto;
      margin-right: auto;
    }

    .inicio-rodape {
      margin-top: 1.5rem;
      font-size: 0.8rem;
      color: var(--cor-texto-suave, #6b7280);
    }

    /* ---- Botões ---- */
    .botao {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 0.5rem;
      padding: 0.55rem 1.1rem;
      font-size: 0.875rem;
      font-weight: 600;
      border: 1.5px solid transparent;
      border-radius: var(--radius-sm);
      cursor: pointer;
      transition: var(--transition-smooth);
      background: var(--cor-fundo-secundario, #f3f4f6);
      color: var(--cor-texto, #1a1a2e);
      line-height: 1.25;
      white-space: nowrap;
      user-select: none;
      text-decoration: none;
    }

    .botao:hover:not(:disabled) {
      transform: translateY(-2px);
      box-shadow: var(--shadow-hover);
    }

    .botao:active:not(:disabled) {
      transform: scale(0.97);
    }

    .botao-primario {
      background: var(--cor-primaria, #4f46e5);
      color: #fff;
      box-shadow: 0 2px 4px rgba(79, 70, 229, 0.25);
    }

    .botao-primario:hover:not(:disabled) {
      background: var(--cor-primaria-escura, #4338ca);
      box-shadow: 0 4px 16px rgba(79, 70, 229, 0.4);
    }

    .botao-grande {
      padding: 0.75rem 2rem;
      font-size: 1rem;
      min-width: 200px;
    }

    .botao:disabled {
      opacity: 0.5;
      cursor: not-allowed;
      transform: none !important;
    }

    /* ---- Saudação do usuário ---- */
    .inicio-bemvindo {
      display: flex;
      align-items: center;
      gap: 1.25rem;
      margin-bottom: 2.5rem;
      padding: 1.5rem;
      background: linear-gradient(135deg, #fafbff 0%, #ffffff 100%);
      border-radius: var(--radius-md);
      border: 1px solid rgba(79, 70, 229, 0.06);
    }

    .inicio-avatar-container {
      flex-shrink: 0;
    }

    .inicio-avatar {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 64px;
      height: 64px;
      border-radius: var(--radius-full);
      background: linear-gradient(135deg, var(--cor-primaria, #4f46e5), #7c3aed);
      color: #fff;
      font-size: 1.75rem;
      font-weight: 700;
      box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
    }

    .inicio-saudacao h1 {
      margin: 0;
      font-size: 1.5rem;
      font-weight: 700;
      letter-spacing: -0.025em;
      color: #1a1a2e;
    }

    .inicio-subtexto {
      margin: 0.2rem 0 0;
      color: var(--cor-texto-suave, #6b7280);
      font-size: 0.95rem;
    }

    /* ---- Grade de cards ---- */
    .grade {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
      gap: 1.5rem;
      margin-bottom: 2rem;
    }

    .cartao-link {
      display: flex;
      align-items: center;
      gap: 1rem;
      text-decoration: none;
      color: inherit;
      padding: 1.5rem;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      transition: var(--transition-smooth);
      border: 1px solid rgba(0, 0, 0, 0.04);
    }

    .cartao-link::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: linear-gradient(135deg, rgba(79, 70, 229, 0.03) 0%, transparent 100%);
      opacity: 0;
      transition: var(--transition-smooth);
    }

    .cartao-link:hover::before {
      opacity: 1;
    }

    .cartao-link:hover {
      transform: translateY(-4px);
      box-shadow: var(--shadow-hover);
      border-color: rgba(79, 70, 229, 0.15);
    }

    .cartao-link:active {
      transform: scale(0.98);
    }

    .card-icone-wrapper {
      flex-shrink: 0;
      width: 52px;
      height: 52px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: var(--radius-sm);
      background: rgba(79, 70, 229, 0.08);
      transition: var(--transition-smooth);
    }

    .cartao-link:hover .card-icone-wrapper {
      background: rgba(79, 70, 229, 0.15);
      transform: scale(1.05);
    }

    .card-icone {
      font-size: 1.75rem;
      line-height: 1;
    }

    .card-conteudo {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
    }

    .card-titulo {
      font-size: 1rem;
      font-weight: 700;
      color: #1a1a2e;
      letter-spacing: -0.01em;
    }

    .card-descricao {
      font-size: 0.875rem;
      color: var(--cor-texto-suave, #6b7280);
      line-height: 1.4;
    }

    .card-badge {
      display: inline-block;
      padding: 0.1rem 0.5rem;
      border-radius: var(--radius-full);
      font-size: 0.65rem;
      font-weight: 600;
      letter-spacing: 0.01em;
      margin-top: 0.2rem;
      align-self: flex-start;
    }

    .badge-info {
      background: #dbeafe;
      color: #1e40af;
    }

    .badge-primario {
      background: #eef2ff;
      color: var(--cor-primaria, #4f46e5);
    }

    .badge {
      background: #f3f4f6;
      color: var(--cor-texto-suave, #6b7280);
    }

    .card-seta {
      flex-shrink: 0;
      color: var(--cor-texto-suave, #6b7280);
      transition: var(--transition-smooth);
    }

    .cartao-link:hover .card-seta {
      color: var(--cor-primaria, #4f46e5);
      transform: translateX(4px);
    }

    /* ---- Informações do usuário ---- */
    .inicio-info {
      margin-top: 1rem;
    }

    .cartao-info {
      padding: 1.25rem 1.5rem;
      background: #fafbfc;
      border: 1px solid #f3f4f6;
    }

    .info-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0.4rem 0;
      border-bottom: 1px solid #f3f4f6;
    }

    .info-item:last-child {
      border-bottom: none;
    }

    .info-label {
      font-size: 0.8rem;
      font-weight: 600;
      color: var(--cor-texto-suave, #6b7280);
      text-transform: uppercase;
      letter-spacing: 0.04em;
    }

    .info-valor {
      font-size: 0.9rem;
      font-weight: 500;
      color: #1a1a2e;
      display: flex;
      gap: 0.4rem;
      align-items: center;
      flex-wrap: wrap;
      justify-content: flex-end;
    }

    /* ---- Responsividade ---- */
    @media (max-width: 768px) {
      .inicio {
        padding: 1rem;
      }

      .cartao-centro {
        padding: 2rem 1.5rem;
        margin: 1rem auto;
      }

      .cartao-centro h1 {
        font-size: 1.5rem;
      }

      .inicio-bemvindo {
        flex-direction: column;
        text-align: center;
        padding: 1.25rem;
      }

      .inicio-avatar {
        width: 56px;
        height: 56px;
        font-size: 1.5rem;
      }

      .grade {
        grid-template-columns: 1fr;
        gap: 1rem;
      }

      .cartao-link {
        padding: 1.25rem;
        flex-wrap: wrap;
      }

      .info-item {
        flex-direction: column;
        align-items: flex-start;
        gap: 0.2rem;
        padding: 0.6rem 0;
      }

      .info-valor {
        width: 100%;
        justify-content: flex-start;
      }
    }

    @media (max-width: 480px) {
      .cartao {
        padding: 1.25rem;
      }

      .botao-grande {
        width: 100%;
        min-width: unset;
      }

      .card-icone-wrapper {
        width: 44px;
        height: 44px;
      }

      .card-icone {
        font-size: 1.5rem;
      }
    }
  `],
})
export class InicioComponent {
  auth = inject(AuthService);

  get inicial(): string {
    const nome = this.auth.displayName?.trim();
    return nome ? nome.charAt(0).toUpperCase() : '?';
  }
}