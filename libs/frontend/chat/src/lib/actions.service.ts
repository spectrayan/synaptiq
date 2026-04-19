/**
 * ActionsService — HTTP client for the Actions Engine (Phase 8).
 *
 * Sends DSL form submissions to POST /api/v1/actions/execute
 * and provides saved items management (T8.1, T8.2).
 */
import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ENVIRONMENT } from '@synaptiq/utils';

export interface ActionResponse {
  success: boolean;
  message: string;
  data: Record<string, unknown>;
  suggestions: Array<{ label: string; prompt: string }>;
}

export interface ActionPayload {
  action: string;
  values: Record<string, unknown>;
  metadata?: Record<string, unknown>;
}

export interface SavedItemOut {
  item_id: string;
  session_id: string;
  item_snapshot: Record<string, unknown>;
  created_at: string | null;
}

export interface SavedItemsResponse {
  items: SavedItemOut[];
  total: number;
}

@Injectable({ providedIn: 'root' })
export class ActionsService {
  private readonly http = inject(HttpClient);
  private readonly env = inject(ENVIRONMENT);
  private readonly baseUrl = `${this.env.apiBaseUrl}/api/v1/actions`;

  /**
   * Execute a DSL form action on the backend.
   *
   * Maps directly to the backend's ``POST /api/v1/actions/execute``.
   */
  async execute(payload: ActionPayload): Promise<ActionResponse> {
    return firstValueFrom(
      this.http.post<ActionResponse>(`${this.baseUrl}/execute`, payload),
    );
  }

  /**
   * Retrieve saved/bookmarked items for a session (T8.2).
   */
  async getSavedItems(sessionId: string): Promise<SavedItemsResponse> {
    const params = new HttpParams().set('session_id', sessionId);
    return firstValueFrom(
      this.http.get<SavedItemsResponse>(`${this.baseUrl}/saved_items`, { params }),
    );
  }

  /**
   * Remove a saved item from the user's list.
   */
  async deleteSavedItem(itemId: string, sessionId: string): Promise<{ success: boolean; message: string }> {
    const params = new HttpParams().set('session_id', sessionId);
    return firstValueFrom(
      this.http.delete<{ success: boolean; message: string }>(
        `${this.baseUrl}/saved_items/${encodeURIComponent(itemId)}`,
        { params },
      ),
    );
  }
}
