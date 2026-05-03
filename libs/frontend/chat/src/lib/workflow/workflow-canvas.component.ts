/**
 * WorkflowCanvasComponent — Interactive Foblex Flow-based workflow graph editor.
 *
 * Renders agent nodes and edges using @foblex/flow for rich interactivity:
 *   - Drag-to-connect via output/input ports
 *   - Drag-to-reposition nodes
 *   - Zoom & pan via scroll/drag (built-in)
 *   - Minimap for large graphs
 *   - Bezier curve connections
 *   - Node inspection panel with config/output tabs
 */
import {
  Component,
  input,
  output,
  signal,
  computed,
  effect,
  ChangeDetectionStrategy,
  viewChild,
  OnInit,
  HostListener,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MarkdownViewerComponent } from './markdown-viewer.component';
import { PromptEditorComponent, RegeneratePromptEvent } from './prompt-editor.component';
import {
  FFlowModule,
  FCanvasComponent,
  FCreateConnectionEvent,
  FZoomDirective,
} from '@foblex/flow';
import type {
  WorkflowSpec, AgentNodeSpec, EdgeSpec, ConditionalEdgeSpec, NodeExecutionStatus,
  WorkflowRunSummary, WorkflowRunNodeDetail, ToolDefinition,
} from '../workflow.service';

// ---------------------------------------------------------------------------
// Layout Types
// ---------------------------------------------------------------------------

interface LayoutNode {
  id: string;
  label: string;
  type: string;
  description: string;
  systemPrompt: string;
  tools: string[];
  x: number;
  y: number;
  color: string;
  icon: string;
  executionStatus: NodeExecutionStatus | 'idle';
  durationMs?: number;
}

/** Edge model for Foblex connections (connector-to-connector). */
interface FoblexEdge {
  id: string;
  sourceOutputId: string;
  targetInputId: string;
  label: string;
  isConditional: boolean;
  condition: string;
}

// ---------------------------------------------------------------------------
// Constants
// ---------------------------------------------------------------------------

const NODE_WIDTH = 220;
const NODE_HEIGHT = 80;
const LAYER_GAP_X = 300;
const NODE_GAP_Y = 120;
const PADDING = 60;

const NODE_COLORS: Record<string, string> = {
  agent: '#6366f1',        // Indigo
  tool: '#f59e0b',         // Amber
  function: '#10b981',     // Emerald
  conditional: '#f43f5e',  // Rose
  parallel: '#8b5cf6',     // Violet
  default: '#6366f1',
};

const NODE_ICONS: Record<string, string> = {
  agent: 'smart_toy',
  tool: 'build',
  function: 'code',
  conditional: 'call_split',
  parallel: 'account_tree',
  default: 'smart_toy',
};

@Component({
  selector: 'sq-workflow-canvas',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatIconModule,
    MatTooltipModule,
    MarkdownViewerComponent,
    PromptEditorComponent,
    FFlowModule,
    FZoomDirective,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './workflow-canvas.component.html',
  styleUrl: './workflow-canvas.component.scss',
})
export class WorkflowCanvasComponent {
  // ── Foblex canvas reference ────────────────────────────────────────────
  protected readonly fCanvas = viewChild(FCanvasComponent);

  // ── Inputs ──────────────────────────────────────────────────────────────
  readonly spec = input<WorkflowSpec | null>(null);

  // ── Outputs ─────────────────────────────────────────────────────────────
  readonly runWorkflow = output<void>();
  readonly stopExecution = output<void>();
  readonly specChange = output<WorkflowSpec>();
  readonly selectRun = output<string>();
  readonly deleteWorkflowRequest = output<string>();
  readonly duplicateWorkflowRequest = output<string>();
  readonly regeneratePromptRequest = output<{
    nodeId: string;
    nodeLabel: string;
    nodeDescription: string;
    currentPrompt: string;
    instruction: string;
  }>();
  readonly runFromNode = output<string>();

  // ── Child refs ──────────────────────────────────────────────────────────
  protected readonly promptEditorRef = viewChild<PromptEditorComponent>('promptEditorRef');

