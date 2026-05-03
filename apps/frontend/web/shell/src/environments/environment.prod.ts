/**
 * Environment configuration — production
 *
 * Uses Firebase auth in production.
 */
export const environment = {
  production: true,
  apiBaseUrl: '',  // Set via Cloud Run service URL
  authProvider: 'firebase' as const,
  firebase: {
    apiKey: '',
    authDomain: '',
    projectId: '',
    storageBucket: '',
    messagingSenderId: '',
    appId: '',
  },
  tenantId: '', // Resolved via subdomain in production
} as const;
