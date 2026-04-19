/**
 * Admin Shell — redirects to the chat interface in admin mode.
 *
 * Per the architecture decision, the admin dashboard uses the same
 * ChatShellComponent as end-users. The backend detects admin role
 * from Firebase custom claims and injects admin-specific prompts
 * (schema management, analytics, onboarding).
 *
 * This component serves as the landing page for `/admin`, showing
 * a brief admin context header before rendering the shared chat.
 */
import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '@synaptiq/auth';

@Component({
  selector: 'sq-admin-shell',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule],
  template: `
    <div class="admin-redirect">
      <div class="admin-redirect-card sq-glass">
        <div class="admin-icon">⚙</div>
        <h2>Admin Console</h2>
        <p class="admin-info">
          Signed in as <strong>{{ auth.currentUser()?.email }}</strong>
          <span class="role-chip">{{ auth.userRole() }}</span>
        </p>
        <p class="admin-desc">
          Your admin dashboard lives inside the same chat interface.
          The AI will automatically switch to admin mode and help you
          manage schemas, import products, and view analytics.
        </p>
        <button class="admin-launch" (click)="goToChat()">
          <mat-icon>chat</mat-icon>
          Open Admin Chat
        </button>
      </div>
    </div>
  `,
  styles: [`
    .admin-redirect {
      display: grid;
      place-items: center;
      min-height: 100vh;
      background: var(--sq-surface-dim, #0a0a0f);
      padding: 2rem;
    }
    .admin-redirect-card {
      max-width: 480px;
      width: 100%;
      padding: 2.5rem;
      border-radius: 1rem;
      text-align: center;
      background: var(--sq-surface-container, #1a1a2e);
      border: 1px solid var(--sq-outline-variant, rgba(255,255,255,0.08));
    }
    .admin-icon {
      font-size: 3rem;
      margin-bottom: 1rem;
      filter: drop-shadow(0 0 12px rgba(179, 157, 219, 0.5));
    }
    h2 {
      margin: 0 0 0.75rem;
      font-size: 1.5rem;
      font-weight: 700;
      color: var(--sq-on-surface, #f0f0f5);
    }
    .admin-info {
      font-size: 0.875rem;
      color: var(--sq-on-surface-variant, rgba(255,255,255,0.6));
      margin-bottom: 0.5rem;
    }
    .role-chip {
      display: inline-block;
      padding: 0.15rem 0.5rem;
      border-radius: 0.375rem;
      background: rgba(179, 157, 219, 0.15);
      color: #b39ddb;
      font-size: 0.75rem;
      font-weight: 500;
      margin-left: 0.5rem;
    }
    .admin-desc {
      font-size: 0.875rem;
      line-height: 1.6;
      color: var(--sq-on-surface-variant, rgba(255,255,255,0.6));
      margin-bottom: 1.5rem;
    }
    .admin-launch {
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.75rem 1.5rem;
      border: none;
      border-radius: 0.5rem;
      background: var(--sq-primary, #b39ddb);
      color: var(--sq-on-primary, #1a1a2e);
      font-size: 0.9375rem;
      font-weight: 600;
      cursor: pointer;
      font-family: inherit;
      transition: transform 0.15s, box-shadow 0.15s;
    }
    .admin-launch:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 16px rgba(179, 157, 219, 0.3);
    }
    .admin-launch mat-icon {
      font-size: 20px;
      width: 20px;
      height: 20px;
    }
  `],
})
export class AdminShellComponent implements OnInit {
  readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  ngOnInit(): void {
    // Auto-redirect to chat after a brief delay if user is admin
    // The chat shell detects admin mode via AuthService.isAdmin()
  }

  goToChat(): void {
    this.router.navigate(['/chat']);
  }
}
