/**
 * SessionService — HTTP client for session management (T6.12).
 *
 * Provides CRUD operations for chat sessions via the backend API:
 *   - List sessions (sidebar history)
 *   - Create session
 *   - Load session history
 *   - Update session title
 *   - Delete session
 */
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ENVIRONMENT } from '@synaptiq/utils';

// ---------------------------------------------------------------------------
// Response types
// ---------------------------------------------------------------------------

export interface SessionListItem {
  readonly session_id: string;
  readonly title: string;
  readonly turn_count: number;
  readonly created_at: string;
  readonly updated_at: string;
}

export interface SessionListResponse {
  readonly sessions: SessionListItem[];
  readonly total: number;
}

export interface SessionHistoryTurn {
  readonly role: 'user' | 'assistant';
  readonly content: string;
  readonly components?: unknown[];
  readonly timestamp?: string;
}

export interface SessionHistoryResponse {
  readonly session_id: string;
  readonly turns: SessionHistoryTurn[];
  readonly total: number;
}

export interface SessionResponse {
  readonly session_id: string;
  readonly tenant_id: string;
  readonly created_at: string;
  readonly turn_count: number;
  readonly title: string;
  readonly updated_at?: string;
}

// ---------------------------------------------------------------------------
// Service
// ---------------------------------------------------------------------------

@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly http = inject(HttpClient);
  private readonly env = inject(ENVIRONMENT);
  private readonly baseUrl = `${this.env.apiBaseUrl}/api/v1/chat`;

  /**
   * List all sessions for the current tenant, ordered by most recent.
   */
  async listSessions(limit = 20): Promise<SessionListResponse> {
    return firstValueFrom(
      this.http.get<SessionListResponse>(`${this.baseUrl}/sessions`, {
        params: { limit: limit.toString() },
      }),
    );
  }

  /**
   * Create a new session.
   */
  async createSession(sessionId: string): Promise<SessionResponse> {
    return firstValueFrom(
      this.http.post<SessionResponse>(`${this.baseUrl}/sessions`, {
        session_id: sessionId,
      }),
    );
  }

  /**
   * Load conversation history for a session.
   */
  async getHistory(sessionId: string, limit = 50): Promise<SessionHistoryResponse> {
    return firstValueFrom(
      this.http.get<SessionHistoryResponse>(
        `${this.baseUrl}/sessions/${sessionId}/history`,
        { params: { limit: limit.toString() } },
      ),
    );
  }

  /**
   * Update session metadata (e.g. title).
   */
  async updateTitle(sessionId: string, title: string): Promise<SessionResponse> {
    return firstValueFrom(
      this.http.patch<SessionResponse>(
        `${this.baseUrl}/sessions/${sessionId}`,
        { title },
      ),
    );
  }

  /**
   * Delete/reset a session.
   */
  async deleteSession(sessionId: string): Promise<void> {
    return firstValueFrom(
      this.http.delete<void>(`${this.baseUrl}/sessions/${sessionId}`),
    );
  }

  // ── P0-B: Pinned views persistence ────────────────────────────────────

  /**
   * Save pinned views to the backend session document.
   */
  async savePinnedViews(sessionId: string, views: unknown[]): Promise<void> {
    await firstValueFrom(
      this.http.put(`${this.baseUrl}/sessions/${sessionId}/pinned_views`, { views }),
    );
  }

  /**
   * Load pinned views from the backend session document.
   */
  async loadPinnedViews(sessionId: string): Promise<unknown[]> {
    const res = await firstValueFrom(
      this.http.get<{ views: unknown[] }>(`${this.baseUrl}/sessions/${sessionId}/pinned_views`),
    );
    return res.views ?? [];
  }
}
