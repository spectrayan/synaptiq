// ---------------------------------------------------------------------------
// ChatStreamOrchestrator — encapsulates SSE streaming and demo-mode fallback
// ---------------------------------------------------------------------------
// Extracted from ChatShellComponent to isolate the streaming callback wiring
// from the component's state management.

import { Injectable, inject } from '@angular/core';
import { type ViewSpec } from '@synaptiq/constants';
import { ChatService } from '@synaptiq/chat';
import { ChatMessage } from '../chat-message.model';
import { MSG_BACKEND_OFFLINE, MSG_BACKEND_UNREACHABLE } from '../chat-shell.constants';

// ── Callback interfaces ─────────────────────────────────────────────────────

/** Callbacks that the component provides so the orchestrator can update state. */
export interface StreamCallbacks {
  /** Replace the full messages array via an updater function. */
  updateMessages: (fn: (msgs: ChatMessage[]) => ChatMessage[]) => void;
  /** Set the loading flag. */
  setLoading: (value: boolean) => void;
  /** Update pinned views via an updater function. */
  updatePinnedViews: (fn: (views: ViewSpec[]) => ViewSpec[]) => void;
  /** Set the active pinned view ID. */
  setActivePinnedView: (id: string) => void;
  /** Start auto-refresh for a pinned view. */
  startAutoRefresh: (viewId: string, intervalSec: number, sourcePrompt: string) => void;
  /** Called on first successful exchange for session auto-persist. */
  onSessionCreated: () => void;
}

// ── Service ─────────────────────────────────────────────────────────────────

@Injectable({ providedIn: 'root' })
export class ChatStreamOrchestrator {
  private readonly chatService = inject(ChatService);

  /**
   * Send a message through the backend SSE channel and wire all streaming
   * events back into the component's message signal.
   */
  async sendViaBackend(
    msg: string,
    sessionId: string,
    typingId: string | undefined,
    callbacks: StreamCallbacks,
    knowledgeBaseIds?: string[],
  ): Promise<void> {
    const assistantMsgId = crypto.randomUUID();
    let typingCleared = false;

    /** Swap the typing indicator for the real assistant message on first data. */
    const ensureAssistantMessage = () => {
      if (typingCleared) return;
      typingCleared = true;
      callbacks.updateMessages((msgs) => {
        const filtered = typingId ? msgs.filter((m) => m.id !== typingId) : msgs;
        return [
          ...filtered,
          {
            id: assistantMsgId,
            role: 'assistant' as const,
            content: '',
            timestamp: new Date(),
            uiComponents: [],
          },
        ];
      });
    };

    await this.chatService.streamMessage(
      {
        sessionId: sessionId,
        message: msg,
        ...(knowledgeBaseIds?.length ? { knowledgeBaseIds } : {}),
      },
      {
        onToken: (token) => {
          ensureAssistantMessage();
          callbacks.updateMessages((msgs) =>
            msgs.map((m) =>
              m.id === assistantMsgId ? { ...m, content: m.content + token } : m,
            ),
          );
        },

        onComponent: (component) => {
          ensureAssistantMessage();

          // If this is a pinned view, add to pinned list
          if (component.type === 'view' && (component as any).pinned) {
            const viewSpec = component as any;
            callbacks.updatePinnedViews((views: ViewSpec[]) => {
              const existing = views.findIndex((v: ViewSpec) => v.view_id === viewSpec.view_id);
              if (existing >= 0) {
                const updated = [...views];
                updated[existing] = viewSpec;
                return updated;
              }
              return [...views, viewSpec];
            });
            callbacks.setActivePinnedView(viewSpec.view_id);

            // Start auto-refresh if the view has a refresh interval
            if (viewSpec.refresh_interval && viewSpec.refresh_interval > 0) {
              callbacks.startAutoRefresh(viewSpec.view_id, viewSpec.refresh_interval, msg);
            }
          }

          // Push DSL component into the assistant message
          callbacks.updateMessages((msgs) =>
            msgs.map((m) =>
              m.id === assistantMsgId
                ? { ...m, uiComponents: [...(m.uiComponents ?? []), component] }
                : m,
            ),
          );
        },

        onStatus: (statusMessage) => {
          ensureAssistantMessage();
          callbacks.updateMessages((msgs) =>
            msgs.map((m) =>
              m.id === assistantMsgId ? { ...m, statusText: statusMessage } : m,
            ),
          );
        },

        onTextReplace: (text) => {
          ensureAssistantMessage();
          callbacks.updateMessages((msgs) =>
            msgs.map((m) =>
              m.id === assistantMsgId ? { ...m, content: text } : m,
            ),
          );
        },

        onStepStart: (event) => {
          ensureAssistantMessage();
          callbacks.updateMessages((msgs) =>
            msgs.map((m) =>
              m.id === assistantMsgId ? { ...m, statusText: event.description } : m,
            ),
          );
        },

        onStepComplete: () => {
          callbacks.updateMessages((msgs) =>
            msgs.map((m) =>
              m.id === assistantMsgId ? { ...m, statusText: undefined } : m,
            ),
          );
        },

        onDone: () => {
          ensureAssistantMessage();
          callbacks.updateMessages((msgs) =>
            msgs.map((m) =>
              m.id === assistantMsgId ? { ...m, statusText: undefined } : m,
            ),
          );
          callbacks.setLoading(false);
          callbacks.onSessionCreated();
        },

        onError: (errorMessage) => {
          ensureAssistantMessage();
          const displayError = (errorMessage.includes('Failed to fetch') || errorMessage.includes('NetworkError'))
            ? MSG_BACKEND_UNREACHABLE
            : errorMessage;

          callbacks.updateMessages((msgs) =>
            msgs.map((m) =>
              m.id === assistantMsgId
                ? {
                    ...m,
                    content: '',
                    statusText: undefined,
                    uiComponents: [
                      {
                        type: 'info_banner' as const,
                        title: 'Error',
                        body: displayError,
                        style: 'error' as const,
                        suggestions: [{ label: 'Try again', prompt: msg }],
                      },
                    ],
                  }
                : m,
            ),
          );
          callbacks.setLoading(false);
        },
      },
    );
  }

  /**
   * Offline / demo-mode fallback — shows a "backend offline" banner.
   */
  async sendViaDemo(
    msg: string,
    typingId: string | undefined,
    callbacks: StreamCallbacks,
  ): Promise<void> {
    await new Promise((r) => setTimeout(r, 600));

    // Remove typing indicator
    if (typingId) {
      callbacks.updateMessages((msgs) => msgs.filter((m) => m.id !== typingId));
    }

    callbacks.updateMessages((msgs) => [
      ...msgs,
      {
        id: crypto.randomUUID(),
        role: 'assistant',
        content: '',
        timestamp: new Date(),
        uiComponents: [
          {
            type: 'info_banner' as const,
            title: 'Backend Offline',
            body: MSG_BACKEND_OFFLINE,
            style: 'warning' as const,
            suggestions: [{ label: 'Retry', prompt: msg }],
          },
        ],
      },
    ]);
    callbacks.setLoading(false);
  }
}
