/**
 * Environment configuration — development
 *
 * Uses built-in auth (MongoDB + JWT) for local development.
 * No Firebase dependency — the backend handles all auth.
 */
export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8000',
  authProvider: 'builtin' as const,
  firebase: {
    apiKey: 'not-used-with-builtin-auth',
    authDomain: 'localhost',
    projectId: 'synaptiq-dev',
    storageBucket: 'synaptiq-dev.appspot.com',
    messagingSenderId: '000000000000',
    appId: '1:000000000000:web:0000000000000000',
  },
  tenantId: 'demo-tenant', // Default tenant for local dev (matches seed_dev.py + seed_e2e_data.py)
  useEmulators: false, // Not needed with builtin auth
} as const;
