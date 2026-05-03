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
  signal,
  computed,
  effect,
  ChangeDetectionStrategy,
  ElementRef,
  viewChild,
  HostListener,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import type { WorkflowSpec, AgentNodeSpec, EdgeSpec, ConditionalEdgeSpec } from '../workflow.service';

// ---------------------------------------------------------------------------
// Layout Types
// ---------------------------------------------------------------------------

interface LayoutNode {
  id: string;
  label: string;
  type: string;
  description: string;
  tools: string[];
  x: number;
  y: number;
  width: number;
  height: number;
  layer: number;
  color: string;
  icon: string;
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
  imports: [CommonModule, MatIconModule, MatTooltipModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="workflow-canvas-wrapper" #canvasWrapper>
      <!-- Toolbar -->
      <div class="canvas-toolbar">
        <div class="toolbar-left">
          <span class="workflow-name">{{ spec()?.name || 'Workflow' }}</span>
          <span class="workflow-badge" [attr.data-type]="spec()?.flow_type">{{ spec()?.flow_type || 'static' }}</span>
          <span class="agent-count">{{ layoutNodes().length }} agents</span>
        </div>
        <div class="toolbar-right">
          <button class="toolbar-btn" (click)="zoomIn()" matTooltip="Zoom in">
            <mat-icon>zoom_in</mat-icon>
          </button>
          <button class="toolbar-btn" (click)="zoomOut()" matTooltip="Zoom out">
            <mat-icon>zoom_out</mat-icon>
          </button>
          <button class="toolbar-btn" (click)="resetView()" matTooltip="Fit to screen">
            <mat-icon>fit_screen</mat-icon>
          </button>
        </div>
      </div>

      <!-- SVG Canvas -->
      <svg
        class="workflow-svg"
        [attr.viewBox]="viewBox()"
        (mousedown)="onPanStart($event)"
        (mousemove)="onPanMove($event)"
        (mouseup)="onPanEnd()"
        (mouseleave)="onPanEnd()"
        (wheel)="onWheel($event)"
      >
        <defs>
          <!-- Arrow marker -->
          <marker id="arrow" viewBox="0 0 10 6" refX="10" refY="3"
                  markerWidth="10" markerHeight="6" orient="auto-start-reverse">
            <path d="M 0 0 L 10 3 L 0 6 Z" fill="var(--sq-text-secondary, #94a3b8)" />
          </marker>
          <marker id="arrow-active" viewBox="0 0 10 6" refX="10" refY="3"
                  markerWidth="10" markerHeight="6" orient="auto-start-reverse">
            <path d="M 0 0 L 10 3 L 0 6 Z" fill="#6366f1" />
          </marker>

          <!-- Glow filter -->
          <filter id="node-glow" x="-20%" y="-20%" width="140%" height="140%">
            <feGaussianBlur stdDeviation="6" result="blur" />
            <feComposite in="SourceGraphic" in2="blur" operator="over" />
          </filter>

          <!-- Background grid pattern -->
          <pattern id="grid" width="24" height="24" patternUnits="userSpaceOnUse">
            <path d="M 24 0 L 0 0 0 24" fill="none" stroke="var(--sq-border, rgba(148,163,184,0.1))" stroke-width="0.5"/>
          </pattern>
        </defs>

        <!-- Grid background -->
        <rect width="100%" height="100%" fill="url(#grid)" />

        <!-- Edges -->
        @for (edge of layoutEdges(); track edge.id) {
          <g class="edge-group"
             [class.edge-conditional]="edge.isConditional"
             [class.edge-highlighted]="selectedNodeId() === edge.source || selectedNodeId() === edge.target">
            <path
              class="edge-path"
              [attr.d]="edge.path"
              fill="none"
              stroke="var(--sq-text-secondary, #94a3b8)"
              stroke-width="2"
              [attr.marker-end]="(selectedNodeId() === edge.source || selectedNodeId() === edge.target) ? 'url(#arrow-active)' : 'url(#arrow)'"
              [attr.stroke-dasharray]="edge.isConditional ? '6,4' : 'none'"
            />
            @if (edge.label) {
              <rect
                [attr.x]="edge.labelX - 40"
                [attr.y]="edge.labelY - 10"
                width="80"
                height="20"
                rx="4"
                fill="var(--sq-surface, #1e293b)"
                stroke="var(--sq-border, rgba(148,163,184,0.15))"
                stroke-width="1"
              />
              <text
                class="edge-label"
                [attr.x]="edge.labelX"
                [attr.y]="edge.labelY + 4"
                text-anchor="middle"
              >{{ edge.label | slice:0:12 }}</text>
            }
          </g>
        }

        <!-- Nodes -->
        @for (node of layoutNodes(); track node.id) {
          <g class="node-group"
             [class.node-selected]="selectedNodeId() === node.id"
             [class.node-entry]="spec()?.entry_point === node.id"
             (click)="selectNode(node.id)"
             (mouseenter)="hoveredNodeId.set(node.id)"
             (mouseleave)="hoveredNodeId.set(null)"
             [attr.transform]="'translate(' + node.x + ',' + node.y + ')'">

            <!-- Node shadow -->
            <rect
              class="node-shadow"
              [attr.x]="2"
              [attr.y]="2"
              [attr.width]="node.width"
              [attr.height]="node.height"
              rx="12"
            />

            <!-- Node background -->
            <rect
              class="node-bg"
              x="0"
              y="0"
              [attr.width]="node.width"
              [attr.height]="node.height"
              rx="12"
            />

            <!-- Color accent bar -->
            <rect
              [attr.fill]="node.color"
              x="0" y="0"
              width="4"
              [attr.height]="node.height"
              rx="2"
            />

            <!-- Entry point badge -->
            @if (spec()?.entry_point === node.id) {
              <circle
                [attr.cx]="node.width - 12"
                cy="12"
                r="6"
                fill="#22c55e"
                stroke="var(--sq-surface, #1e293b)"
                stroke-width="2"
              />
            }

            <!-- Icon circle -->
            <circle
              cx="32"
              cy="40"
              r="16"
              [attr.fill]="node.color + '22'"
              [attr.stroke]="node.color"
              stroke-width="1.5"
            />

            <!-- Label -->
            <text class="node-label" x="56" y="34">{{ node.label }}</text>

            <!-- Type badge -->
            <text class="node-type" x="56" y="52" [attr.fill]="node.color">{{ node.type }}</text>

            <!-- Tools count -->
            @if (node.tools.length) {
              <g transform="translate(0, 0)">
                <rect
                  [attr.x]="node.width - 36"
                  [attr.y]="node.height - 24"
                  width="28"
                  height="16"
                  rx="8"
                  [attr.fill]="node.color + '22'"
                />
                <text
                  class="node-tools-count"
                  [attr.x]="node.width - 22"
                  [attr.y]="node.height - 13"
                  text-anchor="middle"
                  [attr.fill]="node.color"
                >🔧{{ node.tools.length }}</text>
              </g>
            }
          </g>
        }

        <!-- END node -->
        @if (hasEndNode()) {
          <g class="node-group node-end"
             [attr.transform]="'translate(' + endNodePos().x + ',' + endNodePos().y + ')'">
            <circle cx="24" cy="24" r="24"
                    fill="var(--sq-surface, #1e293b)"
                    stroke="#ef4444"
                    stroke-width="2" />
            <circle cx="24" cy="24" r="8" fill="#ef4444" />
            <text class="end-label" x="24" y="64" text-anchor="middle">END</text>
          </g>
        }
      </svg>

      <!-- Detail Panel (slides in from right) -->
      @if (selectedNode()) {
        <aside class="detail-panel">
          <div class="detail-header">
            <div class="detail-icon" [style.background]="selectedNode()!.color + '22'" [style.color]="selectedNode()!.color">
              <mat-icon>{{ selectedNode()!.icon }}</mat-icon>
            </div>
            <div class="detail-title-group">
              <h3 class="detail-title">{{ selectedNode()!.label }}</h3>
              <span class="detail-type" [style.color]="selectedNode()!.color">{{ selectedNode()!.type }}</span>
            </div>
            <button class="detail-close" (click)="selectedNodeId.set(null)">
              <mat-icon>close</mat-icon>
            </button>
          </div>

          @if (selectedNode()!.description) {
            <div class="detail-section">
              <span class="detail-label">Description</span>
              <p class="detail-value">{{ selectedNode()!.description }}</p>
            </div>
          }

          @if (selectedNode()!.tools.length) {
            <div class="detail-section">
              <span class="detail-label">Tools</span>
              <div class="detail-tools">
                @for (tool of selectedNode()!.tools; track tool) {
                  <span class="detail-tool-chip">🔧 {{ tool }}</span>
                }
              </div>
            </div>
          }

          <div class="detail-section">
            <span class="detail-label">Node ID</span>
            <code class="detail-code">{{ selectedNode()!.id }}</code>
          </div>
        </aside>
      }
    </div>
  `,
  styleUrl: './workflow-canvas.component.scss',
})
export class WorkflowCanvasComponent {
  // ── Inputs ──────────────────────────────────────────────────────────────
  readonly spec = input<WorkflowSpec | null>(null);

  // ── State ───────────────────────────────────────────────────────────────
  readonly selectedNodeId = signal<string | null>(null);
  readonly hoveredNodeId = signal<string | null>(null);

  private zoom = signal(1);
  private panX = signal(0);
  private panY = signal(0);
  private isPanning = false;
  private lastPanX = 0;
  private lastPanY = 0;

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
        layoutNodes.push({
          id: agent.id,
          label: agent.label || agent.id,
          type,
          description: agent.description || '',
          tools: agent.tools || [],
          x: PADDING + layer * LAYER_GAP_X,
          y: startY + idx * (NODE_HEIGHT + NODE_GAP_Y),
          width: NODE_WIDTH,
          height: NODE_HEIGHT,
          layer,
          color: NODE_COLORS[type] || NODE_COLORS['default'],
          icon: NODE_ICONS[type] || NODE_ICONS['default'],
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
