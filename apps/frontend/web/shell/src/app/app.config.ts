import {
  ApplicationConfig,
  provideBrowserGlobalErrorListeners,
  provideExperimentalZonelessChangeDetection,
} from '@angular/core';
import { provideRouter, withComponentInputBinding, withViewTransitions } from '@angular/router';
import { provideClientHydration, withEventReplay } from '@angular/platform-browser';
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { appRoutes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    // Zoneless signals-based change detection
    provideExperimentalZonelessChangeDetection(),
    provideBrowserGlobalErrorListeners(),

    // Router with view transitions for smooth page changes
    provideRouter(
      appRoutes,
      withComponentInputBinding(),
      withViewTransitions(),
    ),

    // SSR hydration
    provideClientHydration(withEventReplay()),

    // HTTP with fetch API
    provideHttpClient(withFetch()),

    // Material animations (lazy-loaded)
    provideAnimationsAsync(),
  ],
};
