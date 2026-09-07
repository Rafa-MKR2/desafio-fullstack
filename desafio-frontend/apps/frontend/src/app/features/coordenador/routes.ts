import type { Route } from '@angular/router';

export const coordenadorRoutes: Route[] = [
  { path: '', pathMatch: 'full', redirectTo: 'gestao-aulas' },
  {
    path: 'gestao-aulas',
    loadComponent: () =>
      import('./gestao-aulas/gestao-aulas.component').then(
        (m) => m.GestaoAulasComponent,
      ),
  },
];
