import type { Route } from '@angular/router';
import { ROLES } from './core/auth/auth.service';
import { authGuard, hasAnyRole } from './core/auth/auth.guards';

export const appRoutes: Route[] = [
  { path: '', pathMatch: 'full', redirectTo: 'inicio' },
  {
    path: 'inicio',
    loadComponent: () =>
      import('./features/inicio/inicio.component').then(
        (m) => m.InicioComponent,
      ),
  },
  {
    path: 'aluno',
    canActivate: [authGuard, hasAnyRole(ROLES.ALUNO)],
    loadChildren: () => import('./features/aluno/routes').then((m) => m.alunoRoutes),
  },
  {
    path: 'coordenador',
    canActivate: [authGuard, hasAnyRole(ROLES.COORDENADOR)],
    loadChildren: () =>
      import('./features/coordenador/routes').then((m) => m.coordenadorRoutes),
  },
  { path: '**', redirectTo: 'inicio' },
];
