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
  ChangeDetectionStrategy,
  viewChild,
  HostListener,
  inject,
  effect,
  DestroyRef,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { CollaborationService, CollabMessage } from './collaboration.service';
import { MarkdownViewerComponent } from './markdown-viewer.component';
import { PromptEditorComponent, RegeneratePromptEvent } from './prompt-editor.component';
import {
  FFlowModule,
  FCanvasComponent,
  FCreateConnectionEvent,
  FZoomDirective,
} from '@foblex/flow';
import type {
  WorkflowSpec, AgentNodeSpec, NodeExecutionStatus,
  WorkflowRunSummary, WorkflowRunNodeDetail, ToolDefinition, ToolSpec, LLMSpec,
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
  tools: (string | ToolSpec)[];
  llm?: LLMSpec;
  x: number;
  y: number;
  color: string;
  icon: string;
  executionStatus: NodeExecutionStatus | 'idle';
  durationMs?: number;
}

/** Context menu model. */
interface ContextMenu {
  x: number;
  y: number;
  target: 'canvas' | 'node' | 'edge';
  nodeId?: string;
  edgeId?: string;
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
  readonly runWorkflow = output<Record<string, unknown>>();
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
  readonly shareWorkflowRequest = output<string>();

  // ── Child refs ──────────────────────────────────────────────────────────
  protected readonly promptEditorRef = viewChild<PromptEditorComponent>('promptEditorRef');

  private readonly collabService = inject(CollaborationService);
  private readonly destroyRef = inject(DestroyRef);

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
  
  readonly selectedEdgeId = signal<string | null>(null);

  // ── Resizable Detail Panel ────────────────────────────────────────────
  private static readonly MIN_PANEL_WIDTH = 280;
  private static readonly MAX_PANEL_WIDTH = 600;
  private static readonly DEFAULT_PANEL_WIDTH = 360;
  private static readonly PANEL_WIDTH_KEY = 'synaptiq:workflow-panel-width';

  readonly panelWidth = signal(this._loadPanelWidth());
  readonly detailCollapsed = signal(false);

  private _resizing = false;
  private _resizeStartX = 0;
  private _resizeStartWidth = 0;

  private _loadPanelWidth(): number {
    try {
      const stored = localStorage.getItem(WorkflowCanvasComponent.PANEL_WIDTH_KEY);
      if (stored) {
        const val = parseInt(stored, 10);
        if (!isNaN(val) && val >= WorkflowCanvasComponent.MIN_PANEL_WIDTH && val <= WorkflowCanvasComponent.MAX_PANEL_WIDTH) {
          return val;
        }
      }
    } catch { /* ignore */ }
    return WorkflowCanvasComponent.DEFAULT_PANEL_WIDTH;
  }

  private _savePanelWidth(width: number): void {
    try { localStorage.setItem(WorkflowCanvasComponent.PANEL_WIDTH_KEY, String(width)); } catch { /* ignore */ }
  }

  onResizeStart(event: MouseEvent): void {
    event.preventDefault();
    this._resizing = true;
    this._resizeStartX = event.clientX;
    this._resizeStartWidth = this.panelWidth();

    const onMove = (e: MouseEvent) => {
      if (!this._resizing) return;
      const delta = this._resizeStartX - e.clientX; // Moving left = bigger panel
      const newWidth = Math.min(
        WorkflowCanvasComponent.MAX_PANEL_WIDTH,
        Math.max(WorkflowCanvasComponent.MIN_PANEL_WIDTH, this._resizeStartWidth + delta),
      );
      this.panelWidth.set(newWidth);
    };

    const onUp = () => {
      this._resizing = false;
      this._savePanelWidth(this.panelWidth());
      document.removeEventListener('mousemove', onMove);
      document.removeEventListener('mouseup', onUp);
      document.body.style.cursor = '';
      document.body.style.userSelect = '';
    };

    document.addEventListener('mousemove', onMove);
    document.addEventListener('mouseup', onUp);
    document.body.style.cursor = 'col-resize';
    document.body.style.userSelect = 'none';
  }

