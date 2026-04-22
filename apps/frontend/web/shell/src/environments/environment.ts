/**
 * Environment configuration — development
 *
 * Uses Firebase Auth Emulator for local development.
 * No real Firebase project credentials needed — the emulator handles all auth.
 */
export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8000',
  firebase: {
    apiKey: 'fake-api-key-for-emulator',
    authDomain: 'localhost',
    projectId: 'synaptiq-dev',
    storageBucket: 'synaptiq-dev.appspot.com',
    messagingSenderId: '000000000000',
    appId: '1:000000000000:web:0000000000000000',
  },
  tenantId: 'demo-tenant', // Default tenant for local dev (matches seed_e2e_data.py)
  useEmulators: true,
  emulators: {
    authHost: 'localhost',
    authPort: 9099,
  },
} as const;
