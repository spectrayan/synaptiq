import { Route } from '@angular/router';
import { authGuard } from '@synaptiq/auth';

export const appRoutes: Route[] = [
  {
    path: '',
    redirectTo: 'chat',
    pathMatch: 'full',
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./features/login/login.component').then(
        (m) => m.LoginComponent,
      ),
  },
  {
    path: 'chat',
    loadComponent: () =>
      import('./features/chat-shell/chat-shell.component').then(
        (m) => m.ChatShellComponent,
      ),
    // Chat is accessible without auth for public tenants (REQ-T access_mode)
    // Guard can be enabled per-tenant configuration
  },
  {
    path: 'admin',
    canActivate: [authGuard],
    data: { roles: ['platform_admin', 'tenant_admin'] },
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./features/admin/admin-shell.component').then(
            (m) => m.AdminShellComponent,
          ),
      },
    ],
  },
  {
    path: '**',
    redirectTo: 'chat',
  },
];
