import { inject } from '@angular/core';
import { Router } from '@angular/router';
import type { CanActivateFn } from '@angular/router';
import { createAuthGuard } from 'keycloak-angular';
import type { AuthGuardData } from 'keycloak-angular';

/** Dispara o login do Keycloak quando o usuário ainda não autenticou. */
async function ensureAuthenticated(authData: AuthGuardData): Promise<boolean> {
  if (authData.authenticated) {
    return true;
  }
  await authData.keycloak.login({ redirectUri: window.location.href });
  return false;
}

/** Exige usuário autenticado (qualquer role). */
export const authGuard: CanActivateFn = createAuthGuard<CanActivateFn>(
  async (_route, _state, authData) => ensureAuthenticated(authData),
);

/**
 * Exige autenticação + pelo menos uma das roles informadas.
 * Usuário logado sem a role é redirecionado para a página inicial.
 */
export function hasAnyRole(...allowedRoles: string[]): CanActivateFn {
  return createAuthGuard<CanActivateFn>(async (_route, _state, authData) => {
    if (!authData.authenticated) {
      return ensureAuthenticated(authData);
    }
    const grantedRoles = authData.grantedRoles.realmRoles ?? [];
    const allowed = allowedRoles.some((role) => grantedRoles.includes(role));
    return allowed || inject(Router).createUrlTree(['/inicio']);
  });
}
