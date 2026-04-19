/**
 * Auth HTTP Interceptor — attaches Firebase ID token + tenant headers to outgoing API requests.
 *
 * Automatically:
 *   - Adds `Authorization: Bearer <idToken>` header
 *   - Adds `X-Tenant-ID` header from auth claims or environment fallback
 */
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpInterceptorFn } from '@angular/common/http';
import { from, switchMap } from 'rxjs';
import { AuthService } from './auth.service';
import { ENVIRONMENT } from '@synaptiq/utils';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const platformId = inject(PLATFORM_ID);

  // Skip on SSR
  if (!isPlatformBrowser(platformId)) {
    return next(req);
  }

  const env = inject(ENVIRONMENT);

  // Only intercept API calls (not external URLs or assets)
  if (!req.url.startsWith('/api') && !req.url.startsWith(env.apiBaseUrl)) {
    return next(req);
  }

  const auth = inject(AuthService);
  const tenantId = auth.tenantId() || env.tenantId;

  return from(auth.getIdToken()).pipe(
    switchMap((token) => {
      let headers = req.headers;

      if (token) {
        headers = headers.set('Authorization', `Bearer ${token}`);
      }

      if (tenantId) {
        headers = headers.set('X-Tenant-ID', tenantId);
      }

      return next(req.clone({ headers }));
    }),
  );
};
