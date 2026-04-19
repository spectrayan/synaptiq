/**
 * AuthService — signals-based Firebase authentication (T1.5)
 *
 * Provides reactive auth state via Angular signals:
 *   - currentUser()  → Firebase User | null
 *   - isLoggedIn()   → boolean
 *   - userRole()     → UserRole | null
 *   - tenantId()     → string | null
 *   - isLoading()    → boolean
 *
 * All auth operations return Promises and automatically update signals.
 * No NgRx needed — signals provide fine-grained reactivity.
 */
import { computed, effect, Injectable, signal, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import {
  initializeApp,
  FirebaseApp,
} from 'firebase/app';
import {
  getAuth,
  Auth,
  User,
  onAuthStateChanged,
  signInWithEmailAndPassword,
  createUserWithEmailAndPassword,
  signInWithPopup,
  GoogleAuthProvider,
  signOut,
  getIdTokenResult,
  IdTokenResult,
  Unsubscribe,
} from 'firebase/auth';
import { ENVIRONMENT } from '@synaptiq/utils';
import { CLAIM_KEYS, type UserRole } from '@synaptiq/constants';

export interface AuthUser {
  uid: string;
  email: string | null;
  displayName: string | null;
  photoURL: string | null;
  emailVerified: boolean;
  role: UserRole | null;
  tenantId: string | null;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly router = inject(Router);
  private readonly env = inject(ENVIRONMENT);

  private firebaseApp: FirebaseApp | null = null;
  private auth: Auth | null = null;
  private unsubscribe: Unsubscribe | null = null;

  // ─── Reactive State (signals) ──────────────────────────────────
  private readonly _user = signal<AuthUser | null>(null);
  private readonly _isLoading = signal(true);
  private readonly _error = signal<string | null>(null);

  /** Current authenticated user */
  readonly currentUser = this._user.asReadonly();

  /** Whether a user is currently logged in */
  readonly isLoggedIn = computed(() => this._user() !== null);

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

  constructor() {
    if (isPlatformBrowser(this.platformId)) {
      this.initializeFirebase();
    } else {
      // SSR — skip Firebase init
      this._isLoading.set(false);
    }
  }

  // ─── Firebase Initialization ───────────────────────────────────

  private initializeFirebase(): void {
    try {
      this.firebaseApp = initializeApp(this.env.firebase);
      this.auth = getAuth(this.firebaseApp);

      // Listen to auth state changes
      this.unsubscribe = onAuthStateChanged(this.auth, async (user) => {
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

  // ─── Auth Operations ───────────────────────────────────────────

  /**
   * Sign up with email and password.
   */
  async signUp(email: string, password: string): Promise<AuthUser> {
    this.clearError();
    this._isLoading.set(true);

    try {
      const auth = this.getAuth();
      const credential = await createUserWithEmailAndPassword(auth, email, password);
      const authUser = await this.mapFirebaseUser(credential.user);
      this._user.set(authUser);
      return authUser;
    } catch (e: any) {
      const msg = this.parseFirebaseError(e);
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
      const auth = this.getAuth();
      const credential = await signInWithEmailAndPassword(auth, email, password);
      const authUser = await this.mapFirebaseUser(credential.user);
      this._user.set(authUser);
      return authUser;
    } catch (e: any) {
      const msg = this.parseFirebaseError(e);
      this._error.set(msg);
      throw new Error(msg);
    } finally {
      this._isLoading.set(false);
    }
  }

  /**
   * Sign in with Google OAuth popup.
   */
  async signInWithGoogle(): Promise<AuthUser> {
    this.clearError();
    this._isLoading.set(true);

    try {
      const auth = this.getAuth();
      const provider = new GoogleAuthProvider();
      const credential = await signInWithPopup(auth, provider);
      const authUser = await this.mapFirebaseUser(credential.user);
      this._user.set(authUser);
      return authUser;
    } catch (e: any) {
      const msg = this.parseFirebaseError(e);
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
      const auth = this.getAuth();
      await signOut(auth);
      this._user.set(null);
      this.router.navigate(['/login']);
    } catch (e: any) {
      this._error.set('Failed to sign out');
    }
  }

  /**
   * Get the current Firebase ID token for API calls.
   */
  async getIdToken(): Promise<string | null> {
    if (!this.auth?.currentUser) return null;
    return this.auth.currentUser.getIdToken();
  }

  /**
   * Force refresh the ID token (e.g. after custom claims update).
   */
  async refreshToken(): Promise<void> {
    if (!this.auth?.currentUser) return;
    await this.auth.currentUser.getIdToken(/* forceRefresh */ true);
    const authUser = await this.mapFirebaseUser(this.auth.currentUser);
    this._user.set(authUser);
  }

  /**
   * Clear the current error.
   */
  clearError(): void {
    this._error.set(null);
  }

  // ─── Helpers ───────────────────────────────────────────────────

  private getAuth(): Auth {
    if (!this.auth) {
      throw new Error('Auth not initialized — are you running in a browser?');
    }
    return this.auth;
  }

  private async mapFirebaseUser(user: User): Promise<AuthUser> {
    let role: UserRole | null = null;
    let tenantId: string | null = null;

    try {
      const tokenResult: IdTokenResult = await getIdTokenResult(user);
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
    };
  }

  private parseFirebaseError(error: any): string {
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
