import type { Route } from '@angular/router';

export const alunoRoutes: Route[] = [
  { path: '', pathMatch: 'full', redirectTo: 'minhas-aulas' },
  {
    path: 'minhas-aulas',
    loadComponent: () =>
      import('./minhas-aulas/minhas-aulas.component').then(
        (m) => m.MinhasAulasComponent,
      ),
  },
];
