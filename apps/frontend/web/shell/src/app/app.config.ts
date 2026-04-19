import {
  APP_INITIALIZER,
  ApplicationConfig,
  ErrorHandler,
  inject,
  provideBrowserGlobalErrorListeners,
  provideZonelessChangeDetection,
} from '@angular/core';
import { provideRouter, withComponentInputBinding, withViewTransitions } from '@angular/router';
import { provideClientHydration, withEventReplay } from '@angular/platform-browser';
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { appRoutes } from './app.routes';
import { authInterceptor } from '@synaptiq/auth';
import { ENVIRONMENT } from '@synaptiq/utils';
import { environment } from '../environments/environment';
import { GlobalErrorHandler } from './core/global-error-handler';
import { ThemeService } from './core/theme.service';

/** Load tenant branding during bootstrap (T10.6) */
function initializeBranding(): () => Promise<void> {
  const themeService = inject(ThemeService);
  return () => themeService.loadTenantBranding();
}

export const appConfig: ApplicationConfig = {
  providers: [
    // Zoneless signals-based change detection
    provideZonelessChangeDetection(),
    provideBrowserGlobalErrorListeners(),

    // Router with view transitions for smooth page changes
    provideRouter(
      appRoutes,
      withComponentInputBinding(),
      withViewTransitions(),
    ),

    // SSR hydration
    provideClientHydration(withEventReplay()),

    // HTTP with fetch API + auth interceptor
    provideHttpClient(
      withFetch(),
      withInterceptors([authInterceptor]),
    ),

    // Material animations (lazy-loaded)
    provideAnimationsAsync(),

    // Environment config — injectable by library services
    { provide: ENVIRONMENT, useValue: environment },

    // Global error boundary — prevents unhandled exceptions from crashing the app
    { provide: ErrorHandler, useClass: GlobalErrorHandler },

    // Load tenant branding on startup (T10.6)
    { provide: APP_INITIALIZER, useFactory: initializeBranding, multi: true },
  ],
};
