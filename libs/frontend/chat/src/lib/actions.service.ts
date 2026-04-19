/**
 * ActionsService — HTTP client for the Actions Engine (T8 frontend wiring).
 *
 * Sends DSL form submissions to POST /api/v1/actions/execute
 * and returns structured results with follow-up suggestions.
 */
import { HttpClient } from '@angular/common/http';
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
}
