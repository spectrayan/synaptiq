/**
 * WorkflowService — SSE streaming client for the workflow generation API.
 *
 * Connects to `POST /api/v1/workflow/generate` and streams Server-Sent Events:
 *   - `status`    → progress messages ("Analyzing your workflow requirements...")
 *   - `component` → the generated WorkflowSpec as a component (type=workflow)
 *   - `text`      → summary description of the workflow
 *   - `error`     → error messages
 *   - `done`      → generation complete
 */
import { inject, Injectable, NgZone } from '@angular/core';
import { ENVIRONMENT } from '@synaptiq/utils';

// ---------------------------------------------------------------------------
// Models
// ---------------------------------------------------------------------------

export interface AgentNodeSpec {
  id: string;
  type: string;
  label: string;
  description: string;
  system_prompt: string;
  llm?: { provider: string; model: string };
  tools: string[];
  position?: { x: number; y: number };
}

export interface EdgeSpec {
  from: string;
  to: string;
  condition: string;
  label: string;
}

export interface ConditionalEdgeSpec {
  from: string;
  condition_mapping: Record<string, string>;
  default_target: string;
}

export interface WorkflowSpec {
  id: string;
  name: string;
  description: string;
  flow_type: string;
  agents: AgentNodeSpec[];
  edges: EdgeSpec[];
  conditional_edges: ConditionalEdgeSpec[];
  entry_point: string;
  metadata?: Record<string, unknown>;
  created_at?: number;
}

export interface WorkflowTemplate {
  id: string;
  name: string;
  description: string;
  flow_type: string;
  agents: AgentNodeSpec[];
  edges: EdgeSpec[];
  conditional_edges: ConditionalEdgeSpec[];
  entry_point: string;
}

// ---------------------------------------------------------------------------
// SSE Events
// ---------------------------------------------------------------------------

export interface WorkflowSseStatusEvent {
  readonly type: 'status';
  readonly message: string;
}

export interface WorkflowSseComponentEvent {
  readonly type: 'component';
  readonly spec: WorkflowSpec;
}

export interface WorkflowSseTextEvent {
  readonly type: 'text';
  readonly text: string;
}

export interface WorkflowSseErrorEvent {
  readonly type: 'error';
  readonly message: string;
}

export interface WorkflowSseDoneEvent {
  readonly type: 'done';
}

export type WorkflowSseEvent =
  | WorkflowSseStatusEvent
  | WorkflowSseComponentEvent
  | WorkflowSseTextEvent
  | WorkflowSseErrorEvent
  | WorkflowSseDoneEvent;

// ---------------------------------------------------------------------------
// Callbacks
// ---------------------------------------------------------------------------

export interface WorkflowStreamCallbacks {
  onStatus?: (message: string) => void;
  onComponent?: (spec: WorkflowSpec) => void;
  onText?: (text: string) => void;
  onDone?: () => void;
  onError?: (message: string) => void;
}

// ---------------------------------------------------------------------------
// Service
// ---------------------------------------------------------------------------

@Injectable({ providedIn: 'root' })
export class WorkflowService {
  private readonly env = inject(ENVIRONMENT);
  private readonly zone = inject(NgZone);
  private readonly baseUrl = `${this.env.apiBaseUrl}/api/v1/workflow`;

  private activeController: AbortController | null = null;

  /**
   * Stream a workflow generation request via SSE over POST.
   */
  async streamGenerate(
    prompt: string,
    callbacks: WorkflowStreamCallbacks,
    authToken?: string,
  ): Promise<void> {
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
      if (authToken) {
        headers['Authorization'] = `Bearer ${authToken}`;
      }

      const response = await fetch(`${this.baseUrl}/generate`, {
        method: 'POST',
        headers,
        body: JSON.stringify({ prompt }),
        signal: controller.signal,
      });

      if (!response.ok) {
        const errorBody = await response.text().catch(() => 'Unknown error');
        this.zone.run(() => callbacks.onError?.(`Server responded with ${response.status}: ${errorBody}`));
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

      if (buffer.trim()) {
        const event = this.parseSseBlock(buffer.trim());
        if (event) {
          this.zone.run(() => this.dispatchEvent(event, callbacks));
        }
      }
    } catch (err: unknown) {
      if (err instanceof DOMException && err.name === 'AbortError') return;
      const message = err instanceof Error ? err.message : 'Network error';
      this.zone.run(() => callbacks.onError?.(message));
    } finally {
      if (this.activeController === controller) {
        this.activeController = null;
      }
    }
  }

  /** Fetch starter templates. */
  async getTemplates(authToken?: string): Promise<WorkflowTemplate[]> {
    const headers: Record<string, string> = {};
    if (this.env.tenantId) headers['X-Tenant-ID'] = this.env.tenantId;
    if (authToken) headers['Authorization'] = `Bearer ${authToken}`;

    const response = await fetch(`${this.baseUrl}/templates`, { headers });
    if (!response.ok) return [];
    const data = await response.json();
    return data.templates ?? [];
  }

  /** Save a workflow to the backend. */
  async saveWorkflow(spec: WorkflowSpec, authToken?: string): Promise<string> {
    const headers: Record<string, string> = { 'Content-Type': 'application/json' };
    if (this.env.tenantId) headers['X-Tenant-ID'] = this.env.tenantId;
    if (authToken) headers['Authorization'] = `Bearer ${authToken}`;

    const response = await fetch(`${this.baseUrl}/save`, {
      method: 'POST',
      headers,
      body: JSON.stringify({ spec }),
    });
    const data = await response.json();
    return data.id;
  }

  abort(): void {
    this.activeController?.abort();
    this.activeController = null;
  }

  // ── Private helpers ─────────────────────────────────────────────────────

  private parseSseBlock(block: string): WorkflowSseEvent | null {
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

    if (!eventType) return null;

    try {
      switch (eventType) {
        case 'status':
          return { type: 'status', message: dataStr || '' };
        case 'component': {
          const data = JSON.parse(dataStr);
          return { type: 'component', spec: data.spec ?? data };
        }
        case 'text':
          return { type: 'text', text: dataStr || '' };
        case 'error':
          return { type: 'error', message: dataStr || 'Unknown error' };
        case 'done':
          return { type: 'done' };
        default:
          return null;
      }
    } catch {
      // For non-JSON events (status, text, error), use raw data string
      if (eventType === 'status') return { type: 'status', message: dataStr };
      if (eventType === 'text') return { type: 'text', text: dataStr };
      if (eventType === 'error') return { type: 'error', message: dataStr };
      return null;
    }
  }

  private dispatchEvent(event: WorkflowSseEvent, callbacks: WorkflowStreamCallbacks): void {
    switch (event.type) {
      case 'status': callbacks.onStatus?.(event.message); break;
      case 'component': callbacks.onComponent?.(event.spec); break;
      case 'text': callbacks.onText?.(event.text); break;
      case 'done': callbacks.onDone?.(); break;
      case 'error': callbacks.onError?.(event.message); break;
    }
  }
}
