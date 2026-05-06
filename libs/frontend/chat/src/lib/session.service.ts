/**
 * SessionService — facade for session management (T6.12).
 *
 * Delegates to the generated ChatService from @synaptiq/client for standard
 * CRUD operations, and provides custom methods for features not yet in the
 * OpenAPI spec (e.g., pinned views persistence).
 */
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ENVIRONMENT } from '@synaptiq/utils';
import {
  ChatService as ChatApiService,
  type SessionListResponse,
  type SessionResponse,
  type SessionHistoryResponse,
  type SessionSummaryResponse,
  type ConversationTurnResponse,
} from '@synaptiq/client';

// Re-export SDK types for backward compatibility
export type { SessionListResponse, SessionResponse, SessionHistoryResponse };

/** Alias for sidebar list items (mapped from SDK's SessionSummaryResponse). */
export type SessionListItem = SessionSummaryResponse;

/** Alias for history turns (mapped from SDK's ConversationTurnResponse). */
export type SessionHistoryTurn = ConversationTurnResponse;

// ---------------------------------------------------------------------------
// Service
// ---------------------------------------------------------------------------

@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly api = inject(ChatApiService);
  private readonly http = inject(HttpClient);
  private readonly env = inject(ENVIRONMENT);

  /**
   * List all sessions for the current tenant, ordered by most recent.
   */
  async listSessions(limit = 20): Promise<SessionListResponse> {
    return firstValueFrom(
      this.api.listSessions({ limit }),
    );
  }

  /**
   * Create a new session.
   */
  async createSession(sessionId: string): Promise<SessionResponse> {
    return firstValueFrom(
      this.api.createSession({ createSessionRequest: { sessionId } }),
    );
  }

  /**
   * Load conversation history for a session.
   */
  async getHistory(sessionId: string, _limit = 50): Promise<SessionHistoryResponse> {
    return firstValueFrom(
      this.api.getSessionHistory({ sessionId }),
    );
  }

  /**
   * Update session metadata (e.g. title).
   */
  async updateTitle(sessionId: string, title: string): Promise<SessionResponse> {
    return firstValueFrom(
      this.api.updateSession({ sessionId, updateSessionRequest: { title } }),
    );
  }

  /**
   * Delete/reset a session.
   */
  async deleteSession(sessionId: string): Promise<void> {
    await firstValueFrom(
      this.api.deleteSession({ sessionId }),
    );
  }

  // ── P0-B: Pinned views persistence ────────────────────────────────────
  // These endpoints are not yet in the OpenAPI spec — uses direct HTTP.

  /**
   * Save pinned views to the backend session document.
   */
  async savePinnedViews(sessionId: string, views: unknown[]): Promise<void> {
    const baseUrl = `${this.env.apiBaseUrl}/api/v1/chat`;
    await firstValueFrom(
      this.http.put(`${baseUrl}/sessions/${sessionId}/pinned_views`, { views }),
    );
  }

  /**
   * Load pinned views from the backend session document.
   */
  async loadPinnedViews(sessionId: string): Promise<unknown[]> {
    const baseUrl = `${this.env.apiBaseUrl}/api/v1/chat`;
    const res = await firstValueFrom(
      this.http.get<{ views: unknown[] }>(`${baseUrl}/sessions/${sessionId}/pinned_views`),
    );
    return res.views ?? [];
  }
}
