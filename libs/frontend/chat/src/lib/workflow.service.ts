/**
 * WorkflowService — facade for the workflow API.
 *
 * REST endpoints delegate to the generated WorkflowsService from @synaptiq/client.
 * SSE streaming endpoints (generate, execute) use fetch() + ReadableStream.
 *
 * Custom endpoints not yet in the OpenAPI spec (listRuns, getRunDetails,
 * regeneratePrompt, getAvailableTools) use direct HttpClient calls.
 */
import { HttpClient } from '@angular/common/http';
import { inject, Injectable, NgZone } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ENVIRONMENT } from '@synaptiq/utils';
import {
  WorkflowsService as WorkflowsApiService,
  type WorkflowResponse,
  type WorkflowListResponse,
  type ShareWorkflowResponse,
  type SaveWorkflowRequest,
  type FlowSettingsSpec,
  type AgentSpec,
  type EdgeSpec,
  type ToolSpec,
  type LLMSpec,
  type MCPServerSpec,
  type ExecutionPolicySpec,
  type ToolCatalogResponse,
  type ToolDefinitionResponse,
  type WorkflowInput,
  type WorkflowRunSummary,
  type WorkflowRunDetail,
  type WorkflowRunNodeDetail,
  type ExecuteWorkflowRequest,
  type GenerateWorkflowRequest,
  type RegeneratePromptRequest,
  type RegeneratePromptResponse,
} from '@synaptiq/client';
export type { LLMSpec } from '@synaptiq/client';

// Re-export SDK types — downstream consumers import from @synaptiq/chat, not @synaptiq/client
export type {
  WorkflowResponse,
  WorkflowListResponse,
  ShareWorkflowResponse,
  SaveWorkflowRequest,
  FlowSettingsSpec,
  AgentSpec,
  EdgeSpec,
  ToolSpec,
  MCPServerSpec,
  ExecutionPolicySpec,
  ToolCatalogResponse,
  ToolDefinitionResponse,
  WorkflowInput,
  WorkflowRunSummary,
  WorkflowRunDetail,
  WorkflowRunNodeDetail,
  ExecuteWorkflowRequest,
  GenerateWorkflowRequest,
  RegeneratePromptRequest,
  RegeneratePromptResponse,
} from '@synaptiq/client';

// ---------------------------------------------------------------------------
// Type aliases — the canvas and chat-shell use these names
// ---------------------------------------------------------------------------

/** Workflow spec = FlowSettingsSpec from the SDK. */
export type WorkflowSpec = FlowSettingsSpec;

/** Agent node = AgentSpec from the SDK. */
export type AgentNodeSpec = AgentSpec;

/** Tool catalog entry = ToolDefinitionResponse from the SDK. */
export type ToolDefinition = ToolDefinitionResponse;

/** Tool catalog response alias. */
export type ToolCatalog = ToolCatalogResponse;

/** Tool entry alias. */
export type ToolEntry = ToolDefinitionResponse;

// ---------------------------------------------------------------------------
// SSE Event Types
// ---------------------------------------------------------------------------

export interface WorkflowSseStatusEvent {
  readonly type: 'status';
  readonly message: string;
}

export interface WorkflowSseComponentEvent {
  readonly type: 'component';
  readonly spec: Record<string, unknown>;
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

export interface WorkflowStreamCallbacks {
  onStatus?: (message: string) => void;
  onComponent?: (spec: Record<string, unknown>) => void;
  onText?: (text: string) => void;
  onDone?: () => void;
  onError?: (message: string) => void;
}

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
// Service — REST (via SDK) + SSE streaming + custom endpoints
// ---------------------------------------------------------------------------

@Injectable({ providedIn: 'root' })
export class WorkflowService {
  private readonly api = inject(WorkflowsApiService);
  private readonly http = inject(HttpClient);
  private readonly env = inject(ENVIRONMENT);
  private readonly zone = inject(NgZone);
  private readonly baseUrl = `${this.env.apiBaseUrl}/api/v1/workflow`;

  private activeController: AbortController | null = null;

  // ── Build headers for fetch() calls ──────────────────────────────────

  private buildHeaders(contentType?: string): Record<string, string> {
    const headers: Record<string, string> = {};
    if (contentType) headers['Content-Type'] = contentType;
    if (this.env.tenantId) headers['X-Tenant-ID'] = this.env.tenantId;
    const token = localStorage.getItem('synaptiq_auth_token');
    if (token) headers['Authorization'] = `Bearer ${token}`;
    return headers;
  }

