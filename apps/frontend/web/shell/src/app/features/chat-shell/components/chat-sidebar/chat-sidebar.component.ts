import { Component, input, output, model } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { SessionListItem, type WorkflowSpec } from '@synaptiq/chat';
import { type ViewSpec } from '@synaptiq/constants';

@Component({
  selector: 'sq-chat-sidebar',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule],
  templateUrl: './chat-sidebar.component.html',
  styleUrl: './chat-sidebar.component.scss',
})
export class ChatSidebarComponent {
  // ── Inputs ───────────────────────────────────────────────────────────
  readonly sidebarTab = model<'recent' | 'pinned' | 'workflows'>('recent');
  readonly sessionHistory = input<SessionListItem[]>([]);
  readonly activeSessionId = input('');
  readonly pinnedViews = input<ViewSpec[]>([]);
  readonly activePinnedView = input('');
  readonly savedWorkflows = input<WorkflowSpec[]>([]);
  readonly communityTemplates = input<WorkflowSpec[]>([]);
  readonly currentWorkflowId = input<string | null>(null);
  readonly isAuthenticated = input(false);
  readonly userEmail = input<string | null>(null);

  // ── Outputs ──────────────────────────────────────────────────────────
  readonly toggleSidebar = output<void>();
  readonly newConversation = output<void>();
  readonly loadSession = output<SessionListItem>();
  readonly deleteSession = output<{ session: SessionListItem; event: Event }>();
  readonly selectPinnedView = output<string>();
  readonly unpinView = output<{ viewId: string; event: Event }>();
  readonly loadSavedWorkflow = output<string>();
  readonly loadWorkflowTemplate = output<WorkflowSpec>();
  readonly openAuth = output<'signin' | 'signup'>();
  readonly signOut = output<void>();
}