  // ── State ───────────────────────────────────────────────────────────────
  readonly selectedNodeId = signal<string | null>(null);
  readonly hoveredNodeId = signal<string | null>(null);
  readonly executionStatus = signal<'idle' | 'running' | 'completed' | 'error'>('idle');
  readonly totalDurationMs = signal(0);
  readonly inspectorTab = signal<'config' | 'output'>('config');
  readonly saveStatus = signal<'idle' | 'saving' | 'saved' | 'error'>('idle');
  readonly isEditingName = signal(false);
  readonly editingName = signal('');
  readonly showAddNodeForm = signal(false);
  readonly showDeleteConfirm = signal(false);
  readonly showToolbar3Dot = signal(false);

  /** Tool registry state */
  readonly showToolPicker = signal(false);
  readonly toolSearchQuery = signal('');
  readonly toolCatalog = signal<ToolDefinition[]>([]);
  private _toolIndex = new Map<string, ToolDefinition>();

  /** Grouped/filtered tools for the picker dropdown */
  readonly filteredToolCategories = computed(() => {
    const query = this.toolSearchQuery().toLowerCase().trim();
    const all = this.toolCatalog();
    const groups = new Map<string, { key: string; label: string; tools: ToolDefinition[] }>();
    for (const tool of all) {
      if (query && !tool.name.toLowerCase().includes(query) && !tool.description.toLowerCase().includes(query)) {
        continue;
      }
      if (!groups.has(tool.category)) {
        groups.set(tool.category, { key: tool.category, label: tool.category, tools: [] });
      }
      groups.get(tool.category)!.tools.push(tool);
    }
    return Array.from(groups.values());
  });

  /** Run history for the current workflow */
  readonly runHistory = signal<WorkflowRunSummary[]>([]);
  readonly selectedRunId = signal<string | null>(null);
  readonly showRunHistory = signal(false);

  /** Node outputs from a historical run (keyed by node_id) */
  readonly historicalNodeOutputs = signal<Record<string, WorkflowRunNodeDetail>>({});


  /** Per-node execution state, keyed by node ID. */
  readonly nodeExecState = signal<Record<string, { status: NodeExecutionStatus; durationMs?: number }>>({});

  // ── Undo / Redo ────────────────────────────────────────────────────────

  private static readonly MAX_UNDO_STACK = 50;
  private _undoStack: string[] = []; // JSON snapshots
  private _redoStack: string[] = [];
  readonly canUndo = signal(false);
  readonly canRedo = signal(false);

  /**
   * Central method for all spec mutations.
   * Pushes current state onto undo stack, clears redo, and emits the change.
   */
  protected emitSpecChange(updated: WorkflowSpec): void {
    const current = this.spec();
    if (current) {
      this._undoStack.push(JSON.stringify(current));
      if (this._undoStack.length > WorkflowCanvasComponent.MAX_UNDO_STACK) {
        this._undoStack.shift();
      }
      this._redoStack.length = 0;
      this._syncUndoRedoSignals();
    }
    this.specChange.emit(updated);
  }

  undo(): void {
    if (!this._undoStack.length) return;
    const current = this.spec();
    if (current) {
      this._redoStack.push(JSON.stringify(current));
    }
    const prev = JSON.parse(this._undoStack.pop()!) as WorkflowSpec;
    this._syncUndoRedoSignals();
    this.specChange.emit(prev); // Direct emit — don't push to undo again
    console.log('[Canvas] undo');
  }

  redo(): void {
    if (!this._redoStack.length) return;
    const current = this.spec();
    if (current) {
      this._undoStack.push(JSON.stringify(current));
    }
    const next = JSON.parse(this._redoStack.pop()!) as WorkflowSpec;
    this._syncUndoRedoSignals();
    this.specChange.emit(next); // Direct emit — don't push to undo
    console.log('[Canvas] redo');
  }

  private _syncUndoRedoSignals(): void {
    this.canUndo.set(this._undoStack.length > 0);
    this.canRedo.set(this._redoStack.length > 0);
  }