  // ══════════════════════════════════════════════════════════════════════
  // REST endpoints — delegate to generated SDK (via Angular HttpClient)
  // ══════════════════════════════════════════════════════════════════════

  /** List workflows for the current tenant. */
  async listWorkflows(_authToken?: string): Promise<WorkflowSpec[]> {
    const response = await firstValueFrom(this.api.listWorkflows({}));
    // Each item in the list is a WorkflowResponse { id, tenantId, spec: {...} }
    return (response.workflows ?? []).map((wr: unknown) => {
      const r = wr as { id?: string; spec?: Record<string, unknown> };
      const spec = { ...(r.spec as unknown as WorkflowSpec) };
      if (r.id) spec.id = r.id;
      return spec;
    });
  }

  /** Get a single workflow by ID. */
  async getWorkflow(workflowId: string, _authToken?: string): Promise<WorkflowSpec | null> {
    try {
      const response = await firstValueFrom(this.api.getWorkflow({ workflowId }));
      // WorkflowResponse has { id, tenantId, spec: {...} } — merge id into the spec
      const res = response as unknown as { id?: string; spec?: Record<string, unknown> };
      const spec: WorkflowSpec = { ...(res.spec as unknown as WorkflowSpec) };
      if (res.id) spec.id = res.id;
      return spec;
    } catch {
      return null;
    }
  }

  /** Save a new workflow. Returns the spec with the persisted id. */
  async saveWorkflow(spec: WorkflowSpec, _authToken?: string): Promise<WorkflowSpec> {
    // SaveWorkflowRequest schema: { spec: FlowSettingsSpec }
    const body = { spec } as unknown as SaveWorkflowRequest;
    const response = await firstValueFrom(
      this.api.saveWorkflow({ saveWorkflowRequest: body }),
    );
    // WorkflowResponse has { id, tenantId, spec: {...} } — merge id into the spec
    const res = response as unknown as { id?: string; spec?: Record<string, unknown> };
    const saved: WorkflowSpec = { ...(res.spec as unknown as WorkflowSpec) };
    if (res.id) saved.id = res.id;
    return saved;
  }

  /** Update an existing workflow. */
  async updateWorkflow(workflowId: string, spec: WorkflowSpec, _authToken?: string): Promise<WorkflowSpec> {
    // SaveWorkflowRequest schema: { spec: FlowSettingsSpec }
    const body = { spec } as unknown as SaveWorkflowRequest;
    const response = await firstValueFrom(
      this.api.updateWorkflow({ workflowId, saveWorkflowRequest: body }),
    );
    // WorkflowResponse has { id, tenantId, spec: {...} } — merge id into the spec
    const res = response as unknown as { id?: string; spec?: Record<string, unknown> };
    const saved: WorkflowSpec = { ...(res.spec as unknown as WorkflowSpec) };
    if (res.id) saved.id = res.id;
    return saved;
  }

  /** Delete a workflow. */
  async deleteWorkflow(workflowId: string, _authToken?: string): Promise<void> {
    await firstValueFrom(this.api.deleteWorkflow({ workflowId }));
  }

  /** Duplicate a workflow. Returns the new workflow ID. */
  async duplicateWorkflow(workflowId: string, _authToken?: string): Promise<string> {
    const response = await firstValueFrom(this.api.duplicateWorkflow({ workflowId }));
    return (response as unknown as WorkflowSpec).id ?? '';
  }

  /** Share a workflow. Returns the share token. */
  async shareWorkflow(workflowId: string, _authToken?: string): Promise<string> {
    const response = await firstValueFrom(this.api.shareWorkflow({ workflowId }));
    return response.shareToken ?? '';
  }

  /** Get a shared workflow by token. */
  async getSharedWorkflow(shareToken: string): Promise<WorkflowSpec | null> {
    try {
      const response = await firstValueFrom(this.api.getSharedWorkflow({ shareToken }));
      return response as unknown as WorkflowSpec;
    } catch {
      return null;
    }
  }

  /** List workflow templates (community/pre-built). */
  async listPublicTemplates(_authToken?: string): Promise<WorkflowSpec[]> {
    const response = await firstValueFrom(this.api.listWorkflowTemplates());
    return (response.workflows ?? []) as unknown as WorkflowSpec[];
  }

  // ══════════════════════════════════════════════════════════════════════
  // Custom endpoints — not yet in OpenAPI spec, direct HTTP
  // ══════════════════════════════════════════════════════════════════════

