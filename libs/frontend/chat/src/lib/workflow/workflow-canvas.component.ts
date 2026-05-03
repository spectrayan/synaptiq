/**
 * WorkflowCanvasComponent — Interactive SVG-based workflow graph visualizer.
 *
 * Renders agent nodes and edges with auto-layout using a simple layered algorithm.
 * Features:
 *   - Auto-layout (topological sort + layered positioning)
 *   - Node hover/click to inspect agent details
 *   - Animated edges with directional arrows
 *   - Zoom & pan via scroll/drag
 *   - Responsive sizing
 */
import {
  Component,
  input,
  output,
  signal,
  computed,
  effect,
  ChangeDetectionStrategy,
  ElementRef,
  viewChild,
  HostListener,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import type {
  WorkflowSpec, AgentNodeSpec, EdgeSpec, ConditionalEdgeSpec, NodeExecutionStatus,
  WorkflowRunSummary, WorkflowRunNodeDetail,
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
  width: number;
  height: number;
  layer: number;
  color: string;
  icon: string;
  executionStatus: NodeExecutionStatus | 'idle';
  durationMs?: number;
}

interface LayoutEdge {
  id: string;
  source: string;
  target: string;
  label: string;
  condition: string;
  isConditional: boolean;
  path: string;
  labelX: number;
  labelY: number;
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
  imports: [CommonModule, FormsModule, MatIconModule, MatTooltipModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './workflow-canvas.component.html',
  styleUrl: './workflow-canvas.component.scss',
})
export class WorkflowCanvasComponent {
  // ── Inputs ──────────────────────────────────────────────────────────────
  readonly spec = input<WorkflowSpec | null>(null);

  // ── Outputs ─────────────────────────────────────────────────────────────
  readonly runWorkflow = output<void>();
  readonly stopExecution = output<void>();
  readonly specChange = output<WorkflowSpec>();
  readonly selectRun = output<string>();

  // ── State ───────────────────────────────────────────────────────────────
  readonly selectedNodeId = signal<string | null>(null);
  readonly hoveredNodeId = signal<string | null>(null);
  readonly executionStatus = signal<'idle' | 'running' | 'completed' | 'error'>('idle');
  readonly totalDurationMs = signal(0);
  readonly inspectorTab = signal<'config' | 'output'>('config');

  /** Run history for the current workflow */
  readonly runHistory = signal<WorkflowRunSummary[]>([]);
  readonly selectedRunId = signal<string | null>(null);
  readonly showRunHistory = signal(false);

  /** Node outputs from a historical run (keyed by node_id) */
  readonly historicalNodeOutputs = signal<Record<string, WorkflowRunNodeDetail>>({});

  /** Per-node execution state, keyed by node ID. */
  readonly nodeExecState = signal<Record<string, { status: NodeExecutionStatus; durationMs?: number }>>({});

  private zoom = signal(1);
  private panX = signal(0);
  private panY = signal(0);
  private isPanning = false;
  private lastPanX = 0;
  private lastPanY = 0;

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
    this.specChange.emit(updated);
  }

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

  // ── Layout computation ──────────────────────────────────────────────────

  readonly layoutNodes = computed<LayoutNode[]>(() => {
    const s = this.spec();
    if (!s?.agents?.length) return [];
    return this.computeLayout(s);
  });

  readonly layoutEdges = computed<LayoutEdge[]>(() => {
    const s = this.spec();
    const nodes = this.layoutNodes();
    if (!s || !nodes.length) return [];
    return this.computeEdges(s, nodes);
  });

  readonly hasEndNode = computed(() => {
    const s = this.spec();
    if (!s) return false;
    return s.edges?.some(e => e.to === 'END');
  });

  readonly endNodePos = computed(() => {
    const nodes = this.layoutNodes();
    if (!nodes.length) return { x: 400, y: 200 };
    const maxX = Math.max(...nodes.map(n => n.x + n.width));
    const avgY = nodes.reduce((sum, n) => sum + n.y, 0) / nodes.length;
    return { x: maxX + 100, y: avgY + NODE_HEIGHT / 2 - 24 };
  });

  readonly selectedNode = computed<LayoutNode | null>(() => {
    const id = this.selectedNodeId();
    if (!id) return null;
    return this.layoutNodes().find(n => n.id === id) || null;
  });

  readonly viewBox = computed(() => {
    const nodes = this.layoutNodes();
    const end = this.endNodePos();
    if (!nodes.length) return '0 0 800 400';

    const z = this.zoom();
    const px = this.panX();
    const py = this.panY();

    const maxX = Math.max(end.x + 100, ...nodes.map(n => n.x + n.width)) + PADDING;
    const maxY = Math.max(end.y + 80, ...nodes.map(n => n.y + n.height)) + PADDING;

    const w = maxX / z;
    const h = maxY / z;

    return `${-px / z} ${-py / z} ${w} ${h}`;
  });

  // ── Interactions ────────────────────────────────────────────────────────

  selectNode(id: string): void {
    this.selectedNodeId.set(this.selectedNodeId() === id ? null : id);
  }

  zoomIn(): void {
    this.zoom.update(z => Math.min(z * 1.2, 3));
  }

  zoomOut(): void {
    this.zoom.update(z => Math.max(z / 1.2, 0.3));
  }

  resetView(): void {
    this.zoom.set(1);
    this.panX.set(0);
    this.panY.set(0);
  }

  onWheel(event: WheelEvent): void {
    event.preventDefault();
    if (event.deltaY < 0) {
      this.zoomIn();
    } else {
      this.zoomOut();
    }
  }

  onPanStart(event: MouseEvent): void {
    if (event.button !== 0) return;
    this.isPanning = true;
    this.lastPanX = event.clientX;
    this.lastPanY = event.clientY;
  }

  onPanMove(event: MouseEvent): void {
    if (!this.isPanning) return;
    const dx = event.clientX - this.lastPanX;
    const dy = event.clientY - this.lastPanY;
    this.panX.update(x => x + dx);
    this.panY.update(y => y + dy);
    this.lastPanX = event.clientX;
    this.lastPanY = event.clientY;
  }

  onPanEnd(): void {
    this.isPanning = false;
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

    while (queue.length > 0) {
      const node = queue.shift()!;
      const currentLayer = layers.get(node) ?? 0;
      for (const next of adj.get(node) ?? []) {
        const nextLayer = Math.max(layers.get(next) ?? 0, currentLayer + 1);
        layers.set(next, nextLayer);
        if (!queue.includes(next)) {
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
          width: NODE_WIDTH,
          height: NODE_HEIGHT,
          layer,
          color: NODE_COLORS[type] || NODE_COLORS['default'],
          icon: NODE_ICONS[type] || NODE_ICONS['default'],
          executionStatus: execState?.status ?? 'idle',
          durationMs: execState?.durationMs,
        });
      });
    }

    return layoutNodes;
  }

  private computeEdges(spec: WorkflowSpec, nodes: LayoutNode[]): LayoutEdge[] {
    const nodeMap = new Map(nodes.map(n => [n.id, n]));
    const endPos = this.endNodePos();
    const edges: LayoutEdge[] = [];

    // Regular edges
    for (const e of spec.edges ?? []) {
      const source = nodeMap.get(e.from);
      if (!source) continue;

      if (e.to === 'END') {
        const sx = source.x + source.width;
        const sy = source.y + source.height / 2;
        const ex = endPos.x;
        const ey = endPos.y + 24;
        const midX = (sx + ex) / 2;
        edges.push({
          id: `${e.from}-END`,
          source: e.from,
          target: 'END',
          label: e.label || '',
          condition: e.condition || 'always',
          isConditional: false,
          path: `M ${sx} ${sy} C ${midX} ${sy} ${midX} ${ey} ${ex} ${ey}`,
          labelX: midX,
          labelY: (sy + ey) / 2,
        });
      } else {
        const target = nodeMap.get(e.to);
        if (!target) continue;
        const sx = source.x + source.width;
        const sy = source.y + source.height / 2;
        const ex = target.x;
        const ey = target.y + target.height / 2;
        const midX = (sx + ex) / 2;
        edges.push({
          id: `${e.from}-${e.to}`,
          source: e.from,
          target: e.to,
          label: e.label || '',
          condition: e.condition || 'always',
          isConditional: false,
          path: `M ${sx} ${sy} C ${midX} ${sy} ${midX} ${ey} ${ex} ${ey}`,
          labelX: midX,
          labelY: (sy + ey) / 2,
        });
      }
    }

    // Conditional edges
    for (const ce of spec.conditional_edges ?? []) {
      const source = nodeMap.get(ce.from);
      if (!source) continue;

      for (const [condition, targetId] of Object.entries(ce.condition_mapping)) {
        if (targetId === 'END') continue;
        const target = nodeMap.get(targetId);
        if (!target) continue;

        const sx = source.x + source.width;
        const sy = source.y + source.height / 2;
        const ex = target.x;
        const ey = target.y + target.height / 2;
        const midX = (sx + ex) / 2;
        edges.push({
          id: `${ce.from}-${targetId}-${condition}`,
          source: ce.from,
          target: targetId,
          label: condition,
          condition,
          isConditional: true,
          path: `M ${sx} ${sy} C ${midX} ${sy} ${midX} ${ey} ${ex} ${ey}`,
          labelX: midX,
          labelY: (sy + ey) / 2,
        });
      }
    }

    return edges;
  }
}
