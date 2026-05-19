/**
 * ChatStreamService — SSE streaming client for the Synaptiq chat API (T9.1).
 *
 * Connects to `POST /api/v1/chat/message` and streams Server-Sent Events:
 *   - `token`     → individual text tokens (typewriter effect)
 *   - `component` → parsed DSL ComponentSpec JSON
 *   - `status`    → progress / fallback messages
 *   - `done`      → final metadata (turn_id, token counts)
 *   - `error`     → error messages
 *
 * Uses `fetch()` with `ReadableStream` instead of `EventSource` because
 * `EventSource` only supports GET — our chat endpoint is POST.
 *
 * All REST endpoints (sessions CRUD, history) are handled by the generated
 * ChatService from @synaptiq/client.
 */
import { inject, Injectable, NgZone } from '@angular/core';
import { ENVIRONMENT } from '@synaptiq/utils';
import { ComponentSpec } from '@synaptiq/constants';

// Re-export the generated SDK service and types
export {
  ChatService as ChatApiService,
  type ChatMessageRequest,
  type SessionResponse,
  type SessionListResponse,
  type SessionHistoryResponse,
} from '@synaptiq/client';

// ---------------------------------------------------------------------------
// SSE Event Types (not in SDK — SSE streaming is custom)
// ---------------------------------------------------------------------------

export interface SseTokenEvent {
  readonly type: 'token';
  readonly text: string;
}

export interface SseComponentEvent {
  readonly type: 'component';
  readonly component: ComponentSpec;
}

export interface SseStatusEvent {
  readonly type: 'status';
  readonly message: string;
}

export interface SseDoneEvent {
  readonly type: 'done';
  readonly turnId: string;
  readonly tokensInput: number;
  readonly tokensOutput: number;
}

export interface SseErrorEvent {
  readonly type: 'error';
  readonly message: string;
}

export interface SseTextReplaceEvent {
  readonly type: 'text_replace';
  readonly text: string;
}

export interface SseStepStartEvent {
  readonly type: 'step_start';
  readonly stepId: string;
  readonly action: string;
  readonly description: string;
}

export interface SseStepCompleteEvent {
  readonly type: 'step_complete';
  readonly stepId: string;
  readonly action: string;
}

export type ChatSseEvent =
  | SseTokenEvent
  | SseComponentEvent
  | SseStatusEvent
  | SseDoneEvent
  | SseErrorEvent
  | SseTextReplaceEvent
  | SseStepStartEvent
  | SseStepCompleteEvent;

// ---------------------------------------------------------------------------
// Chat request payload (for SSE stream — not the same as SDK's ChatMessageRequest)
// ---------------------------------------------------------------------------

export interface ChatRequest {
  readonly sessionId: string;
  readonly message: string;
  readonly modelOverride?: string;
  readonly background?: boolean;
  /** Optional KB category IDs for RAG-scoped search. */
  readonly knowledgeBaseIds?: string[];
}

// ---------------------------------------------------------------------------
// Callback-based stream handler
// ---------------------------------------------------------------------------

export interface ChatStreamCallbacks {
  onToken?: (text: string) => void;
  onComponent?: (component: ComponentSpec) => void;
  onStatus?: (message: string) => void;
  onTextReplace?: (text: string) => void;
  onStepStart?: (event: SseStepStartEvent) => void;
  onStepComplete?: (event: SseStepCompleteEvent) => void;
  onDone?: (event: SseDoneEvent) => void;
  onError?: (message: string) => void;
}

// ---------------------------------------------------------------------------
// Service — SSE streaming only
// ---------------------------------------------------------------------------

@Injectable({ providedIn: 'root' })
export class ChatService {
  private readonly env = inject(ENVIRONMENT);
  private readonly zone = inject(NgZone);
  private readonly baseUrl = `${this.env.apiBaseUrl}/api/v1/chat`;

  /** Active AbortController for the current stream (allows cancellation). */
  private activeController: AbortController | null = null;

