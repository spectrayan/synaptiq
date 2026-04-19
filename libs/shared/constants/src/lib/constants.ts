/**
 * Synaptiq shared constants
 */

// ---------- API ----------
export const API_BASE_URL = '/api/v1';
export const API_ENDPOINTS = {
  AUTH: {
    SIGNUP: `${API_BASE_URL}/auth/signup`,
    LOGIN: `${API_BASE_URL}/auth/login`,
    REFRESH: `${API_BASE_URL}/auth/refresh`,
    ME: `${API_BASE_URL}/auth/me`,
  },
  TENANTS: {
    BASE: `${API_BASE_URL}/tenants`,
    BY_ID: (id: string) => `${API_BASE_URL}/tenants/${id}`,
    ADMINS: (id: string) => `${API_BASE_URL}/tenants/${id}/admins`,
  },
  CHAT: {
    MESSAGE: `${API_BASE_URL}/chat/message`,
    SESSIONS: `${API_BASE_URL}/chat/sessions`,
  },
  CATALOG: {
    SCHEMA: `${API_BASE_URL}/catalog/schema`,
    ITEMS: `${API_BASE_URL}/catalog/items`,
    IMPORT_CSV: `${API_BASE_URL}/catalog/import/csv`,
  },
} as const;

// ---------- Storage Keys ----------
export const STORAGE_KEYS = {
  THEME_PREFERENCE: 'synaptiq_theme',
  FONT_PREFERENCE: 'synaptiq_font',
  SESSION_ID: 'synaptiq_session_id',
} as const;

// ---------- Firebase ----------
export const FIREBASE_CONFIG = {
  // Populated at build time via environment.ts
} as const;

// ---------- Custom Claim Keys ----------
export const CLAIM_KEYS = {
  ROLE: 'role',
  TENANT_ID: 'tenant_id',
} as const;

// ---------- Roles ----------
export const ROLES = {
  PLATFORM_ADMIN: 'platform_admin',
  TENANT_ADMIN: 'tenant_admin',
  TENANT_EDITOR: 'tenant_editor',
  TENANT_VIEWER: 'tenant_viewer',
} as const;

export type UserRole = (typeof ROLES)[keyof typeof ROLES];