  /** List execution runs for a workflow. */
  async listRuns(workflowId: string, _authToken?: string): Promise<WorkflowRunSummary[]> {
    return firstValueFrom(
      this.http.get<WorkflowRunSummary[]>(`${this.baseUrl}/${workflowId}/runs`),
    );
  }

  /** Get details for a specific run. */
  async getRunDetails(runId: string, _authToken?: string): Promise<WorkflowRunDetail> {
    return firstValueFrom(
      this.http.get<WorkflowRunDetail>(`${this.baseUrl}/runs/${runId}`),
    );
  }

  /** Get available tools/actions for workflow nodes. */
  async getAvailableTools(_authToken?: string): Promise<ToolCatalog> {
    return firstValueFrom(
      this.http.get<ToolCatalog>(`${this.baseUrl}/tools`),
    );
  }

  /** Regenerate a prompt for a workflow node using AI. */
  async regeneratePrompt(payload: Record<string, unknown>, _authToken?: string): Promise<string> {
    const res = await firstValueFrom(
      this.http.post<{ prompt: string }>(`${this.baseUrl}/regenerate-prompt`, payload),
    );
    return res.prompt;
  }

  // ══════════════════════════════════════════════════════════════════════
  // SSE streaming endpoints — fetch() + ReadableStream
  // ══════════════════════════════════════════════════════════════════════

  /** Stream a workflow generation request via SSE over POST. */
  async streamGenerate(prompt: string, callbacks: WorkflowStreamCallbacks): Promise<void> {
    this.abort();

    const controller = new AbortController();
    this.activeController = controller;

    try {
      const headers = this.buildHeaders('application/json');
      headers['Accept'] = 'text/event-stream';

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

      await this.consumeStream(response, (event) => this.dispatchEvent(event, callbacks));
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

  /** Stream a workflow execution via SSE over POST. */
  async streamExecute(
    spec: Record<string, unknown>,
    inputText: string,
    callbacks: ExecutionCallbacks,
    _authToken?: string,
    dryRun = false,
    startNodeId?: string,
    priorContext?: string,
    inputVariables?: Record<string, unknown>,
  ): Promise<void> {
    this.abort();

    const controller = new AbortController();
    this.activeController = controller;

    try {
      const headers = this.buildHeaders('application/json');
      headers['Accept'] = 'text/event-stream';

      const response = await fetch(`${this.baseUrl}/execute`, {
        method: 'POST',
        headers,
        body: JSON.stringify({
          spec,
          input_text: inputText,
          dry_run: dryRun,
          ...(startNodeId ? { start_node_id: startNodeId } : {}),
          ...(priorContext ? { prior_context: priorContext } : {}),
          ...(inputVariables ? { input_variables: inputVariables } : {}),
        }),
        signal: controller.signal,
      });

      if (!response.ok) {
        const errorBody = await response.text().catch(() => 'Unknown error');
        this.zone.run(() => callbacks.onExecutionError?.(`Server responded with ${response.status}: ${errorBody}`));
        return;
      }

      await this.consumeStream(response, (block) => {
        this.zone.run(() => this.dispatchExecutionEvent(block, callbacks));
      });
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

  abort(): void {
    this.activeController?.abort();
    this.activeController = null;
  }

  // ── Private helpers ─────────────────────────────────────────────────────

  private async consumeStream(
    response: Response,
    onBlock: (block: string) => void,
  ): Promise<void> {
    const reader = response.body?.getReader();
    if (!reader) return;

    const decoder = new TextDecoder();
    let buffer = '';

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;

      buffer += decoder.decode(value, { stream: true });
      const parts = buffer.split('\n\n');
      buffer = parts.pop() ?? '';

      for (const part of parts) {
        onBlock(part.trim());
      }
    }

    if (buffer.trim()) {
      onBlock(buffer.trim());
    }
  }

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
      if (eventType === 'status') return { type: 'status', message: dataStr };
      if (eventType === 'text') return { type: 'text', text: dataStr };
      if (eventType === 'error') return { type: 'error', message: dataStr };
      return null;
    }
  }

  private dispatchEvent(block: string, callbacks: WorkflowStreamCallbacks): void {
    const event = this.parseSseBlock(block);
    if (!event) return;

    this.zone.run(() => {
      switch (event.type) {
        case 'status': callbacks.onStatus?.(event.message); break;
        case 'component': callbacks.onComponent?.(event.spec); break;
        case 'text': callbacks.onText?.(event.text); break;
        case 'done': callbacks.onDone?.(); break;
        case 'error': callbacks.onError?.(event.message); break;
      }
    });
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
