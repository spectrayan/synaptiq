/**
 * AuthService — signals-based authentication (supports builtin + Firebase)
 *
 * Provides reactive auth state via Angular signals:
 *   - currentUser()  → AuthUser | null
 *   - isLoggedIn()   → boolean
 *   - userRole()     → UserRole | null
 *   - tenantId()     → string | null
 *   - isLoading()    → boolean
 *
 * Provider modes:
 *   - "builtin": JWT-based auth via backend API (no Firebase dependency)
 *   - "firebase": Firebase Auth SDK (production)
 *
 * All auth operations return Promises and automatically update signals.
 */
import { computed, Injectable, signal, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { ENVIRONMENT } from '@synaptiq/utils';
import { CLAIM_KEYS, type UserRole } from '@synaptiq/constants';
import { firstValueFrom } from 'rxjs';

export interface AuthUser {
  uid: string;
  email: string | null;
  displayName: string | null;
  photoURL: string | null;
  emailVerified: boolean;
  role: UserRole | null;
  tenantId: string | null;
  isAnonymous: boolean;
  mustChangePassword?: boolean;
}

/** localStorage keys for builtin auth */
const STORAGE_KEY_TOKEN = 'synaptiq_auth_token';
const STORAGE_KEY_USER = 'synaptiq_auth_user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly router = inject(Router);
  private readonly env = inject(ENVIRONMENT);
  private readonly http = inject(HttpClient);

  // Firebase-specific (lazy-loaded only when provider=firebase)
  private firebaseApp: any = null;
  private firebaseAuth: any = null;
  private unsubscribe: (() => void) | null = null;

  // ─── Reactive State (signals) ──────────────────────────────────
  private readonly _user = signal<AuthUser | null>(null);
  private readonly _isLoading = signal(true);
  private readonly _error = signal<string | null>(null);

  /** Current authenticated user */
  readonly currentUser = this._user.asReadonly();

  /** Whether a user is currently logged in (including anonymous) */
  readonly isLoggedIn = computed(() => this._user() !== null);

  /** Whether user is a real (non-anonymous) authenticated user */
  readonly isAuthenticated = computed(() => {
    const user = this._user();
    return user !== null && !user.isAnonymous;
  });

  /** Whether user is an anonymous guest */
  readonly isGuest = computed(() => this._user()?.isAnonymous ?? false);

  /** Current user's role from custom claims */
  readonly userRole = computed(() => this._user()?.role ?? null);

  /** Current user's tenant ID from custom claims */
  readonly tenantId = computed(() => this._user()?.tenantId ?? null);

  /** Whether an auth operation is in progress */
  readonly isLoading = this._isLoading.asReadonly();

  /** Last auth error message */
  readonly error = this._error.asReadonly();

  /** Whether user has admin privileges */
  readonly isAdmin = computed(() => {
    const role = this.userRole();
    return role === 'platform_admin' || role === 'tenant_admin';
  });

  /** Whether user must change their password */
  readonly mustChangePassword = computed(() => this._user()?.mustChangePassword ?? false);

  /** Whether using built-in auth */
  private get isBuiltin(): boolean {
    return this.env.authProvider === 'builtin';
  }

  constructor() {
    if (isPlatformBrowser(this.platformId)) {
      if (this.isBuiltin) {
        this.initializeBuiltin();
      } else {
        this.initializeFirebase();
      }
    } else {
      // SSR — skip auth init
      this._isLoading.set(false);
    }
  }

  // ─── Builtin Auth Initialization ──────────────────────────────

  private initializeBuiltin(): void {
    // Restore session from localStorage
    try {
      const storedToken = localStorage.getItem(STORAGE_KEY_TOKEN);
      const storedUser = localStorage.getItem(STORAGE_KEY_USER);
      if (storedToken && storedUser) {
        const user: AuthUser = JSON.parse(storedUser);
        // Quick check: is token expired? (decode JWT payload)
        const payload = JSON.parse(atob(storedToken.split('.')[1]));
        if (payload.exp * 1000 > Date.now()) {
          this._user.set(user);
        } else {
          // Token expired — clear storage
          localStorage.removeItem(STORAGE_KEY_TOKEN);
          localStorage.removeItem(STORAGE_KEY_USER);
        }
      }
    } catch {
      // Corrupted storage — clear
      localStorage.removeItem(STORAGE_KEY_TOKEN);
      localStorage.removeItem(STORAGE_KEY_USER);
    }
    this._isLoading.set(false);
    console.log('🔐 Built-in auth initialized');
  }

  // ─── Firebase Initialization ──────────────────────────────────

  private async initializeFirebase(): Promise<void> {
    try {
      const { initializeApp } = await import('firebase/app');
      const {
        getAuth,
        connectAuthEmulator,
        onAuthStateChanged,
      } = await import('firebase/auth');

      this.firebaseApp = initializeApp(this.env.firebase);
      this.firebaseAuth = getAuth(this.firebaseApp);

      // Connect to Firebase Auth Emulator in development
      if (this.env.useEmulators) {
        const host = this.env.emulators?.authHost ?? 'localhost';
        const port = this.env.emulators?.authPort ?? 9099;
        connectAuthEmulator(this.firebaseAuth, `http://${host}:${port}`, {
          disableWarnings: true,
        });
        console.log(`🔧 Firebase Auth Emulator connected → http://${host}:${port}`);
      }

      // Listen to auth state changes
      this.unsubscribe = onAuthStateChanged(this.firebaseAuth, async (user: any) => {
        if (user) {
          const authUser = await this.mapFirebaseUser(user);
          this._user.set(authUser);
        } else {
          this._user.set(null);
        }
        this._isLoading.set(false);
      });
    } catch (e) {
      console.error('Firebase initialization failed:', e);
      this._isLoading.set(false);
      this._error.set('Failed to initialize authentication');
    }
  }

  // ─── Auth Operations ──────────────────────────────────────────

  /**
   * Sign in anonymously — allows immediate chat access as a guest.
   * The guest session can later be upgraded to a full account via linkWithCredential.
   */
  async signInAsGuest(): Promise<AuthUser> {
    if (this.isBuiltin) {
      // Builtin doesn't support anonymous — just create a temporary user
      const guest: AuthUser = {
        uid: 'guest-' + Date.now(),
        email: null,
        displayName: 'Guest',
        photoURL: null,
        emailVerified: false,
        role: null,
        tenantId: this.env.tenantId || null,
        isAnonymous: true,
      };
      this._user.set(guest);
      return guest;
    }

    this.clearError();
    this._isLoading.set(true);
    try {
      const { signInAnonymously } = await import('firebase/auth');
      const credential = await signInAnonymously(this.firebaseAuth);
      const authUser = await this.mapFirebaseUser(credential.user);
      this._user.set(authUser);
      return authUser;
    } catch (e: any) {
      const msg = this.parseError(e);
      this._error.set(msg);
      throw new Error(msg);
    } finally {
      this._isLoading.set(false);
    }
  }

  /**
   * Sign up with email and password.
   * If user is currently anonymous, upgrades the anonymous account (Firebase only).
   */
  async signUp(email: string, password: string): Promise<AuthUser> {
    this.clearError();
    this._isLoading.set(true);

    try {
      if (this.isBuiltin) {
        return await this.builtinSignUp(email, password);
      }
      return await this.firebaseSignUp(email, password);
    } catch (e: any) {
      const msg = this.parseError(e);
      this._error.set(msg);
      throw new Error(msg);
    } finally {
      this._isLoading.set(false);
    }
  }

  /**
   * Sign in with email and password.
   */
  async signIn(email: string, password: string): Promise<AuthUser> {
    this.clearError();
    this._isLoading.set(true);

    try {
      if (this.isBuiltin) {
        return await this.builtinSignIn(email, password);
      }
      return await this.firebaseSignIn(email, password);
    } catch (e: any) {
      const msg = this.parseError(e);
      this._error.set(msg);
      throw new Error(msg);
    } finally {
      this._isLoading.set(false);
    }
  }

  /**
   * Sign in with Google OAuth popup (Firebase only).
   * If user is currently anonymous, upgrades via linkWithPopup.
   */
  async signInWithGoogle(): Promise<AuthUser> {
    if (this.isBuiltin) {
      throw new Error('Google sign-in is not available with built-in auth. Configure OIDC or use Firebase.');
    }

    this.clearError();
    this._isLoading.set(true);
    try {
      const {
        signInWithPopup,
        GoogleAuthProvider,
        linkWithPopup,
      } = await import('firebase/auth');
      const provider = new GoogleAuthProvider();

      // If currently anonymous, link to upgrade the account
      if (this.firebaseAuth.currentUser?.isAnonymous) {
        const result = await linkWithPopup(this.firebaseAuth.currentUser, provider);
        const authUser = await this.mapFirebaseUser(result.user);
        this._user.set(authUser);
        return authUser;
      }

      const credential = await signInWithPopup(this.firebaseAuth, provider);
      const authUser = await this.mapFirebaseUser(credential.user);
      this._user.set(authUser);
      return authUser;
    } catch (e: any) {
      const msg = this.parseError(e);
      this._error.set(msg);
      throw new Error(msg);
    } finally {
      this._isLoading.set(false);
    }
  }

  /**
   * Sign out the current user.
   */
  async signOutUser(): Promise<void> {
    try {
      if (this.isBuiltin) {
        localStorage.removeItem(STORAGE_KEY_TOKEN);
        localStorage.removeItem(STORAGE_KEY_USER);
        this._user.set(null);
      } else {
        const { signOut } = await import('firebase/auth');
        await signOut(this.firebaseAuth);
        this._user.set(null);
      }
    } catch {
      this._error.set('Failed to sign out');
    }
  }

  /**
   * Get the current auth token for API calls.
   */
  async getIdToken(): Promise<string | null> {
    if (this.isBuiltin) {
      return localStorage.getItem(STORAGE_KEY_TOKEN);
    }
    if (!this.firebaseAuth?.currentUser) return null;
    return this.firebaseAuth.currentUser.getIdToken();
  }

  /**
   * Force refresh the ID token (e.g. after custom claims update).
   */
  async refreshToken(): Promise<void> {
    if (this.isBuiltin) {
      // For builtin, user must re-login for a fresh token
      return;
    }
    if (!this.firebaseAuth?.currentUser) return;
    await this.firebaseAuth.currentUser.getIdToken(/* forceRefresh */ true);
    const authUser = await this.mapFirebaseUser(this.firebaseAuth.currentUser);
    this._user.set(authUser);
  }

  /**
   * Change password (builtin auth only).
   */
  async changePassword(currentPassword: string, newPassword: string): Promise<void> {
    if (!this.isBuiltin) {
      throw new Error('Password change via API is only available with built-in auth');
    }

    const url = `${this.env.apiBaseUrl}/api/v1/auth/change-password`;
    await firstValueFrom(
      this.http.post(url, { current_password: currentPassword, new_password: newPassword })
    );

    // Update local user state to clear mustChangePassword
    const current = this._user();
    if (current) {
      this._user.set({ ...current, mustChangePassword: false });
      localStorage.setItem(STORAGE_KEY_USER, JSON.stringify({ ...current, mustChangePassword: false }));
    }
  }

  /**
   * Clear the current error.
   */
  clearError(): void {
    this._error.set(null);
  }

  // ─── Builtin Auth Helpers ─────────────────────────────────────

  private async builtinSignUp(email: string, password: string): Promise<AuthUser> {
    interface SignUpResponse {
      uid: string;
      email: string;
      email_verified: boolean;
      display_name: string | null;
    }

    const url = `${this.env.apiBaseUrl}/api/v1/auth/signup`;
    const response = await firstValueFrom(
      this.http.post<SignUpResponse>(url, { email, password })
    );

    // After signup, auto-login
    return this.builtinSignIn(email, password);
  }

  private async builtinSignIn(email: string, password: string): Promise<AuthUser> {
    interface LoginResponse {
      idToken: string;
      refreshToken: string;
      expiresIn: number;
    }

    const url = `${this.env.apiBaseUrl}/api/v1/auth/login`;
    const response = await firstValueFrom(
      this.http.post<LoginResponse>(url, { email, password })
    );

    // Store JWT
    localStorage.setItem(STORAGE_KEY_TOKEN, response.idToken);

    // Decode user info from JWT payload
    const payload = JSON.parse(atob(response.idToken.split('.')[1]));

    const authUser: AuthUser = {
      uid: payload.sub,
      email: payload.email || email,
      displayName: payload.display_name || payload.email || email,
      photoURL: null,
      emailVerified: true,
      role: (payload.role as UserRole) || null,
      tenantId: payload.tenant_id || this.env.tenantId || null,
      isAnonymous: false,
      mustChangePassword: false,
    };

    localStorage.setItem(STORAGE_KEY_USER, JSON.stringify(authUser));
    this._user.set(authUser);
    return authUser;
  }

  // ─── Firebase Auth Helpers ────────────────────────────────────

  private async firebaseSignUp(email: string, password: string): Promise<AuthUser> {
    const {
      createUserWithEmailAndPassword,
      EmailAuthProvider,
      linkWithCredential,
    } = await import('firebase/auth');

    // If currently anonymous, link credentials to upgrade the account
    if (this.firebaseAuth.currentUser?.isAnonymous) {
      const credential = EmailAuthProvider.credential(email, password);
      const result = await linkWithCredential(this.firebaseAuth.currentUser, credential);
      const authUser = await this.mapFirebaseUser(result.user);
      this._user.set(authUser);
      return authUser;
    }

    const credential = await createUserWithEmailAndPassword(this.firebaseAuth, email, password);
    const authUser = await this.mapFirebaseUser(credential.user);
    this._user.set(authUser);
    return authUser;
  }

  private async firebaseSignIn(email: string, password: string): Promise<AuthUser> {
    const { signInWithEmailAndPassword } = await import('firebase/auth');
    const credential = await signInWithEmailAndPassword(this.firebaseAuth, email, password);
    const authUser = await this.mapFirebaseUser(credential.user);
    this._user.set(authUser);
    return authUser;
  }

  private async mapFirebaseUser(user: any): Promise<AuthUser> {
    const { getIdTokenResult } = await import('firebase/auth');
    let role: UserRole | null = null;
    let tenantId: string | null = null;

    try {
      const tokenResult = await getIdTokenResult(user);
      role = (tokenResult.claims[CLAIM_KEYS.ROLE] as UserRole) ?? null;
      tenantId = (tokenResult.claims[CLAIM_KEYS.TENANT_ID] as string) ?? null;
    } catch {
      // Claims may not be set yet for new users
    }

    return {
      uid: user.uid,
      email: user.email,
      displayName: user.displayName,
      photoURL: user.photoURL,
      emailVerified: user.emailVerified,
      role,
      tenantId,
      isAnonymous: user.isAnonymous,
    };
  }

  // ─── Error Parsing ────────────────────────────────────────────

  private parseError(error: any): string {
    // Handle backend error responses (builtin)
    if (error?.error?.detail) {
      return error.error.detail;
    }
    if (error?.message && !error?.code) {
      return error.message;
    }

    // Handle Firebase error codes
    const code: string = error?.code ?? '';
    const errorMap: Record<string, string> = {
      'auth/email-already-in-use': 'This email is already registered.',
      'auth/invalid-email': 'Please enter a valid email address.',
      'auth/weak-password': 'Password must be at least 6 characters.',
      'auth/user-not-found': 'No account found with this email.',
      'auth/wrong-password': 'Incorrect password.',
      'auth/too-many-requests': 'Too many attempts. Please try again later.',
      'auth/popup-closed-by-user': 'Sign-in popup was closed.',
      'auth/network-request-failed': 'Network error. Please check your connection.',
      'auth/credential-already-in-use': 'This account is already linked to another user.',
    };
    return errorMap[code] ?? 'An unexpected authentication error occurred.';
  }

  /**
   * Cleanup — unsubscribe from auth state listener.
   */
  ngOnDestroy(): void {
    this.unsubscribe?.();
  }
}