  /** Reset undo/redo stacks (e.g. when loading a new workflow). */
  resetHistory(): void {
    this._undoStack.length = 0;
    this._redoStack.length = 0;
    this._syncUndoRedoSignals();
  }

  @HostListener('document:keydown', ['$event'])
  onKeydown(event: KeyboardEvent): void {
    const ctrl = event.ctrlKey || event.metaKey;
    if (ctrl && event.key === 'z' && !event.shiftKey) {
      event.preventDefault();
      this.undo();
    } else if (ctrl && event.key === 'z' && event.shiftKey) {
      event.preventDefault();
      this.redo();
    } else if (ctrl && event.key === 'y') {
      event.preventDefault();
      this.redo();
    }
  }

  // ── Execution API (called from parent) ─────────────────────────────────

  /** Mark a node as running/completed/error — called by parent via SSE callbacks. */
  setNodeStatus(nodeId: string, status: NodeExecutionStatus, durationMs?: number): void {
    this.nodeExecState.update(s => ({ ...s, [nodeId]: { status, durationMs } }));
  }

  setExecutionStatus(status: 'idle' | 'running' | 'completed' | 'error', durationMs?: number): void {
    this.executionStatus.set(status);
    if (durationMs != null) this.totalDurationMs.set(durationMs);
  }

  resetExecution(): void {
    this.nodeExecState.set({});
    this.executionStatus.set('idle');
    this.totalDurationMs.set(0);
  }

  getNodeExecStatus(nodeId: string): NodeExecutionStatus | 'idle' {
    return this.nodeExecState()[nodeId]?.status ?? 'idle';
  }

  /** Inspector: update system prompt on the spec */
  onSystemPromptChange(newPrompt: string): void {
    const s = this.spec();
    const nodeId = this.selectedNodeId();
    if (!s || !nodeId) return;
    const updated: WorkflowSpec = {
      ...s,
      agents: s.agents.map(a => a.id === nodeId ? { ...a, system_prompt: newPrompt } : a),
    };
    this.emitSpecChange(updated);
  }

  /** Inspector: regenerate system prompt with AI */
  onRegeneratePrompt(event: RegeneratePromptEvent): void {
    const s = this.spec();
    const nodeId = this.selectedNodeId();
    if (!s || !nodeId) return;
    const node = s.agents.find(a => a.id === nodeId);
    if (!node) return;

    this.promptEditorRef()?.setRegenerating(true);
    this.regeneratePromptRequest.emit({
      nodeId: node.id,
      nodeLabel: node.label,
      nodeDescription: node.description,
      currentPrompt: node.system_prompt,
      instruction: event.instruction,
    });
  }

  /** Called by parent after regeneration completes. */
  applyRegeneratedPrompt(nodeId: string, newPrompt: string): void {
    const s = this.spec();
    if (!s) return;
    const updated: WorkflowSpec = {
      ...s,
      agents: s.agents.map(a => a.id === nodeId ? { ...a, system_prompt: newPrompt } : a),
    };
    this.emitSpecChange(updated);
    this.promptEditorRef()?.setRegenerating(false);
  }

  /** Called by parent if regeneration fails. */
  regenerationFailed(): void {
    this.promptEditorRef()?.setRegenerating(false);
  }

  // ── Graph Editing ────────────────────────────────────────────────────────

  /** Delete a node and all connected edges. */
  deleteNode(nodeId: string): void {
    const s = this.spec();
    if (!s) return;
    const updated: WorkflowSpec = {
      ...s,
      agents: s.agents.filter(a => a.id !== nodeId),
      edges: (s.edges ?? []).filter(e => e.from !== nodeId && e.to !== nodeId),
      conditional_edges: (s.conditional_edges ?? []).filter(ce => ce.from !== nodeId),
      entry_point: s.entry_point === nodeId
        ? (s.agents.find(a => a.id !== nodeId)?.id ?? '')
        : s.entry_point,
    };
    this.selectedNodeId.set(null);
    this.showDeleteConfirm.set(false);
    this.emitSpecChange(updated);
  }

