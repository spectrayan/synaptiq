/**
 * Environment configuration — production
 */
export const environment = {
  production: true,
  apiBaseUrl: '',  // Set via Cloud Run service URL
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
