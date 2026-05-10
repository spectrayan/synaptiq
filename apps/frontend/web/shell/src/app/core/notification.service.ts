import { Injectable, inject, signal, OnDestroy } from '@angular/core';
import { Subject, takeUntil, mergeMap, EMPTY, catchError } from 'rxjs';
import { NotificationsService as NotificationsApiService } from '@synaptiq/client';
import { SseService } from './sse.service';
import { environment } from '../../../environments/environment';

/** Notification item (frontend model) */
export interface Notification {
  id: string;
  userId: string;
  tenantId: string;
  type: string;
  title: string;
  message: string;
  icon: string;
  read: boolean;
  createdAt: string;
}

/** SSE event payload pushed from the backend */
interface SseNotificationEvent {
  eventType?: string;
  _title?: string;
  _message?: string;
  _icon?: string;
  [key: string]: unknown;
}

/**
 * Notification service — manages in-app notifications with real-time SSE push.
 *
 * Connects to the SSE topic `tenant-{tenantId}` to receive real-time events.
 * When an SSE event arrives, it reloads the notification list from the API
 * (the backend already persisted the notification during fan-out).
 *
 * Uses the generated `@synaptiq/client` NotificationsService for all API calls.
 * Uses Angular signals for reactive state — no NgRx needed.
 */
@Injectable({ providedIn: 'root' })
export class NotificationService implements OnDestroy {
  private readonly api = inject(NotificationsApiService);
  private readonly sse = inject(SseService);
  private readonly destroy$ = new Subject<void>();

  // ── State (signals) ──
  private readonly _items = signal<Notification[]>([]);
  private readonly _unreadCount = signal(0);
  private readonly _hasMore = signal(false);
  private readonly _loading = signal(false);

  // ── Public selectors ──
  readonly items = this._items.asReadonly();
  readonly unreadCount = this._unreadCount.asReadonly();
  readonly hasMore = this._hasMore.asReadonly();
  readonly loading = this._loading.asReadonly();

  private currentTenantId = '';

  /**
   * Initialize SSE connection and load initial notifications.
   * Call this once when the user logs in / app bootstraps.
   */
  connectAndLoad(tenantId: string): void {
    this.currentTenantId = tenantId;

    // Load persisted notifications immediately
    this.loadNotifications(tenantId);
    this.loadUnreadCount(tenantId);

    // Connect to SSE topic for real-time push
    const topic = `tenant-${tenantId}`;
    const eventTypes = [
      'workflow.generated', 'workflow.completed', 'workflow.failed',
      'workflow.shared', 'data.imported', 'tenant.created',
      'chat.session_created', 'llm.error',
    ];

    this.sse.connect<SseNotificationEvent>(topic, eventTypes).pipe(
      takeUntil(this.destroy$),
      mergeMap(() => {
        // Reload from API — the backend already persisted the notification
        this.loadNotifications(tenantId);
        this.loadUnreadCount(tenantId);
        return EMPTY;
      }),
    ).subscribe();
  }

  /** Disconnect SSE and clear state. */
  disconnect(): void {
    this.sse.disconnect();
    this._items.set([]);
    this._unreadCount.set(0);
  }

  // ── API calls (via generated SDK) ──

  loadNotifications(tenantId: string): void {
    this._loading.set(true);
    this.api.listNotifications({ xTenantID: tenantId, limit: 20 }).pipe(
      catchError(() => { this._loading.set(false); return EMPTY; }),
    ).subscribe(res => {
      this._items.set((res.items ?? []).map(item => ({
        id: item.id ?? '',
        userId: item.userId ?? '',
        tenantId: item.tenantId ?? '',
        type: item.type ?? '',
        title: item.title ?? '',
        message: item.message ?? '',
        icon: item.icon ?? 'notifications',
        read: item.read ?? false,
        createdAt: item.createdAt ?? '',
      })));
      this._hasMore.set(res.hasMore ?? false);
      this._loading.set(false);
    });
  }

  loadUnreadCount(tenantId: string): void {
    this.api.getUnreadNotificationCount({ xTenantID: tenantId }).pipe(
      catchError(() => EMPTY),
    ).subscribe(res => {
      this._unreadCount.set(res.unread ?? 0);
    });
  }

  markAsRead(id: string): void {
    // Optimistic update
    this._items.update(items =>
      items.map(n => n.id === id ? { ...n, read: true } : n)
    );
    this._unreadCount.update(c => Math.max(0, c - 1));

    this.api.markNotificationAsRead({ id }).pipe(
      catchError(() => EMPTY),
    ).subscribe();
  }

  markAllAsRead(): void {
    this._items.update(items => items.map(n => ({ ...n, read: true })));
    this._unreadCount.set(0);

    this.api.markAllNotificationsRead({ xTenantID: this.currentTenantId }).pipe(
      catchError(() => EMPTY),
    ).subscribe();
  }

  dismiss(id: string): void {
    const wasUnread = this._items().find(n => n.id === id)?.read === false;
    this._items.update(items => items.filter(n => n.id !== id));
    if (wasUnread) this._unreadCount.update(c => Math.max(0, c - 1));

    this.api.dismissNotification({ id }).pipe(
      catchError(() => EMPTY),
    ).subscribe();
  }

  clearAll(): void {
    this._items.set([]);
    this._unreadCount.set(0);

    this.api.clearAllNotifications({ xTenantID: this.currentTenantId }).pipe(
      catchError(() => EMPTY),
    ).subscribe();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