  /** Delete an edge by its source→target pair. */
  deleteEdge(source: string, target: string): void {
    const s = this.spec();
    if (!s) return;
    const updated: WorkflowSpec = {
      ...s,
      edges: (s.edges ?? []).filter(e => !(e.from === source && e.to === target)),
    };
    this.emitSpecChange(updated);
  }

  /** Add a new agent node to the workflow. */
  addNode(label: string, type = 'agent', description = ''): void {
    const s = this.spec();
    if (!s) return;
    const id = label.toLowerCase().replace(/[^a-z0-9]+/g, '_').replace(/^_|_$/g, '');
    if (s.agents.some(a => a.id === id)) return; // Prevent duplicates

    const newAgent: AgentNodeSpec = {
      id,
      type,
      label,
      description,
      system_prompt: `You are a ${label}. ${description}`,
      tools: [],
    };
    const updated: WorkflowSpec = {
      ...s,
      agents: [...s.agents, newAgent],
    };
    this.showAddNodeForm.set(false);
    this.emitSpecChange(updated);
  }

  /** Set a node as the workflow entry point. */
  setEntryPoint(nodeId: string): void {
    const s = this.spec();
    if (!s) return;
    this.emitSpecChange({ ...s, entry_point: nodeId });
  }

  /** Update a node's label. */
  onNodeLabelChange(newLabel: string): void {
    this._updateSelectedNode({ label: newLabel });
  }

  /** Update a node's description. */
  onNodeDescriptionChange(newDescription: string): void {
    this._updateSelectedNode({ description: newDescription });
  }

  /** Update a node's type. */
  onNodeTypeChange(newType: string): void {
    this._updateSelectedNode({ type: newType });
  }

  /** Start inline name editing */
  startNameEdit(): void {
    this.editingName.set(this.spec()?.name ?? '');
    this.isEditingName.set(true);
  }

  /** Commit inline name edit */
  commitNameEdit(): void {
    const s = this.spec();
    if (!s) return;
    const name = this.editingName().trim();
    if (name && name !== s.name) {
      this.emitSpecChange({ ...s, name });
    }
    this.isEditingName.set(false);
  }

  /** Cancel inline name edit */
  cancelNameEdit(): void {
    this.isEditingName.set(false);
  }

  private _updateSelectedNode(patch: Partial<AgentNodeSpec>): void {
    const s = this.spec();
    const nodeId = this.selectedNodeId();
    if (!s || !nodeId) return;
    this.emitSpecChange({
      ...s,
      agents: s.agents.map(a => a.id === nodeId ? { ...a, ...patch } : a),
    });
  }

  // ── Foblex Event Handlers ──────────────────────────────────────────────

  /** Called when the graph first renders — fit canvas to content. */
  onFlowRendered(): void {
    this.fCanvas()?.resetScaleAndCenter(false);
  }

  /** Fit the graph to the current viewport (toolbar button). */
  fitToScreen(): void {
    this.fCanvas()?.resetScaleAndCenter(true);
  }

  /** Handle drag-to-connect: user drew from an output port to an input port. */
  onConnectionCreated(event: FCreateConnectionEvent): void {
    const s = this.spec();
    if (!s) return;

    // Connector IDs follow the pattern: `{nodeId}-output` and `{nodeId}-input`
    const sourceNodeId = event.fOutputId.replace(/-output$/, '');
    const targetNodeId = event.fInputId?.replace(/-input$/, '');

    if (!targetNodeId) return;

    // Prevent duplicate edges
    const exists = (s.edges ?? []).some(e => e.from === sourceNodeId && e.to === targetNodeId);
    if (exists) return;

    const updated: WorkflowSpec = {
      ...s,
      edges: [...(s.edges ?? []), { from: sourceNodeId, to: targetNodeId, condition: 'always', label: '' }],
    };
    this.emitSpecChange(updated);
  }

