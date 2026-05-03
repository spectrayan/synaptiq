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

export interface ToolDefinition {
  id: string;
  name: string;
  description: string;
  category: string;
  icon: string;
}

export interface ToolCatalogResponse {
  tools: ToolDefinition[];
  categories: Record<string, { label: string; icon: string }>;
  by_category: Record<string, ToolDefinition[]>;
}

// ---------------------------------------------------------------------------
// Execution Run Types
// ---------------------------------------------------------------------------

export interface WorkflowRunSummary {
  run_id: string;
  status: 'running' | 'completed' | 'error';
  dry_run: boolean;
  started_at: number;
  completed_at?: number;
  total_duration_ms?: number;
  workflow_name?: string;
}

export interface WorkflowRunNodeDetail {
  node_id: string;
  label?: string;
  status: NodeExecutionStatus;
  started_at?: number;
  completed_at?: number;
  duration_ms?: number;
  output?: string;
  error?: string;
}

export interface WorkflowRunDetail {
  run_id: string;
  workflow_id: string;
  workflow_name: string;
  tenant_id: string;
  status: 'running' | 'completed' | 'error';
  dry_run: boolean;
  input_text: string;
  started_at: number;
  completed_at?: number;
  total_duration_ms?: number;
  nodes: Record<string, WorkflowRunNodeDetail>;
  result?: string;
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
// Execution Models
// ---------------------------------------------------------------------------

export type NodeExecutionStatus = 'pending' | 'running' | 'completed' | 'error' | 'skipped';

export interface NodeStatusUpdate {
  node_id: string;
  status: NodeExecutionStatus;
  started_at?: number;
  completed_at?: number;
  duration_ms?: number;
  output?: string;
  error?: string;
}

export interface ExecutionState {
  run_id: string;
  workflow_id?: string;
  status: 'running' | 'completed' | 'error' | 'cancelled';
  current_node: string | null;
  nodes: Record<string, NodeStatusUpdate>;
  started_at: number;
  completed_at?: number;
  total_duration_ms?: number;
  result?: string;
}

export interface ExecutionCallbacks {
  onExecutionStart?: (state: ExecutionState) => void;
  onNodeStart?: (nodeId: string, label: string) => void;
  onNodeComplete?: (nodeId: string, label: string, durationMs: number, output?: string) => void;
  onNodeError?: (nodeId: string, label: string, error: string) => void;
  onExecutionComplete?: (state: ExecutionState) => void;
  onExecutionError?: (error: string, failedNode?: string) => void;
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

  /** Fetch available tools catalog (cached after first call). */
  private _toolCache: ToolCatalogResponse | null = null;

