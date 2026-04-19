/**
 * Admin Shell — placeholder for Phase 11 admin dashboard.
 * Protected by authGuard with role-based access.
 */
import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { AuthService } from '@synaptiq/auth';

@Component({
  selector: 'sq-admin-shell',
  standalone: true,
  imports: [CommonModule, RouterOutlet],
  template: `
    <div class="admin-shell">
      <header class="admin-header">
        <h1>Synaptiq Admin</h1>
        <div class="user-info">
          <span>{{ auth.currentUser()?.email }}</span>
          <span class="role-badge">{{ auth.userRole() }}</span>
        </div>
      </header>
      <main class="admin-content">
        <p>Admin dashboard — coming in Phase 11.</p>
        <router-outlet />
      </main>
    </div>
  `,
  styles: [`
    .admin-shell {
      min-height: 100vh;
      background: var(--sq-bg-primary, #0a0a0f);
      color: var(--sq-text-primary, #f0f0f5);
    }
    .admin-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 1rem 2rem;
      border-bottom: 1px solid rgba(255, 255, 255, 0.08);
    }
    .admin-header h1 {
      font-size: 1.25rem;
      font-weight: 600;
      margin: 0;
    }
    .user-info {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      font-size: 0.875rem;
      color: var(--sq-text-secondary, rgba(255, 255, 255, 0.6));
    }
    .role-badge {
      padding: 0.25rem 0.5rem;
      border-radius: 0.375rem;
      background: rgba(99, 102, 241, 0.15);
      color: #a5b4fc;
      font-size: 0.75rem;
      font-weight: 500;
    }
    .admin-content {
      padding: 2rem;
    }
  `],
})
export class AdminShellComponent {
  readonly auth = inject(AuthService);
}