  /** Handle node position change after user drags a node. */
  onNodePositionChanged(nodeId: string, position: { x: number; y: number }): void {
    // Store position in our layout. Since Foblex manages the visual drag,
    // we only emit a spec change if we're persisting positions.
    // For now, positions are auto-computed from the layout algorithm,
    // so we absorb this event silently.
  }

  // ── Validation ───────────────────────────────────────────────────────────

  readonly validationErrors = computed(() => {
    const s = this.spec();
    if (!s) return [];
    const errors: string[] = [];
    if (!s.entry_point) errors.push('No entry point set');
    if (s.entry_point && !s.agents.some(a => a.id === s.entry_point)) {
      errors.push(`Entry point "${s.entry_point}" references a non-existent node`);
    }
    for (const a of s.agents) {
      if (a.type === 'agent' && !a.system_prompt?.trim()) {
        errors.push(`Agent "${a.label}" has no system prompt`);
      }
    }
    const nodeIds = new Set(s.agents.map(a => a.id));
    nodeIds.add('END');
    for (const e of s.edges ?? []) {
      if (!nodeIds.has(e.from)) errors.push(`Edge references non-existent source "${e.from}"`);
      if (!nodeIds.has(e.to)) errors.push(`Edge references non-existent target "${e.to}"`);
    }
    // Orphan nodes (no edges at all)
    const connectedNodes = new Set<string>();
    for (const e of s.edges ?? []) { connectedNodes.add(e.from); connectedNodes.add(e.to); }
    for (const ce of s.conditional_edges ?? []) {
      connectedNodes.add(ce.from);
      Object.values(ce.condition_mapping).forEach(t => connectedNodes.add(t));
    }
    for (const a of s.agents) {
      if (a.id !== s.entry_point && !connectedNodes.has(a.id)) {
        errors.push(`Agent "${a.label}" is disconnected (no edges)`);
      }
    }
    return errors;
  });

  // ── Run History API ─────────────────────────────────────────────────────

  /** Set the run history (called by parent after fetching from API). */
  setRunHistory(runs: WorkflowRunSummary[]): void {
    this.runHistory.set(runs);
  }

  /** Load historical node outputs from a completed run (called by parent). */
  loadHistoricalRun(runId: string, nodes: Record<string, WorkflowRunNodeDetail>): void {
    this.selectedRunId.set(runId);
    this.historicalNodeOutputs.set(nodes);
    this.showRunHistory.set(false);

    // Apply the historical statuses to the node exec state for visual display
    const execState: Record<string, { status: NodeExecutionStatus; durationMs?: number }> = {};
    for (const [nodeId, detail] of Object.entries(nodes)) {
      execState[nodeId] = { status: detail.status, durationMs: detail.duration_ms };
    }
    this.nodeExecState.set(execState);

    // Calculate total duration from the run
    const durations = Object.values(nodes).map(n => n.duration_ms ?? 0);
    const total = durations.reduce((s, d) => s + d, 0);
    this.totalDurationMs.set(total);
    this.executionStatus.set('completed');
  }

  /** Clear historical state and return to live mode. */
  clearSelectedRun(): void {
    this.selectedRunId.set(null);
    this.historicalNodeOutputs.set({});
    this.showRunHistory.set(false);
    this.resetExecution();
  }

  /** Toggle run history dropdown. */
  toggleRunHistory(): void {
    this.showRunHistory.update(v => !v);
  }

  /** Get node output — from historical data or live execution. */
  getNodeOutput(nodeId: string): string | null {
    const historical = this.historicalNodeOutputs();
    if (historical[nodeId]?.output) return historical[nodeId].output!;
    return null;
  }

  /** Format timestamp to readable time string. */
  formatRunTime(ts: number): string {
    const d = new Date(ts * 1000);
    return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' }) + ' ' +
           d.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
  }

  // ── Layout computation (positions for Foblex fNodePosition) ────────────

  readonly layoutNodes = computed<LayoutNode[]>(() => {
    const s = this.spec();
    if (!s?.agents?.length) return [];
    return this.computeLayout(s);
  });

