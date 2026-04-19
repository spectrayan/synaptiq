/**
 * Environment injection token — allows library code to access
 * app-level environment configuration without deep relative imports.
 *
 * Usage in app bootstrap:
 *   providers: [{ provide: ENVIRONMENT, useValue: environment }]
 *
 * Usage in library services:
 *   private readonly env = inject(ENVIRONMENT);
 */
import { InjectionToken } from '@angular/core';

export interface AppEnvironment {
  readonly production: boolean;
  readonly apiBaseUrl: string;
  readonly firebase: {
    readonly apiKey: string;
    readonly authDomain: string;
    readonly projectId: string;
    readonly storageBucket: string;
    readonly messagingSenderId: string;
    readonly appId: string;
  };
  readonly tenantId: string;
  /** When true, connect Firebase services to local emulators. */
  readonly useEmulators?: boolean;
  /** Firebase Auth Emulator config (default: localhost:9099). */
  readonly emulators?: {
    readonly authHost?: string;
    readonly authPort?: number;
  };
}

export const ENVIRONMENT = new InjectionToken<AppEnvironment>('AppEnvironment');
