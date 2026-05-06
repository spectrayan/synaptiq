/**
 * ActionsService — facade for the Actions Engine API.
 *
 * Delegates to the generated ActionsService from @synaptiq/client.
 * Re-exports SDK types for backward compatibility.
 */
import { inject, Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import {
  ActionsService as ActionsApiService,
  type ActionResultResponse,
  type ExecuteActionRequest,
  type SavedItemListResponse,
  type SavedItemResponse,
} from '@synaptiq/client';

// Re-export SDK types under legacy aliases for existing consumers
export type { ActionResultResponse, ExecuteActionRequest, SavedItemListResponse, SavedItemResponse } from '@synaptiq/client';

/** Backward-compatible alias */
export type ActionResponse = ActionResultResponse & {
  suggestions?: Array<{ label: string; prompt: string }>;
};

/** Backward-compatible alias */
export interface ActionPayload {
  action: string;
  values?: Record<string, unknown>;
  metadata?: Record<string, unknown>;
}

@Injectable({ providedIn: 'root' })
export class ActionsService {
  private readonly api = inject(ActionsApiService);

  /**
   * Execute a DSL form action on the backend.
   */
  async execute(payload: ActionPayload): Promise<ActionResponse> {
    const result = await firstValueFrom(
      this.api.executeAction({
        executeActionRequest: {
          action: payload.action,
          sessionId: (payload.metadata?.['sessionId'] ?? payload.metadata?.['session_id']) as string | undefined,
        },
      }),
    );
    return {
      success: result.success ?? false,
      message: result.message ?? '',
      data: (result.data ?? {}) as Record<string, unknown>,
      suggestions: [],
    };
  }

  /**
   * Retrieve saved/bookmarked items for a session.
   */
  async getSavedItems(sessionId: string): Promise<SavedItemListResponse> {
    return firstValueFrom(
      this.api.listSavedItems({ sessionId }),
    );
  }

  /**
   * Remove a saved item from the user's list.
   */
  async deleteSavedItem(itemId: string, sessionId: string): Promise<void> {
    await firstValueFrom(
      this.api.removeSavedItem({ itemId, sessionId }),
    );
  }
}