  onResizeTouchStart(event: TouchEvent): void {
    if (!event.touches.length) return;
    event.preventDefault();
    this._resizing = true;
    this._resizeStartX = event.touches[0].clientX;
    this._resizeStartWidth = this.panelWidth();

    const onTouchMove = (e: TouchEvent) => {
      if (!this._resizing || !e.touches.length) return;
      const delta = this._resizeStartX - e.touches[0].clientX;
      const newWidth = Math.min(
        WorkflowCanvasComponent.MAX_PANEL_WIDTH,
        Math.max(WorkflowCanvasComponent.MIN_PANEL_WIDTH, this._resizeStartWidth + delta),
      );
      this.panelWidth.set(newWidth);
    };

    const onTouchEnd = () => {
      this._resizing = false;
      this._savePanelWidth(this.panelWidth());
      document.removeEventListener('touchmove', onTouchMove);
      document.removeEventListener('touchend', onTouchEnd);
    };

    document.addEventListener('touchmove', onTouchMove, { passive: false });
    document.addEventListener('touchend', onTouchEnd);
  }

  /** Context menu state */
  readonly contextMenu = signal<ContextMenu | null>(null);

  /** Node positions persisted by user (overrides auto-layout). Keyed by node ID. */
  private _userPositions = new Map<string, { x: number; y: number }>();

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
      if (query && !tool.name.toLowerCase().includes(query) && !(tool.description ?? '').toLowerCase().includes(query)) {
        continue;
      }
      const cat = tool.category ?? 'General';
      if (!groups.has(cat)) {
        groups.set(cat, { key: cat, label: cat, tools: [] });
      }
      const group = groups.get(cat);
      if (group) group.tools.push(tool);
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

  /** Active Users */
  readonly activeUsers = signal<any[]>([]);

  /** Remote cursors state */
  readonly remoteCursors = signal<Record<string, { id: string, name: string, x: number, y: number, color: string }>>({});
  readonly remoteCursorsList = computed(() => Object.values(this.remoteCursors()));

  /** Remote node selections state */
  readonly remoteSelections = signal<Record<string, { nodeId: string, color: string, name: string }>>({});
  readonly remoteSelectionsList = computed(() => Object.values(this.remoteSelections()));

  getRemoteSelectionsForNode(nodeId: string): { color: string, name: string }[] {
    return this.remoteSelectionsList().filter(sel => sel.nodeId === nodeId);
  }

  constructor() {
    effect(() => {
      const s = this.spec();
      if (s?.id) {
        this.collabService.connect(s.id);
      } else {
        this.collabService.disconnect();
      }
    });

    const sub = this.collabService.messages$.subscribe((msg: CollabMessage) => {
      this.handleCollabMessage(msg);
    });

    this.destroyRef.onDestroy(() => {
      sub.unsubscribe();
      this.collabService.disconnect();
    });
  }

  private handleCollabMessage(msg: CollabMessage): void {
    switch (msg.type) {
      case 'user_joined':
      case 'user_left':
      case 'active_users':
        if (msg.users) {
          this.activeUsers.set(msg.users);
        } else if (msg.type === 'user_joined' && msg.user) {
          this.activeUsers.update(u => [...u.filter(x => x.id !== msg.user.id), msg.user]);
        } else if (msg.type === 'user_left' && msg.user) {
          this.activeUsers.update(u => u.filter(x => x.id !== msg.user.id));
          this.remoteCursors.update(c => {
            const next = { ...c };
            delete next[msg.user.id];
            return next;
          });
          this.remoteSelections.update(sels => {
            const next = { ...sels };
            delete next[msg.user.id];
            return next;
          });
        }
        break;
      case 'cursor_move':
        if (msg.user_id && msg.x !== undefined && msg.y !== undefined) {
          this.remoteCursors.update(cursors => ({
            ...cursors,
            [msg.user_id!]: {
              id: msg.user_id!,
              name: msg.user?.name || 'Guest',
              x: msg.x!,
              y: msg.y!,
              color: msg.user?.color || '#ff0000'
            }
          }));
        }
        break;
      case 'spec_change':
        if (msg.spec) {
          this.specChange.emit(msg.spec);
        }
        break;
      case 'node_selected':
        if (msg.user_id && msg.node_id) {
          this.remoteSelections.update(sels => ({
            ...sels,
            [msg.user_id!]: {
              nodeId: msg.node_id!,
              color: msg.user?.color || '#ff0000',
              name: msg.user?.name || 'Guest'
            }
          }));
        } else if (msg.user_id && !msg.node_id) {
          this.remoteSelections.update(sels => {
            const next = { ...sels };
            delete next[msg.user_id!];
            return next;
          });
        }
        break;
    }
  }

