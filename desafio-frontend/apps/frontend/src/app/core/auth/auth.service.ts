import { Injectable, inject } from '@angular/core';
import Keycloak from 'keycloak-js';

export const ROLES = {
  ALUNO: 'aluno',
  COORDENADOR: 'coordenador',
} as const;

/** Claims do token JWT que o frontend utiliza. */
interface TokenProfile {
  name?: string;
  preferred_username?: string;
  email?: string;
  realm_access?: { roles?: string[] };
}

/**
 * Facade sobre a instância Keycloak fornecida por `provideKeycloak`
 * (keycloak-angular v22). O token já é anexado às requisições pelo
 * `includeBearerTokenInterceptor`.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly keycloak = inject(Keycloak);

  private get profile(): TokenProfile | undefined {
    return this.keycloak.tokenParsed as TokenProfile | undefined;
  }

  get authenticated(): boolean {
    return this.keycloak.authenticated ?? false;
  }

  get displayName(): string {
    return (
      this.profile?.name ??
      this.profile?.preferred_username ??
      this.profile?.email ??
      ''
    );
  }

  get email(): string {
    return this.profile?.email ?? '';
  }

  /** Roles do realm (claim realm_access.roles — o backend valida por elas). */
  get roles(): string[] {
    return this.profile?.realm_access?.roles ?? [];
  }

  get isAluno(): boolean {
    return this.roles.includes(ROLES.ALUNO);
  }

  get isCoordenador(): boolean {
    return this.roles.includes(ROLES.COORDENADOR);
  }

  login(redirectUri: string = window.location.href): void {
    void this.keycloak.login({ redirectUri });
  }

  logout(): void {
    void this.keycloak.logout({ redirectUri: window.location.origin });
  }
}
