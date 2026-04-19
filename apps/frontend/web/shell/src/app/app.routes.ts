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
    // Chat is the entire app — auth is handled conversationally within the shell.
    // Anonymous users get guest access; auth upgrades happen inline.
  },
  {
    path: 'admin',
    redirectTo: 'chat',
    // Admin capabilities are surfaced within the chat when the user has admin claims.
  },
  {
    path: '**',
    redirectTo: 'chat',
  },
];