  private _lastCursorMove = 0;
  private _cursorThrottleMs = 50;

  @HostListener('mousemove', ['$event'])
  onMouseMove(event: MouseEvent): void {
    if (this.spec()?.id) {
      const now = Date.now();
      if (now - this._lastCursorMove > this._cursorThrottleMs) {
        this._lastCursorMove = now;
        const rect = (event.currentTarget as HTMLElement).getBoundingClientRect();
        const x = event.clientX - rect.left;
        const y = event.clientY - rect.top;
        this.collabService.sendMessage({ type: 'cursor_move', x, y });
      }
    }
  }

  /**
   * Central method for all spec mutations.
   * Pushes current state onto undo stack, clears redo, and emits the change.
   */
  protected emitSpecChange(updated: WorkflowSpec, broadcast = true): void {
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
    
    if (broadcast && updated.id) {
      this.collabService.sendMessage({ type: 'spec_change', spec: updated });
    }
  }

  undo(): void {
    if (!this._undoStack.length) return;
    const current = this.spec();
    if (current) {
      this._redoStack.push(JSON.stringify(current));
    }
    const popPrev = this._undoStack.pop();
    if (!popPrev) return;
    const prev = JSON.parse(popPrev) as WorkflowSpec;
    this._syncUndoRedoSignals();
    this.specChange.emit(prev); // Direct emit — don't push to undo again
    if (prev.id) this.collabService.sendMessage({ type: 'spec_change', spec: prev });
    console.log('[Canvas] undo');
  }

  redo(): void {
    if (!this._redoStack.length) return;
    const current = this.spec();
    if (current) {
      this._undoStack.push(JSON.stringify(current));
    }
    const popNext = this._redoStack.pop();
    if (!popNext) return;
    const next = JSON.parse(popNext) as WorkflowSpec;
    this._syncUndoRedoSignals();
    this.specChange.emit(next); // Direct emit — don't push to undo
    if (next.id) this.collabService.sendMessage({ type: 'spec_change', spec: next });
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
    const activeTag = document.activeElement?.tagName;
    const inInput = activeTag === 'INPUT' || activeTag === 'TEXTAREA';

    // Ctrl+Z — Undo
    if (ctrl && event.key === 'z' && !event.shiftKey) {
      event.preventDefault();
      this.undo();
    }
    // Ctrl+Shift+Z / Ctrl+Y — Redo
    else if (ctrl && ((event.key === 'z' && event.shiftKey) || event.key === 'y')) {
      event.preventDefault();
      this.redo();
    }
    // Ctrl+S — Save (emit specChange to trigger auto-save)
    else if (ctrl && event.key === 's') {
      event.preventDefault();
      const s = this.spec();
      if (s) this.specChange.emit(s);
    }
    // Ctrl+D — Duplicate selected node
    else if (ctrl && event.key === 'd' && !inInput) {
      event.preventDefault();
      this.duplicateSelectedNode();
    }
    // Ctrl+Enter — Run workflow
    else if (ctrl && event.key === 'Enter') {
      event.preventDefault();
      if (this.executionStatus() !== 'running') {
        this.runWorkflow.emit({});
      }
    }
    // Escape — Deselect / close panels
    else if (event.key === 'Escape') {
      this.selectedNodeId.set(null);
      this.selectedEdgeId.set(null);
      this.showAddNodeForm.set(false);
      this.showToolPicker.set(false);
      this.contextMenu.set(null);
      if (this.spec()?.id) {
        this.collabService.sendMessage({ type: 'node_selected', node_id: undefined });
      }
    }
    // Delete / Backspace — Delete selected node or edge
    else if ((event.key === 'Delete' || event.key === 'Backspace') && !inInput) {
      if (this.selectedNodeId()) {
        this.showDeleteConfirm.set(true);
      } else if (this.selectedEdgeId()) {
        this.deleteSelectedEdge();
      }
    }
  }