  async getAvailableTools(authToken?: string): Promise<ToolCatalogResponse> {
    if (this._toolCache) return this._toolCache;

    const headers: Record<string, string> = {};
    if (this.env.tenantId) headers['X-Tenant-ID'] = this.env.tenantId;
    if (authToken) headers['Authorization'] = `Bearer ${authToken}`;

    const response = await fetch(`${this.baseUrl}/tools`, { headers });
    if (!response.ok) return { tools: [], categories: {}, by_category: {} };
    const data = await response.json();
    this._toolCache = data;
    return data;
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

  /** Load all saved workflows for the current tenant. */
  async listWorkflows(authToken?: string): Promise<WorkflowSpec[]> {
    console.log('[WorkflowService] listWorkflows: fetching...');
    const headers: Record<string, string> = {};
    if (this.env.tenantId) headers['X-Tenant-ID'] = this.env.tenantId;
    if (authToken) headers['Authorization'] = `Bearer ${authToken}`;

    const response = await fetch(`${this.baseUrl}/list`, { headers });
    if (!response.ok) {
      console.warn(`[WorkflowService] listWorkflows failed: ${response.status}`);
      return [];
    }
    const data = await response.json();
    console.log(`[WorkflowService] listWorkflows: got ${(data.workflows ?? []).length} workflows`);
    return data.workflows ?? [];
  }

  /** Get full workflow details by ID. */
  async getWorkflow(workflowId: string, authToken?: string): Promise<WorkflowSpec | null> {
    console.log(`[WorkflowService] getWorkflow: ${workflowId}`);
    const headers: Record<string, string> = {};
    if (this.env.tenantId) headers['X-Tenant-ID'] = this.env.tenantId;
    if (authToken) headers['Authorization'] = `Bearer ${authToken}`;

    const response = await fetch(`${this.baseUrl}/${workflowId}`, { headers });
    if (!response.ok) {
      console.warn(`[WorkflowService] getWorkflow ${workflowId} failed: ${response.status}`);
      return null;
    }
    const data = await response.json();
    console.log(`[WorkflowService] getWorkflow: loaded "${data?.name}" with ${data?.agents?.length ?? 0} agents`);
    return data;
  }

  /** List past execution runs for a workflow. */
  async listRuns(workflowId: string, authToken?: string): Promise<WorkflowRunSummary[]> {
    console.log(`[WorkflowService] listRuns: workflow=${workflowId}`);
    const headers: Record<string, string> = {};
    if (this.env.tenantId) headers['X-Tenant-ID'] = this.env.tenantId;
    if (authToken) headers['Authorization'] = `Bearer ${authToken}`;

    const response = await fetch(`${this.baseUrl}/${workflowId}/runs`, { headers });
    if (!response.ok) {
      console.warn(`[WorkflowService] listRuns ${workflowId} failed: ${response.status}`);
      return [];
    }
    const data = await response.json();
    console.log(`[WorkflowService] listRuns: got ${(data.runs ?? []).length} runs`);
    return data.runs ?? [];
  }

  /** Get full execution run details with all node outputs. */
  async getRunDetails(runId: string, authToken?: string): Promise<WorkflowRunDetail | null> {
    console.log(`[WorkflowService] getRunDetails: ${runId}`);
    const headers: Record<string, string> = {};
    if (this.env.tenantId) headers['X-Tenant-ID'] = this.env.tenantId;
    if (authToken) headers['Authorization'] = `Bearer ${authToken}`;

    const response = await fetch(`${this.baseUrl}/runs/${runId}`, { headers });
    if (!response.ok) {
      console.warn(`[WorkflowService] getRunDetails ${runId} failed: ${response.status}`);
      return null;
    }
    const data = await response.json();
    console.log(`[WorkflowService] getRunDetails: loaded run status=${data?.status}`);
    return data;
  }

  /** Update an existing workflow spec (partial or full). */
  async updateWorkflow(workflowId: string, spec: Partial<WorkflowSpec>, authToken?: string): Promise<{ updated_at: number }> {
    console.log(`[WorkflowService] updateWorkflow: ${workflowId}`);
    const headers: Record<string, string> = { 'Content-Type': 'application/json' };
    if (this.env.tenantId) headers['X-Tenant-ID'] = this.env.tenantId;
    if (authToken) headers['Authorization'] = `Bearer ${authToken}`;

    const response = await fetch(`${this.baseUrl}/${workflowId}`, {
      method: 'PATCH',
      headers,
      body: JSON.stringify({ spec }),
    });
    if (!response.ok) {
      const err = await response.text().catch(() => 'Unknown error');
      console.error(`[WorkflowService] updateWorkflow failed: ${response.status} — ${err}`);
      throw new Error(`Failed to update workflow: ${response.status}`);
    }
    const data = await response.json();
    console.log(`[WorkflowService] updateWorkflow: updated_at=${data.updated_at}`);
    return data;
  }

  /** Delete a workflow and its execution history. */
  async deleteWorkflow(workflowId: string, authToken?: string): Promise<void> {
    console.log(`[WorkflowService] deleteWorkflow: ${workflowId}`);
    const headers: Record<string, string> = {};
    if (this.env.tenantId) headers['X-Tenant-ID'] = this.env.tenantId;
    if (authToken) headers['Authorization'] = `Bearer ${authToken}`;

    const response = await fetch(`${this.baseUrl}/${workflowId}`, {
      method: 'DELETE',
      headers,
    });
    if (!response.ok && response.status !== 204) {
      console.error(`[WorkflowService] deleteWorkflow failed: ${response.status}`);
      throw new Error(`Failed to delete workflow: ${response.status}`);
    }
    console.log(`[WorkflowService] deleteWorkflow: done`);
  }

  /** Ask AI to improve a node's system prompt. */
  async regeneratePrompt(
    payload: {
      node_id: string;
      node_label: string;
      node_description: string;
      current_prompt: string;
      instruction: string;
      workflow_context: Record<string, unknown>;
    },
    authToken?: string,
  ): Promise<string> {
    console.log(`[WorkflowService] regeneratePrompt: node=${payload.node_id}`);
    const headers: Record<string, string> = { 'Content-Type': 'application/json' };
    if (this.env.tenantId) headers['X-Tenant-ID'] = this.env.tenantId;
    if (authToken) headers['Authorization'] = `Bearer ${authToken}`;

    const response = await fetch(`${this.baseUrl}/regenerate-prompt`, {
      method: 'POST',
      headers,
      body: JSON.stringify(payload),
    });
    if (!response.ok) {
      const err = await response.text().catch(() => 'Unknown error');
      console.error(`[WorkflowService] regeneratePrompt failed: ${response.status} — ${err}`);
      throw new Error(`Failed to regenerate prompt: ${response.status}`);
    }
    const data = await response.json();
    console.log(`[WorkflowService] regeneratePrompt: got ${data.improved_prompt?.length ?? 0} chars`);
    return data.improved_prompt;
  }

  /** Duplicate a workflow, returns the new workflow ID. */
  async duplicateWorkflow(workflowId: string, authToken?: string): Promise<string> {
    console.log(`[WorkflowService] duplicateWorkflow: ${workflowId}`);
    const headers: Record<string, string> = { 'Content-Type': 'application/json' };
    if (this.env.tenantId) headers['X-Tenant-ID'] = this.env.tenantId;
    if (authToken) headers['Authorization'] = `Bearer ${authToken}`;

    const response = await fetch(`${this.baseUrl}/${workflowId}/duplicate`, {
      method: 'POST',
      headers,
    });
    if (!response.ok) {
      console.error(`[WorkflowService] duplicateWorkflow failed: ${response.status}`);
      throw new Error(`Failed to duplicate workflow: ${response.status}`);
    }
    const data = await response.json();
    console.log(`[WorkflowService] duplicateWorkflow: new id=${data.id}`);
    return data.id;
  }

  abort(): void {
    this.activeController?.abort();
    this.activeController = null;
  }

  /**
   * Stream a workflow execution via SSE over POST.
   * Emits real-time events for each node's lifecycle.
   */
  async streamExecute(
    spec: WorkflowSpec,
    inputText: string,
    callbacks: ExecutionCallbacks,
    authToken?: string,
    dryRun = false,
  ): Promise<void> {
    this.abort();

    const controller = new AbortController();
    this.activeController = controller;

    try {
      const headers: Record<string, string> = {
        'Content-Type': 'application/json',
        'Accept': 'text/event-stream',
      };
      if (this.env.tenantId) headers['X-Tenant-ID'] = this.env.tenantId;
      if (authToken) headers['Authorization'] = `Bearer ${authToken}`;

      const response = await fetch(`${this.baseUrl}/execute`, {
        method: 'POST',
        headers,
        body: JSON.stringify({ spec, input_text: inputText, dry_run: dryRun }),
        signal: controller.signal,
      });

      if (!response.ok) {
        const errorBody = await response.text().catch(() => 'Unknown error');
        this.zone.run(() => callbacks.onExecutionError?.(`Server responded with ${response.status}: ${errorBody}`));
        return;
      }

      const reader = response.body?.getReader();
      if (!reader) {
        this.zone.run(() => callbacks.onExecutionError?.('No response body'));
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
          this.zone.run(() => this.dispatchExecutionEvent(part.trim(), callbacks));
        }
      }

      if (buffer.trim()) {
        this.zone.run(() => this.dispatchExecutionEvent(buffer.trim(), callbacks));
      }
    } catch (err: unknown) {
      if (err instanceof DOMException && err.name === 'AbortError') return;
      const message = err instanceof Error ? err.message : 'Network error';
      this.zone.run(() => callbacks.onExecutionError?.(message));
    } finally {
      if (this.activeController === controller) {
        this.activeController = null;
      }
    }
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

  private dispatchExecutionEvent(block: string, callbacks: ExecutionCallbacks): void {
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

    if (!eventType || !dataStr) return;

    try {
      const data = JSON.parse(dataStr);

      switch (eventType) {
        case 'execution_start':
          callbacks.onExecutionStart?.({
            run_id: data.run_id,
            status: 'running',
            current_node: null,
            nodes: data.nodes ?? {},
            started_at: data.started_at,
          });
          break;

        case 'node_start':
          callbacks.onNodeStart?.(data.node_id, data.label);
          break;

        case 'node_complete':
          callbacks.onNodeComplete?.(data.node_id, data.label, data.duration_ms, data.output_preview);
          break;

        case 'node_error':
          callbacks.onNodeError?.(data.node_id, data.label, data.error);
          break;

        case 'execution_complete':
          callbacks.onExecutionComplete?.({
            run_id: data.run_id,
            workflow_id: data.workflow_id,
            status: 'completed',
            current_node: null,
            nodes: data.nodes ?? {},
            started_at: data.started_at,
            completed_at: data.completed_at,
            total_duration_ms: data.total_duration_ms,
            result: data.result,
          });
          break;

        case 'execution_error':
          callbacks.onExecutionError?.(data.error, data.failed_node);
          break;
      }
    } catch {
      // Ignore malformed SSE data
    }
  }
}