  /** Foblex edge model — maps spec edges to connector-id pairs. */
  readonly foblexEdges = computed<FoblexEdge[]>(() => {
    const s = this.spec();
    if (!s) return [];
    return this.computeFoblexEdges(s);
  });

  readonly hasEndNode = computed(() => {
    const s = this.spec();
    if (!s) return false;
    return s.edges?.some(e => e.to === 'END');
  });

  readonly endNodePos = computed(() => {
    const nodes = this.layoutNodes();
    if (!nodes.length) return { x: 400, y: 200 };
    const maxX = Math.max(...nodes.map(n => n.x + NODE_WIDTH));
    const avgY = nodes.reduce((sum, n) => sum + n.y, 0) / nodes.length;
    return { x: maxX + 100, y: avgY + NODE_HEIGHT / 2 - 24 };
  });

  readonly selectedNode = computed<LayoutNode | null>(() => {
    const id = this.selectedNodeId();
    if (!id) return null;
    return this.layoutNodes().find(n => n.id === id) || null;
  });

  // ── Interactions ────────────────────────────────────────────────────────

  selectNode(id: string): void {
    this.selectedNodeId.set(this.selectedNodeId() === id ? null : id);
  }

  // ── Layout Algorithm ────────────────────────────────────────────────────

  private computeLayout(spec: WorkflowSpec): LayoutNode[] {
    const agents = spec.agents;
    if (!agents.length) return [];

    // Build adjacency list
    const adj = new Map<string, string[]>();
    const inDeg = new Map<string, number>();
    for (const a of agents) {
      adj.set(a.id, []);
      inDeg.set(a.id, 0);
    }
    for (const e of spec.edges ?? []) {
      if (e.to !== 'END' && adj.has(e.from)) {
        adj.get(e.from)!.push(e.to);
        inDeg.set(e.to, (inDeg.get(e.to) ?? 0) + 1);
      }
    }
    for (const ce of spec.conditional_edges ?? []) {
      if (adj.has(ce.from)) {
        for (const target of Object.values(ce.condition_mapping)) {
          if (target !== 'END' && adj.has(target)) {
            adj.get(ce.from)!.push(target);
            inDeg.set(target, (inDeg.get(target) ?? 0) + 1);
          }
        }
      }
    }

    // Topological sort (BFS) to compute layers
    const layers = new Map<string, number>();
    const queue: string[] = [];
    const visited = new Set<string>();

    // Entry point is always layer 0
    if (spec.entry_point && inDeg.has(spec.entry_point)) {
      queue.push(spec.entry_point);
      layers.set(spec.entry_point, 0);
    }
    // Also add other roots (in-degree 0)
    for (const [id, deg] of inDeg) {
      if (deg === 0 && !layers.has(id)) {
        queue.push(id);
        layers.set(id, 0);
      }
    }

    // Guard: max iterations to prevent hangs in malformed graphs
    const maxIterations = agents.length * agents.length + agents.length;
    let iterations = 0;

    while (queue.length > 0 && iterations < maxIterations) {
      iterations++;
      const node = queue.shift()!;
      if (visited.has(node)) continue;
      visited.add(node);

      const currentLayer = layers.get(node) ?? 0;
      for (const next of adj.get(node) ?? []) {
        const nextLayer = Math.max(layers.get(next) ?? 0, currentLayer + 1);
        layers.set(next, nextLayer);
        if (!visited.has(next)) {
          queue.push(next);
        }
      }
    }

    // Assign layers to any unvisited nodes
    for (const a of agents) {
      if (!layers.has(a.id)) layers.set(a.id, 0);
    }

    // Group by layer
    const layerGroups = new Map<number, string[]>();
    for (const [id, layer] of layers) {
      if (!layerGroups.has(layer)) layerGroups.set(layer, []);
      layerGroups.get(layer)!.push(id);
    }

    // Position nodes
    const agentMap = new Map(agents.map(a => [a.id, a]));
    const layoutNodes: LayoutNode[] = [];

    for (const [layer, nodeIds] of layerGroups) {
      const totalHeight = nodeIds.length * (NODE_HEIGHT + NODE_GAP_Y) - NODE_GAP_Y;
      const startY = PADDING + (Math.max(0, (3 * (NODE_HEIGHT + NODE_GAP_Y) - totalHeight) / 2));

      nodeIds.forEach((id, idx) => {
        const agent = agentMap.get(id);
        if (!agent) return;

        const type = agent.type || 'agent';
        const execState = this.nodeExecState()[agent.id];
        layoutNodes.push({
          id: agent.id,
          label: agent.label || agent.id,
          type,
          description: agent.description || '',
          systemPrompt: agent.system_prompt || '',
          tools: agent.tools || [],
          x: PADDING + layer * LAYER_GAP_X,
          y: startY + idx * (NODE_HEIGHT + NODE_GAP_Y),
          color: NODE_COLORS[type] || NODE_COLORS['default'],
          icon: NODE_ICONS[type] || NODE_ICONS['default'],
          executionStatus: execState?.status ?? 'idle',
          durationMs: execState?.durationMs,
        });
      });
    }

    return layoutNodes;
  }

