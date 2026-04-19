/**
 * ChatService — SSE streaming client for the Synaptiq chat API (T9.1).
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
 */
import { inject, Injectable, NgZone } from '@angular/core';
import { ENVIRONMENT } from '@synaptiq/utils';
import { ComponentSpec } from '@synaptiq/constants';

// ---------------------------------------------------------------------------
// SSE Event Types
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
  readonly turn_id: string;
  readonly tokens_input: number;
  readonly tokens_output: number;
}

export interface SseErrorEvent {
  readonly type: 'error';
  readonly message: string;
}

export interface SseTextReplaceEvent {
  readonly type: 'text_replace';
  readonly text: string;
}

export type ChatSseEvent =
  | SseTokenEvent
  | SseComponentEvent
  | SseStatusEvent
  | SseDoneEvent
  | SseErrorEvent
  | SseTextReplaceEvent;

// ---------------------------------------------------------------------------
// Chat request payload
// ---------------------------------------------------------------------------

export interface ChatRequest {
  readonly session_id: string;
  readonly message: string;
  readonly model_override?: string;
}

// ---------------------------------------------------------------------------
// Callback-based stream handler
// ---------------------------------------------------------------------------

export interface ChatStreamCallbacks {
  /** Called for each text token (build typewriter effect). */
  onToken?: (text: string) => void;
  /** Called when the backend emits a DSL component. */
  onComponent?: (component: ComponentSpec) => void;
  /** Called for status/progress messages (e.g., fallback notices). */
  onStatus?: (message: string) => void;
  /** Called when the backend replaces streamed text with cleaned version (strips raw JSON). */
  onTextReplace?: (text: string) => void;
  /** Called once the stream completes successfully. */
  onDone?: (event: SseDoneEvent) => void;
  /** Called on any error (network, backend, parse). */
  onError?: (message: string) => void;
}

// ---------------------------------------------------------------------------
// Service
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
   *
   * Returns a `Promise` that resolves when the stream finishes (or errors).
   * The caller interacts via callbacks for real-time updates.
   *
   * @example
   * ```ts
   * await chatService.streamMessage(
   *   { session_id: 'abc', message: 'Show me electronics' },
   *   {
   *     onToken: (text) => accumulatedText += text,
   *     onComponent: (comp) => components.push(comp),
   *     onDone: (evt) => console.log('Done', evt.turn_id),
   *     onError: (msg) => console.error(msg),
   *   },
   *   authToken,
   * );
   * ```
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
      // Tenant resolution: backend middleware uses this header (local dev)
      // or subdomain extraction (production)
      if (this.env.tenantId) {
        headers['X-Tenant-ID'] = this.env.tenantId;
      }
      if (authToken) {
        headers['Authorization'] = `Bearer ${authToken}`;
      }

      const response = await fetch(`${this.baseUrl}/message`, {
        method: 'POST',
        headers,
        body: JSON.stringify(request),
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

        // SSE events are separated by double newlines
        const parts = buffer.split('\n\n');
        // Keep the last (potentially incomplete) chunk in the buffer
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
        // Stream was intentionally cancelled — not an error
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
   *
   * Safe to call even if no stream is active.
   */
  abort(): void {
    this.activeController?.abort();
    this.activeController = null;
  }

  // ── Private helpers ───────────────────────────────────────────────────

  /**
   * Parse a raw SSE text block into a typed event.
   *
   * SSE format:
   * ```
   * event: token
   * data: {"text": "Hello"}
   * ```
   */
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
        case 'done':
          return {
            type: 'done',
            turn_id: data.turn_id ?? '',
            tokens_input: data.tokens_input ?? 0,
            tokens_output: data.tokens_output ?? 0,
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

  /**
   * Dispatch a parsed SSE event to the appropriate callback.
   */
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
      case 'done':
        callbacks.onDone?.(event);
        break;
      case 'error':
        callbacks.onError?.(event.message);
        break;
    }
  }
}
