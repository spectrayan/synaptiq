/**
 * Environment configuration — development
 *
 * Firebase config values should be populated from the Firebase Console
 * under Project Settings → General → Your apps → Web app.
 */
export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8000',
  firebase: {
    apiKey: '',
    authDomain: '',
    projectId: '',
    storageBucket: '',
    messagingSenderId: '',
    appId: '',
  },
  tenantId: 'acme-demo', // Default tenant for local dev
} as const;
