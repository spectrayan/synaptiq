/**
 * Login Page Component (T1.7) — Email/password + Google SSO
 *
 * Standalone component with Material Design form controls.
 * Uses signals-based AuthService for reactive state management.
 *
 * Supports both built-in auth (JWT) and Firebase auth providers.
 * When `mustChangePassword` is true, shows a change-password overlay.
 */
import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { AuthService } from '@synaptiq/auth';
import { ENVIRONMENT } from '@synaptiq/utils';

@Component({
  selector: 'sq-login',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,

    MatInputModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatDividerModule,
  ],
  template: `
    <div class="login-container">
      <div class="login-card">
        <!-- Logo / Header -->
        <div class="login-header">
          <div class="logo-mark">
            <svg viewBox="0 0 32 32" width="40" height="40" fill="none">
              <rect width="32" height="32" rx="8" fill="var(--sq-primary)" />
              <path d="M10 22V10l6 6 6-6v12" stroke="white" stroke-width="2.5" stroke-linecap="round"
                stroke-linejoin="round" />
            </svg>
          </div>
          <h1 class="brand-name">Synaptiq</h1>
          <p class="brand-tagline">AI-native data application platform</p>
        </div>

        <!-- Error Banner -->
        @if (auth.error()) {
          <div class="error-banner" role="alert">
            <mat-icon>error_outline</mat-icon>
            <span>{{ auth.error() }}</span>
            <button mat-icon-button (click)="auth.clearError()" aria-label="Dismiss error">
              <mat-icon>close</mat-icon>
            </button>
          </div>
        }

        <!-- Success Banner -->
        @if (successMessage()) {
          <div class="success-banner" role="status">
            <mat-icon>check_circle</mat-icon>
            <span>{{ successMessage() }}</span>
          </div>
        }

        <!-- ═══ Change Password Overlay ═══ -->
        @if (showChangePassword()) {
          <div class="change-password-section">
            <div class="change-pw-header">
              <mat-icon class="lock-icon">lock_reset</mat-icon>
              <h2>Change Your Password</h2>
              <p>Your account requires a password change before continuing.</p>
            </div>

            <form class="auth-form" (ngSubmit)="onChangePassword()" #changePwForm="ngForm">
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Current Password</mat-label>
                <input
                  matInput
                  type="password"
                  id="current-password-input"
                  [(ngModel)]="currentPassword"
                  name="currentPassword"
                  required
                  autocomplete="current-password"
                />
                <mat-icon matPrefix>lock</mat-icon>
              </mat-form-field>

              <mat-form-field appearance="outline" class="full-width">
                <mat-label>New Password</mat-label>
                <input
                  matInput
                  [type]="showNewPassword() ? 'text' : 'password'"
                  id="new-password-input"
                  [(ngModel)]="newPassword"
                  name="newPassword"
                  required
                  minlength="6"
                  autocomplete="new-password"
                  placeholder="At least 6 characters"
                />
                <mat-icon matPrefix>lock_outline</mat-icon>
                <button
                  mat-icon-button
                  matSuffix
                  type="button"
                  (click)="showNewPassword.update(v => !v)"
                  [attr.aria-label]="showNewPassword() ? 'Hide password' : 'Show password'"
                >
                  <mat-icon>{{ showNewPassword() ? 'visibility_off' : 'visibility' }}</mat-icon>
                </button>
              </mat-form-field>

              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Confirm New Password</mat-label>
                <input
                  matInput
                  type="password"
                  id="confirm-password-input"
                  [(ngModel)]="confirmPassword"
                  name="confirmPassword"
                  required
                  minlength="6"
                  autocomplete="new-password"
                />
                <mat-icon matPrefix>lock_outline</mat-icon>
              </mat-form-field>

              @if (newPassword && confirmPassword && newPassword !== confirmPassword) {
                <p class="password-mismatch">Passwords do not match</p>
              }

              <button
                mat-flat-button
                color="primary"
                type="submit"
                id="change-password-btn"
                class="full-width submit-btn"
                [disabled]="changingPassword() || !changePwForm.valid || newPassword !== confirmPassword"
              >
                @if (changingPassword()) {
                  <mat-spinner diameter="20" />
                } @else {
                  Update Password
                }
              </button>
            </form>
          </div>
        } @else {
          <!-- ═══ Normal Login / Signup Form ═══ -->

          <!-- Toggle: Sign In / Sign Up -->
          <div class="mode-toggle">
            <button
              [class.active]="mode() === 'signin'"
              (click)="mode.set('signin')"
              id="tab-signin"
            >
              Sign In
            </button>
            <button
              [class.active]="mode() === 'signup'"
              (click)="mode.set('signup')"
              id="tab-signup"
            >
              Sign Up
            </button>
          </div>

          <!-- Email / Password Form -->
          <form class="auth-form" (ngSubmit)="onSubmit()" #authForm="ngForm">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Email</mat-label>
              <input
                matInput
                type="email"
                id="email-input"
                [(ngModel)]="email"
                name="email"
                required
                email
                autocomplete="email"
                placeholder="you@company.com"
              />
              <mat-icon matPrefix>email</mat-icon>
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Password</mat-label>
              <input
                matInput
                [type]="showPassword() ? 'text' : 'password'"
                id="password-input"
                [(ngModel)]="password"
                name="password"
                required
                minlength="6"
                autocomplete="current-password"
                placeholder="••••••••"
              />
              <mat-icon matPrefix>lock</mat-icon>
              <button
                mat-icon-button
                matSuffix
                type="button"
                (click)="showPassword.update(v => !v)"
                [attr.aria-label]="showPassword() ? 'Hide password' : 'Show password'"
              >
                <mat-icon>{{ showPassword() ? 'visibility_off' : 'visibility' }}</mat-icon>
              </button>
            </mat-form-field>

            <button
              mat-flat-button
              color="primary"
              type="submit"
              id="submit-btn"
              class="full-width submit-btn"
              [disabled]="auth.isLoading() || !authForm.valid"
            >
              @if (auth.isLoading()) {
                <mat-spinner diameter="20" />
              } @else {
                {{ mode() === 'signin' ? 'Sign In' : 'Create Account' }}
              }
            </button>
          </form>

          <!-- Google SSO (only for Firebase provider) -->
          @if (!isBuiltinAuth) {
            <mat-divider />

            <div class="social-section">
              <p class="divider-text">or continue with</p>
              <button
                mat-stroked-button
                class="full-width google-btn"
                id="google-signin-btn"
                (click)="signInWithGoogle()"
                [disabled]="auth.isLoading()"
              >
                <svg viewBox="0 0 24 24" width="20" height="20" class="google-icon">
                  <path fill="#4285F4"
                    d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 0 1-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z" />
                  <path fill="#34A853"
                    d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" />
                  <path fill="#FBBC05"
                    d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18A10.97 10.97 0 0 0 1 12c0 1.77.42 3.45 1.18 4.93l2.85-2.22.81-.62z" />
                  <path fill="#EA4335"
                    d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" />
                </svg>
                <span>Google</span>
              </button>
            </div>
          }

          <!-- Builtin auth hint -->
          @if (isBuiltinAuth && mode() === 'signin') {
            <div class="builtin-hint">
              <mat-icon>info_outline</mat-icon>
              <span>Default: <code>admin&#64;synaptiq.dev</code> / <code>admin</code></span>
            </div>
          }
        }

        <p class="footer-text">
          By continuing, you agree to Synaptiq's Terms of Service and Privacy Policy.
        </p>
      </div>
    </div>
  `,
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly env = inject(ENVIRONMENT);

  mode = signal<'signin' | 'signup'>('signin');
  email = '';
  password = '';
  showPassword = signal(false);

  // Change password state
  showChangePassword = signal(false);
  currentPassword = '';
  newPassword = '';
  confirmPassword = '';
  showNewPassword = signal(false);
  changingPassword = signal(false);
  successMessage = signal<string | null>(null);

  /** Whether builtin auth is active */
  get isBuiltinAuth(): boolean {
    return this.env.authProvider === 'builtin';
  }

  async onSubmit(): Promise<void> {
    if (!this.email || !this.password) return;

    try {
      if (this.mode() === 'signup') {
        await this.auth.signUp(this.email, this.password);
      } else {
        await this.auth.signIn(this.email, this.password);
      }

      // Check if user must change password
      if (this.auth.mustChangePassword()) {
        this.showChangePassword.set(true);
        this.currentPassword = this.password; // Pre-fill current password
        return;
      }

      this.router.navigate(['/']);
    } catch {
      // Error is already captured in auth.error() signal
    }
  }

  async onChangePassword(): Promise<void> {
    if (!this.newPassword || this.newPassword !== this.confirmPassword) return;

    this.changingPassword.set(true);
    this.successMessage.set(null);

    try {
      await this.auth.changePassword(this.currentPassword, this.newPassword);
      this.successMessage.set('Password changed successfully! Redirecting...');
      this.showChangePassword.set(false);

      // Brief delay then navigate
      setTimeout(() => {
        this.router.navigate(['/']);
      }, 1500);
    } catch (e: any) {
      // Error is captured in auth.error()
    } finally {
      this.changingPassword.set(false);
    }
  }

  async signInWithGoogle(): Promise<void> {
    try {
      await this.auth.signInWithGoogle();
      this.router.navigate(['/']);
    } catch {
      // Error is already captured in auth.error() signal
    }
  }
}