  /** Close context menu on any click. */
  @HostListener('document:click')
  onDocClick(): void {
    this.contextMenu.set(null);
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
      agents: (s.agents ?? []).map(a => a.id === nodeId ? { ...a, system_prompt: newPrompt } : a),
    };
    this.emitSpecChange(updated);
  }

  /** Inspector: regenerate system prompt with AI */
  onRegeneratePrompt(event: RegeneratePromptEvent): void {
    const s = this.spec();
    const nodeId = this.selectedNodeId();
    if (!s || !nodeId) return;
    const node = (s.agents ?? []).find(a => a.id === nodeId);
    if (!node) return;

    this.promptEditorRef()?.setRegenerating(true);
    this.regeneratePromptRequest.emit({
      nodeId: node.id,
      nodeLabel: node.name ?? node.id,
      nodeDescription: node.description ?? '',
      currentPrompt: node.systemPrompt ?? '',
      instruction: event.instruction,
    });
  }

  /** Called by parent after regeneration completes. */
  applyRegeneratedPrompt(nodeId: string, newPrompt: string): void {
    const s = this.spec();
    if (!s) return;
    const updated: WorkflowSpec = {
      ...s,
      agents: (s.agents ?? []).map(a => a.id === nodeId ? { ...a, system_prompt: newPrompt } : a),
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
    const updated = {
      ...s,
      agents: (s.agents ?? []).filter(a => a.id !== nodeId),
      edges: (s.edges ?? []).filter(e => e.source !== nodeId && e.target !== nodeId),
      conditionalEdges: ((s as any).conditionalEdges ?? []).filter((ce: any) => ce.source !== nodeId),
      entrypoint: s.entrypoint === nodeId
        ? ((s.agents ?? []).find(a => a.id !== nodeId)?.id ?? '')
        : s.entrypoint,
    } as WorkflowSpec;
    this.selectedNodeId.set(null);
    this.selectedEdgeId.set(null);
    this.showDeleteConfirm.set(false);
    this.emitSpecChange(updated);
    if (this.spec()?.id) {
      this.collabService.sendMessage({ type: 'node_selected', node_id: undefined });
    }
  }

  /** Delete an edge by its source→target pair. */
  deleteEdge(source: string, target: string): void {
    const s = this.spec();
    if (!s) return;
    const updated: WorkflowSpec = {
      ...s,
      edges: (s.edges ?? []).filter(e => !(e.source === source && e.target === target)),
    };
    this.emitSpecChange(updated);
  }

  deleteSelectedEdge(): void {
    const edge = this.selectedEdge();
    if (!edge) return;
    const s = this.spec();
    if (!s) return;

    const fromId = edge.sourceOutputId.replace(/-output$/, '');
    const targetId = edge.targetInputId.replace(/-input$/, '');

    let updated: WorkflowSpec;

    if (edge.isConditional) {
      const conditionalEdges = [...((s as any).conditionalEdges ?? [])];
      const ceIndex = conditionalEdges.findIndex(ce => ce.source === fromId);
      if (ceIndex >= 0) {
        const ce = { ...conditionalEdges[ceIndex] };
        const mapping = { ...ce.conditionMapping };
        const condition = edge.condition;
        delete mapping[condition];
        
        if (Object.keys(mapping).length === 0) {
          conditionalEdges.splice(ceIndex, 1);
        } else {
          ce.conditionMapping = mapping;
          conditionalEdges[ceIndex] = ce;
        }
      }
      updated = { ...s, conditionalEdges: conditionalEdges } as unknown as WorkflowSpec;
    } else {
      updated = {
        ...s,
        edges: (s.edges ?? []).filter(e => !(e.source === fromId && e.target === targetId)),
      };
    }

    this.selectedEdgeId.set(null);
    this.emitSpecChange(updated);
  }

  onEdgeConditionChange(newCondition: string): void {
    const edge = this.selectedEdge();
    if (!edge || !edge.isConditional) return;
    const s = this.spec();
    if (!s) return;

    const fromId = edge.sourceOutputId.replace(/-output$/, '');
    const targetId = edge.targetInputId.replace(/-input$/, '');

    const conditionalEdges = [...((s as any).conditionalEdges ?? [])];
    const ceIndex = conditionalEdges.findIndex(ce => ce.source === fromId);
    if (ceIndex >= 0) {
      const ce = { ...conditionalEdges[ceIndex] };
      const mapping = { ...ce.conditionMapping };
      
      const oldCondition = edge.condition;
      
      if (newCondition && newCondition !== oldCondition) {
         mapping[newCondition] = mapping[oldCondition];
         delete mapping[oldCondition];
         ce.conditionMapping = mapping;
         conditionalEdges[ceIndex] = ce;
         
         this.emitSpecChange({ ...s, conditionalEdges: conditionalEdges } as unknown as WorkflowSpec);
         
         const newEdgeId = `${fromId}-${targetId}-${newCondition}`;
         this.selectedEdgeId.set(newEdgeId);
      }
    }
  }

  /** Add a new agent node to the workflow. */
  addNode(label: string, type = 'agent', description = ''): void {
    const s = this.spec();
    if (!s) return;
    const id = label.toLowerCase().replace(/[^a-z0-9]+/g, '_').replace(/^_|_$/g, '');
    if ((s.agents ?? []).some(a => a.id === id)) return; // Prevent duplicates

    const newAgent: AgentNodeSpec = {
      id,
      name: label,
      description,
      systemPrompt: `You are a ${label}. ${description}`,
      tools: [],
    };
    const updated: WorkflowSpec = {
      ...s,
      agents: [...(s.agents ?? []), newAgent],
    };
    this.showAddNodeForm.set(false);
    this.emitSpecChange(updated);
  }

  /** Set a node as the workflow entry point. */
  setEntryPoint(nodeId: string): void {
    const s = this.spec();
    if (!s) return;
    this.emitSpecChange({ ...s, entrypoint: nodeId });
  }

  /** Update a node's label. */
  onNodeLabelChange(newLabel: string): void {
    this._updateSelectedNode({ name: newLabel } as Partial<AgentNodeSpec>);
  }

  /** Update a node's description. */
  onNodeDescriptionChange(newDescription: string): void {
    this._updateSelectedNode({ description: newDescription });
  }

  /** Update a node's type. */
  onNodeTypeChange(newType: string): void {
    this._updateSelectedNode({ description: newType } as Partial<AgentNodeSpec>);
  }

  /** Update a node's LLM provider. */
  onNodeLlmProviderChange(provider: string): void {
    const node = this.selectedNode();
    if (!node) return;
    const llm = { ...(node.llm ?? {}), provider } as any;
    this._updateSelectedNode({ llm } as Partial<AgentNodeSpec>);
  }

  /** Update a node's LLM model. */
  onNodeLlmModelChange(model: string): void {
    const node = this.selectedNode();
    if (!node) return;
    const llm = { ...(node.llm ?? {}), model } as any;
    this._updateSelectedNode({ llm } as Partial<AgentNodeSpec>);
  }

  /** Update a node's LLM temperature. */
  onNodeLlmTemperatureChange(temp: number): void {
    const node = this.selectedNode();
    if (!node) return;
    const llm = { ...(node.llm ?? {}), temperature: temp } as any;
    this._updateSelectedNode({ llm } as Partial<AgentNodeSpec>);
  }

  /** Duplicate the selected node. */
  duplicateSelectedNode(): void {
    const node = this.selectedNode();
    const s = this.spec();
    if (!node || !s) return;
    const suffix = '_copy';
    const newId = node.id + suffix;
    const newAgent: AgentNodeSpec = {
      id: newId,
      name: node.label + ' (Copy)',
      description: node.description,
      systemPrompt: node.systemPrompt,
      tools: node.tools.filter((t): t is ToolSpec => typeof t !== 'string') as ToolSpec[],
    };
    this.emitSpecChange({
      ...s,
      agents: [...(s.agents ?? []), newAgent],
    });
  }

  // ── Context Menu ───────────────────────────────────────────────────────

  openContextMenu(event: MouseEvent, target: 'canvas' | 'node' | 'edge', id?: string): void {
    event.preventDefault();
    event.stopPropagation();
    this.contextMenu.set({
      x: event.clientX,
      y: event.clientY,
      target,
      nodeId: target === 'node' ? id : undefined,
      edgeId: target === 'edge' ? id : undefined,
    });
  }

  /** Auto-reset all node positions to the computed layout. */
  autoLayout(): void {
    this._userPositions.clear();
    // Force re-computation by emitting the same spec
    const s = this.spec();
    if (s) this.specChange.emit(s);
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
      agents: (s.agents ?? []).map(a => a.id === nodeId ? { ...a, ...patch } : a),
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
    const exists = (s.edges ?? []).some(e => e.source === sourceNodeId && e.target === targetNodeId);
    if (exists) return;

    const updated: WorkflowSpec = {
      ...s,
      edges: [...(s.edges ?? []), { source: sourceNodeId, target: targetNodeId }],
    };
    this.emitSpecChange(updated);
  }

  /** Handle node position change after user drags a node — persist position. */
  onNodePositionChanged(nodeId: string, x: number, y: number): void {
    this._userPositions.set(nodeId, { x, y });
  }

  // ── Validation ───────────────────────────────────────────────────────────

  readonly validationErrors = computed(() => {
    const s = this.spec();
    if (!s) return [];
    const errors: string[] = [];
    if (!s.entrypoint) errors.push('No entry point set');
    const agents = s.agents ?? [];
    if (s.entrypoint && !agents.some(a => a.id === s.entrypoint)) {
      errors.push(`Entry point "${s.entrypoint}" references a non-existent node`);
    }
    for (const a of agents) {
      if (!a.systemPrompt?.trim()) {
        errors.push(`Agent "${a.name}" has no system prompt`);
      }
    }
    const nodeIds = new Set(agents.map(a => a.id));
    nodeIds.add('END');
    for (const e of s.edges ?? []) {
      if (!nodeIds.has(e.source)) errors.push(`Edge references non-existent source "${e.source}"`);
      if (!nodeIds.has(e.target)) errors.push(`Edge references non-existent target "${e.target}"`);
    }
    // Orphan nodes (no edges at all)
    const connectedNodes = new Set<string>();
    for (const e of s.edges ?? []) { connectedNodes.add(e.source); connectedNodes.add(e.target); }
    for (const ce of (s as any).conditionalEdges ?? []) {
      connectedNodes.add(ce.source);
      Object.values(ce.conditionMapping).forEach((t: any) => connectedNodes.add(t as string));
    }
    for (const a of (s.agents ?? [])) {
      if (a.id !== s.entrypoint && !connectedNodes.has(a.id)) {
        errors.push(`Agent "${a.name}" is disconnected (no edges)`);
      }
    }

    // Cycle detection
    if (this.cycleNodes().size > 0) {
      errors.push('Workflow contains one or more cycles, which are not allowed');
    }

    return errors;
  });

  readonly cycleNodes = computed(() => {
    const s = this.spec();
    if (!s) return new Set<string>();

    const inDegree = new Map<string, number>();
    const adj = new Map<string, string[]>();
    for (const a of (s.agents ?? [])) {
      inDegree.set(a.id, 0);
      adj.set(a.id, []);
    }
    inDegree.set('END', 0);
    adj.set('END', []);

    const addEdge = (from: string, to: string) => {
      if (adj.has(from) && adj.has(to)) {
        adj.get(from)!.push(to);
        inDegree.set(to, inDegree.get(to)! + 1);
      }
    };

    for (const e of s.edges ?? []) { addEdge(e.source, e.target); }
    for (const ce of (s as any).conditionalEdges ?? []) {
      for (const target of Object.values(ce.conditionMapping) as string[]) { addEdge(ce.source, target); }
    }

    const inDegreeCopy = new Map(inDegree);
    const q: string[] = [];
    for (const [node, deg] of inDegreeCopy.entries()) {
      if (deg === 0) q.push(node);
    }

    let visitedCount = 0;
    while (q.length > 0) {
      const u = q.shift()!;
      visitedCount++;
      for (const v of adj.get(u)!) {
        const newDeg = inDegreeCopy.get(v)! - 1;
        inDegreeCopy.set(v, newDeg);
        if (newDeg === 0) q.push(v);
      }
    }

    const cycles = new Set<string>();
    if (visitedCount !== adj.size) {
      for (const [node, deg] of inDegreeCopy.entries()) {
        if (deg > 0) cycles.add(node);
      }
    }
    return cycles;
  });

  readonly invalidRunFromNodes = computed(() => {
    const s = this.spec();
    if (!s) return new Set<string>();
    
    const invalidSet = new Set<string>();
    const inDegree = new Map<string, number>();
    
    for (const a of (s.agents ?? [])) {
      inDegree.set(a.id, 0);
    }
    inDegree.set('END', 0);
    
    const addEdge = (from: string, to: string) => {
      if (inDegree.has(to)) {
        inDegree.set(to, inDegree.get(to)! + 1);
      }
    };
    
    for (const e of s.edges ?? []) { addEdge(e.source, e.target); }
    for (const ce of (s as any).conditionalEdges ?? []) {
      for (const target of Object.values(ce.conditionMapping) as string[]) { addEdge(ce.source, target); }
    }
    
    // Nodes with no incoming edges (except entry point)
    for (const a of (s.agents ?? [])) {
      if (a.id !== s.entrypoint && inDegree.get(a.id) === 0) {
        invalidSet.add(a.id);
      }
    }
    
    for (const node of this.cycleNodes()) {
      invalidSet.add(node);
    }
    
    return invalidSet;
  });

  // ── Run History API ─────────────────────────────────────────────────────

  /** Set the run history (called by parent after fetching from API). */
  setRunHistory(runs: WorkflowRunSummary[]): void {
    this.runHistory.set(runs);
  }

  /** Load historical node outputs from a completed run (called by parent). */
  loadHistoricalRun(runId: string, nodes?: Record<string, WorkflowRunNodeDetail>): void {
    if (!nodes) return;
    this.selectedRunId.set(runId);
    this.historicalNodeOutputs.set(nodes);
    this.showRunHistory.set(false);

    // Apply the historical statuses to the node exec state for visual display
    // SDK uses UPPERCASE enums, canvas uses lowercase
    const execState: Record<string, { status: NodeExecutionStatus; durationMs?: number }> = {};
    for (const [nodeId, detail] of Object.entries(nodes)) {
      const status = (detail.status?.toLowerCase() ?? 'pending') as NodeExecutionStatus;
      execState[nodeId] = { status, durationMs: detail.durationMs };
    }
    this.nodeExecState.set(execState);

    // Calculate total duration from the run
    const durations = Object.values(nodes).map(n => n.durationMs ?? 0);
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
    if (historical[nodeId]?.output) return historical[nodeId].output ?? null;
    return null;
  }

  /** Format timestamp to readable time string. */
  formatRunTime(ts: string | number): string {
    const d = typeof ts === 'string' ? new Date(ts) : new Date(ts * 1000);
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
    return s.edges?.some(e => e.target === 'END');
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

  readonly selectedEdge = computed<FoblexEdge | null>(() => {
    const id = this.selectedEdgeId();
    if (!id) return null;
    return this.foblexEdges().find(e => e.id === id) || null;
  });

  // ── Interactions ────────────────────────────────────────────────────────

  selectNode(id: string): void {
    const isDeselect = this.selectedNodeId() === id;
    this.selectedNodeId.set(isDeselect ? null : id);
    this.selectedEdgeId.set(null);
    if (this.spec()?.id) {
      this.collabService.sendMessage({ type: 'node_selected', node_id: isDeselect ? undefined : id });
    }
  }

  selectEdge(id: string): void {
    const isDeselect = this.selectedEdgeId() === id;
    this.selectedEdgeId.set(isDeselect ? null : id);
    if (this.selectedNodeId() !== null) {
      this.selectedNodeId.set(null);
      if (this.spec()?.id) {
        this.collabService.sendMessage({ type: 'node_selected', node_id: undefined });
      }
    }
  }

  // ── Layout Algorithm ────────────────────────────────────────────────────

  private computeLayout(spec: WorkflowSpec): LayoutNode[] {
    const agents = spec.agents ?? [];
    if (!agents.length) return [];

    // Build adjacency list
    const adj = new Map<string, string[]>();
    const inDeg = new Map<string, number>();
    for (const a of agents) {
      adj.set(a.id, []);
      inDeg.set(a.id, 0);
    }
    for (const e of spec.edges ?? []) {
      if (e.target !== 'END' && adj.has(e.source)) {
        const fromAdj = adj.get(e.source);
        if (fromAdj) fromAdj.push(e.target);
        inDeg.set(e.target, (inDeg.get(e.target) ?? 0) + 1);
      }
    }
    for (const ce of (spec as any).conditionalEdges ?? []) {
      if (adj.has(ce.source)) {
        for (const target of Object.values(ce.conditionMapping) as string[]) {
          if (target !== 'END' && adj.has(target)) {
            const fromAdj = adj.get(ce.source);
            if (fromAdj) fromAdj.push(target);
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
    if (spec.entrypoint && inDeg.has(spec.entrypoint)) {
      queue.push(spec.entrypoint);
      layers.set(spec.entrypoint, 0);
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
      const node = queue.shift();
      if (!node) continue;
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
      const group = layerGroups.get(layer);
      if (group) group.push(id);
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

        const type = 'agent';
        const execState = this.nodeExecState()[agent.id];
        const userPos = this._userPositions.get(agent.id);
        layoutNodes.push({
          id: agent.id,
          label: agent.name || agent.id,
          type,
          description: agent.description || '',
          systemPrompt: agent.systemPrompt || '',
          tools: agent.tools || [],
          llm: (agent as any).llm,
          x: userPos?.x ?? (PADDING + layer * LAYER_GAP_X),
          y: userPos?.y ?? (startY + idx * (NODE_HEIGHT + NODE_GAP_Y)),
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
      const targetId = e.target === 'END' ? 'END' : e.target;
      edges.push({
        id: `${e.source}-${targetId}`,
        sourceOutputId: `${e.source}-output`,
        targetInputId: `${targetId}-input`,
        label: (e as unknown as Record<string, string>)['label'] || '',
        isConditional: false,
        condition: (e as unknown as Record<string, string>)['condition'] || 'always',
      });
    }

    // Conditional edges
    for (const ce of (spec as any).conditionalEdges ?? []) {
      for (const [condition, targetId] of Object.entries(ce.conditionMapping)) {
        const actualTarget = targetId === 'END' ? 'END' : targetId;
        edges.push({
          id: `${ce.source}-${actualTarget}-${condition}`,
          sourceOutputId: `${ce.source}-output`,
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

  /** Extract string ID from a tool reference (string or object with id). */
  resolveToolId(tool: string | ToolSpec | ToolDefinition): string {
    return typeof tool === 'string' ? tool : (tool.id ?? '');
  }

  /** Check if a tools array contains a given tool ID. */
  hasToolId(tools: (string | ToolSpec | ToolDefinition)[], toolId: string): boolean {
    return tools.some(t => (typeof t === 'string' ? t : (t.id ?? '')) === toolId);
  }

  getToolName(tool: string | ToolSpec | ToolDefinition): string {
    const id = this.resolveToolId(tool);
    return this._toolIndex.get(id)?.name ?? (typeof tool !== 'string' && 'name' in tool ? tool.name ?? id : id);
  }

  getToolIcon(tool: string | ToolSpec | ToolDefinition): string {
    const id = this.resolveToolId(tool);
    return this._toolIndex.get(id)?.icon ?? '🔧';
  }

  getToolDescription(tool: string | ToolSpec | ToolDefinition): string {
    const id = this.resolveToolId(tool);
    return this._toolIndex.get(id)?.description ?? id;
  }

  addTool(toolId: string): void {
    const node = this.selectedNode();
    if (!node || this.hasToolId(node.tools, toolId)) return;

    const spec = this.spec();
    if (!spec) return;

    const updatedAgents = (spec.agents ?? []).map(a => {
      if (a.id !== node.id) return a;
      return { ...a, tools: [...(a.tools ?? []), { id: toolId } as ToolSpec] };
    });

    this.emitSpecChange({ ...spec, agents: updatedAgents });
    console.log(`[Canvas] added tool "${toolId}" to node "${node.id}"`);
  }

  removeTool(tool: string | ToolSpec | ToolDefinition): void {
    const toolId = this.resolveToolId(tool);
    const node = this.selectedNode();
    if (!node) return;

    const spec = this.spec();
    if (!spec) return;

    const updatedAgents = (spec.agents ?? []).map(a => {
      if (a.id !== node.id) return a;
      return { ...a, tools: (a.tools ?? []).filter(t => (typeof t === 'string' ? t : (t.id ?? '')) !== toolId) };
    });

    this.emitSpecChange({ ...spec, agents: updatedAgents });
    this.showToolPicker.set(false);
    console.log(`[Canvas] removed tool "${toolId}" from node "${node.id}"`);
  }

  exportJson(): void {
    const spec = this.spec();
    if (!spec) return;
    
    const dataStr = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(spec, null, 2));
    const downloadAnchorNode = document.createElement('a');
    downloadAnchorNode.setAttribute("href", dataStr);
    downloadAnchorNode.setAttribute("download", (spec.name || 'workflow') + ".json");
    document.body.appendChild(downloadAnchorNode);
    downloadAnchorNode.click();
    downloadAnchorNode.remove();
  }

  importJson(event: Event): void {
    const target = event.target as HTMLInputElement;
    const file = target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (e) => {
      try {
        const content = e.target?.result as string;
        const spec = JSON.parse(content);
        
        // Ensure imported spec maintains structural integrity if needed
        // but we'll assume it's valid for now.
        this.emitSpecChange(spec);
        console.log(`[Canvas] Imported workflow "${spec.name}"`);
      } catch (err) {
        console.error('[Canvas] Error parsing imported JSON', err);
        alert('Invalid JSON file.');
      }
      target.value = '';
    };
    reader.readAsText(file);
  }
}
