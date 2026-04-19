/**
 * AuthGuard — route guard for authenticated/admin routes (T1.6)
 *
 * Usage in routes:
 *   { path: 'admin', canActivate: [authGuard], data: { roles: ['platform_admin', 'tenant_admin'] } }
 *   { path: 'chat',  canActivate: [authGuard] }   // Any authenticated user
 */
import { inject } from '@angular/core';
import { CanActivateFn, Router, ActivatedRouteSnapshot } from '@angular/router';
import { AuthService } from './auth.service';
import type { UserRole } from '@synaptiq/constants';

/**
 * Functional route guard — waits for auth initialization,
 * then checks login status and optional role requirements.
 */
export const authGuard: CanActivateFn = async (route: ActivatedRouteSnapshot) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  // Wait for Firebase auth to initialize (first load may be async)
  if (auth.isLoading()) {
    await waitForAuth(auth);
  }

  // Must be logged in
  if (!auth.isLoggedIn()) {
    return router.createUrlTree(['/login'], {
      queryParams: { returnUrl: route.url.join('/') },
    });
  }

  // Check role requirements (if specified in route data)
  const requiredRoles = route.data?.['roles'] as UserRole[] | undefined;
  if (requiredRoles && requiredRoles.length > 0) {
    const userRole = auth.userRole();
    if (!userRole || !requiredRoles.includes(userRole)) {
      // Redirect to unauthorized or home
      return router.createUrlTree(['/']);
    }
  }

  return true;
};

/**
 * Helper to wait until auth loading is complete.
 * Polls isLoading signal with a timeout.
 */
function waitForAuth(auth: AuthService, timeoutMs = 5000): Promise<void> {
  return new Promise((resolve) => {
    const start = Date.now();
    const check = () => {
      if (!auth.isLoading() || Date.now() - start > timeoutMs) {
        resolve();
      } else {
        setTimeout(check, 50);
      }
    };
    check();
  });
}