  /**
   * Stream a chat response from the backend via SSE over POST.
   */
  async streamMessage(
    request: ChatRequest,
    callbacks: ChatStreamCallbacks,
    authToken?: string,
  ): Promise<void> {
    // Cancel any in-flight stream
    this.abort();

    const controller = new AbortController();
    this.activeController = controller;

    try {
      const headers: Record<string, string> = {
        'Content-Type': 'application/json',
        'Accept': 'text/event-stream',
      };
      if (this.env.tenantId) {
        headers['X-Tenant-ID'] = this.env.tenantId;
      }
      // Auto-retrieve token from localStorage if not explicitly passed
      const token = authToken ?? localStorage.getItem('synaptiq_auth_token');
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }

      const response = await fetch(`${this.baseUrl}/message`, {
        method: 'POST',
        headers,
        body: JSON.stringify({
          sessionId: request.sessionId,
          message: request.message,
          ...(request.modelOverride ? { modelOverride: request.modelOverride } : {}),
          ...(request.background ? { background: true } : {}),
          ...(request.knowledgeBaseIds?.length ? { knowledgeBaseIds: request.knowledgeBaseIds } : {}),
        }),
        signal: controller.signal,
      });

      if (!response.ok) {
        const errorBody = await response.text().catch(() => 'Unknown error');
        this.zone.run(() => callbacks.onError?.(
          `Server responded with ${response.status}: ${errorBody}`,
        ));
        return;
      }

      const reader = response.body?.getReader();
      if (!reader) {
        this.zone.run(() => callbacks.onError?.('No response body'));
        return;
      }

      const decoder = new TextDecoder();
      let buffer = '';

      while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        buffer += decoder.decode(value, { stream: true });

        const parts = buffer.split('\n\n');
        buffer = parts.pop() ?? '';

        for (const part of parts) {
          const event = this.parseSseBlock(part.trim());
          if (event) {
            this.zone.run(() => this.dispatchEvent(event, callbacks));
          }
        }
      }

      // Process any remaining buffer
      if (buffer.trim()) {
        const event = this.parseSseBlock(buffer.trim());
        if (event) {
          this.zone.run(() => this.dispatchEvent(event, callbacks));
        }
      }
    } catch (err: unknown) {
      if (err instanceof DOMException && err.name === 'AbortError') {
        return;
      }
      const message = err instanceof Error ? err.message : 'Network error';
      this.zone.run(() => callbacks.onError?.(message));
    } finally {
      if (this.activeController === controller) {
        this.activeController = null;
      }
    }
  }

  /**
   * Abort the current in-flight stream.
   */
  abort(): void {
    this.activeController?.abort();
    this.activeController = null;
  }

  // ── Private helpers ───────────────────────────────────────────────────

  private parseSseBlock(block: string): ChatSseEvent | null {
    const lines = block.split('\n');
    let eventType = '';
    let dataStr = '';

    for (const line of lines) {
      if (line.startsWith('event: ')) {
        eventType = line.slice(7).trim();
      } else if (line.startsWith('data: ')) {
        dataStr += line.slice(6);
      } else if (line.startsWith('data:')) {
        dataStr += line.slice(5);
      }
    }

    if (!eventType || !dataStr) return null;

    try {
      const data = JSON.parse(dataStr);

      switch (eventType) {
        case 'token':
          return { type: 'token', text: data.text ?? '' };
        case 'component':
          return { type: 'component', component: data as ComponentSpec };
        case 'status':
          return { type: 'status', message: data.message ?? '' };
        case 'text_replace':
          return { type: 'text_replace', text: data.text ?? '' };
        case 'step_start':
          return {
            type: 'step_start',
            stepId: data.step_id ?? data.stepId ?? '',
            action: data.action ?? '',
            description: data.description ?? '',
          };
        case 'step_complete':
          return {
            type: 'step_complete',
            stepId: data.step_id ?? data.stepId ?? '',
            action: data.action ?? '',
          };
        case 'done':
          return {
            type: 'done',
            turnId: data.turn_id ?? data.turnId ?? '',
            tokensInput: data.tokens_input ?? data.tokensInput ?? 0,
            tokensOutput: data.tokens_output ?? data.tokensOutput ?? 0,
          };
        case 'error':
          return { type: 'error', message: data.message ?? 'Unknown error' };
        default:
          return null;
      }
    } catch {
      return null;
    }
  }

  private dispatchEvent(event: ChatSseEvent, callbacks: ChatStreamCallbacks): void {
    switch (event.type) {
      case 'token':
        callbacks.onToken?.(event.text);
        break;
      case 'component':
        callbacks.onComponent?.(event.component);
        break;
      case 'status':
        callbacks.onStatus?.(event.message);
        break;
      case 'text_replace':
        callbacks.onTextReplace?.(event.text);
        break;
      case 'step_start':
        callbacks.onStepStart?.(event);
        break;
      case 'step_complete':
        callbacks.onStepComplete?.(event);
        break;
      case 'done':
        callbacks.onDone?.(event);
        break;
      case 'error':
        callbacks.onError?.(event.message);
        break;
    }
  }
}
