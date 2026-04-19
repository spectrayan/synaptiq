import { Route } from '@angular/router';

export const appRoutes: Route[] = [
  {
    path: '',
    redirectTo: 'chat',
    pathMatch: 'full',
  },
  {
    path: 'chat',
    loadComponent: () =>
      import('./features/chat-shell/chat-shell.component').then(
        (m) => m.ChatShellComponent,
      ),
  },
  {
    path: '**',
    redirectTo: 'chat',
  },
];