  /** Build Foblex edge models from spec edges + conditional edges. */
  private computeFoblexEdges(spec: WorkflowSpec): FoblexEdge[] {
    const edges: FoblexEdge[] = [];

    // Regular edges
    for (const e of spec.edges ?? []) {
      const targetId = e.to === 'END' ? 'END' : e.to;
      edges.push({
        id: `${e.from}-${targetId}`,
        sourceOutputId: `${e.from}-output`,
        targetInputId: `${targetId}-input`,
        label: (e as any).label || '',
        isConditional: false,
        condition: (e as any).condition || 'always',
      });
    }

    // Conditional edges
    for (const ce of spec.conditional_edges ?? []) {
      for (const [condition, targetId] of Object.entries(ce.condition_mapping)) {
        const actualTarget = targetId === 'END' ? 'END' : targetId;
        edges.push({
          id: `${ce.from}-${actualTarget}-${condition}`,
          sourceOutputId: `${ce.from}-output`,
          targetInputId: `${actualTarget}-input`,
          label: condition,
          isConditional: true,
          condition,
        });
      }
    }

    return edges;
  }

  // ── Tool Registry ──────────────────────────────────────────────────────

  /** Load tool catalog from parent-provided data. */
  loadToolCatalog(tools: ToolDefinition[]): void {
    this.toolCatalog.set(tools);
    this._toolIndex.clear();
    for (const t of tools) {
      this._toolIndex.set(t.id, t);
    }
  }

  getToolName(toolId: string): string {
    return this._toolIndex.get(toolId)?.name ?? toolId;
  }

  getToolIcon(toolId: string): string {
    return this._toolIndex.get(toolId)?.icon ?? '🔧';
  }

  getToolDescription(toolId: string): string {
    return this._toolIndex.get(toolId)?.description ?? toolId;
  }

  addTool(toolId: string): void {
    const node = this.selectedNode();
    if (!node || node.tools.includes(toolId)) return;

    const spec = this.spec();
    if (!spec) return;

    const updatedAgents = (spec.agents ?? []).map(a => {
      if (a.id !== node.id) return a;
      return { ...a, tools: [...a.tools, toolId] };
    });

    this.emitSpecChange({ ...spec, agents: updatedAgents });
    console.log(`[Canvas] added tool "${toolId}" to node "${node.id}"`);
  }

  removeTool(toolId: string): void {
    const node = this.selectedNode();
    if (!node) return;

    const spec = this.spec();
    if (!spec) return;

    const updatedAgents = (spec.agents ?? []).map(a => {
      if (a.id !== node.id) return a;
      return { ...a, tools: a.tools.filter(t => t !== toolId) };
    });

    this.emitSpecChange({ ...spec, agents: updatedAgents });
    this.showToolPicker.set(false);
    console.log(`[Canvas] removed tool "${toolId}" from node "${node.id}"`);
  }
}
