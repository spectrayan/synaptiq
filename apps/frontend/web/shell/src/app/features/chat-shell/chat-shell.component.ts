import { Component, signal, inject, effect, computed, viewChild, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { map } from 'rxjs';
import { type ComponentSpec, type ViewSpec } from '@synaptiq/constants';
import { FormSubmitEvent } from '@synaptiq/dsl-renderer';
import {
  ActionsService,
  ChatService,
  ConfigService,
  SessionService,
  SessionListItem,
  WorkflowService,
  WorkflowCanvasComponent,
  type WorkflowSpec,
  type WorkflowRunSummary,
  type ToolDefinition,
  type BrandingConfig,
} from '@synaptiq/chat';
import { AuthService } from '@synaptiq/auth';
import { ENVIRONMENT } from '@synaptiq/utils';
import { ThemeService } from '../../core/theme.service';
import { ChatMessage } from './chat-message.model';
import { ChatSidebarComponent } from './components/chat-sidebar/chat-sidebar.component';
import { ChatTopbarComponent } from './components/chat-topbar/chat-topbar.component';
import { ChatMessageListComponent } from './components/chat-message-list/chat-message-list.component';
import { ChatInputBarComponent } from './components/chat-input-bar/chat-input-bar.component';
import { ChatSettingsDrawerComponent } from './components/chat-settings-drawer/chat-settings-drawer.component';
import { MatDialog } from '@angular/material/dialog';
import { WorkflowRunDialogComponent } from './components/workflow-run-dialog/workflow-run-dialog.component';
import { ConfigFieldSaveEvent } from './components/admin-config-panel/admin-config-panel.component';
import {
  CMD_SIGN_IN,
  CMD_SIGN_UP,
  CMD_ANALYTICS_DASHBOARD,
  CONFIG_CMD_MAP,
  type ConfigPanelType,
  PINNED_VIEWS_KEY,
  DEFAULT_PERSONA_NAME,
  MSG_SESSION_LOADED,
  MSG_SESSION_LOAD_ERROR,
  MSG_AUTH_SIGNUP,
  MSG_AUTH_SIGNIN,
  isWorkflowIntent,
  resolveCommandToDisplayLabel,
} from './chat-shell.constants';
import { buildWelcomeMessages, type WelcomeMessageContext } from './welcome-message.builder';
import { ChatStreamOrchestrator, type StreamCallbacks } from './services/chat-stream.orchestrator';
import { ConfigPanelOrchestrator } from './services/config-panel.orchestrator';
import { ChatAnalyticsOrchestrator } from './services/chat-analytics.orchestrator';

// Re-export ChatMessage from model file for external consumers
export type { ChatMessage } from './chat-message.model';

// ---------------------------------------------------------------------------
// Component
// ---------------------------------------------------------------------------

@Component({
  selector: 'sq-chat-shell',
  standalone: true,
  imports: [
    CommonModule, MatIconModule, MatButtonModule, WorkflowCanvasComponent,
    ChatSidebarComponent, ChatTopbarComponent, ChatMessageListComponent,
    ChatInputBarComponent, ChatSettingsDrawerComponent,
  ],
  templateUrl: './chat-shell.component.html',
  styleUrl: './chat-shell.component.scss',
})
export class ChatShellComponent implements OnDestroy {
  private readonly breakpoints = inject(BreakpointObserver);
  private readonly actionsService = inject(ActionsService);
  private readonly chatService = inject(ChatService);
  private readonly configService = inject(ConfigService);
  private readonly sessionService = inject(SessionService);
  private readonly workflowService = inject(WorkflowService);
  readonly auth = inject(AuthService);
  private readonly env = inject(ENVIRONMENT);
  readonly themeService = inject(ThemeService);
  private readonly streamOrchestrator = inject(ChatStreamOrchestrator);
  private readonly configPanelOrchestrator = inject(ConfigPanelOrchestrator);
  private readonly analyticsOrchestrator = inject(ChatAnalyticsOrchestrator);
  private readonly route = inject(ActivatedRoute);
  private readonly dialog = inject(MatDialog);

  /** Exposed for settings panel template */
  readonly apiBaseUrl = this.env.apiBaseUrl;

  /** Active session ID for the current conversation. */
  private sessionId: string = crypto.randomUUID();
  /** Whether the current session has been persisted to the backend. */
  private sessionPersisted = false;

  isMobile = toSignal(
    this.breakpoints.observe([Breakpoints.XSmall, Breakpoints.Small]).pipe(map((r) => r.matches)),
    { initialValue: false },
  );

  sidebarOpen = signal(true);
  isLoading = signal(false);
  /** Whether the backend is reachable — enables SSE mode vs demo fallback.
   *  Defaults to `true`; auto-switches to demo on network failure (see onError). */
  useBackend = signal(true);

  /** Auth UI state — rendered as inline chat message */
  authFormMessageId = signal<string | null>(null);
  authMode = signal<'signin' | 'signup'>('signin');
  authEmail = signal('');
  authPassword = signal('');
  authLoading = signal(false);

  /** Settings panel */
  showSettings = signal(false);

  /** Session history for the sidebar */
  sessionHistory = signal<SessionListItem[]>([]);
  activeSessionId = signal<string>('');
  /** Sidebar tab: 'recent' (session list) or 'pinned' (pinned views) or 'workflows' */
  readonly sidebarTab = signal<'recent' | 'pinned' | 'workflows'>('recent');

  /** Main content tab: chat or workflow canvas */
  readonly mainTab = signal<'chat' | 'workflow'>('chat');
  /** Current workflow spec being visualized */
  readonly currentWorkflow = signal<WorkflowSpec | null>(null);
  /** Status message during workflow generation */
  readonly workflowStatus = signal<string>('');
  /** Whether workflow generation is in progress */
  readonly workflowGenerating = signal(false);
  /** Past execution runs for the current workflow */
  readonly workflowRunHistory = signal<WorkflowRunSummary[]>([]);
  /** Saved workflows list for the sidebar */
  readonly savedWorkflows = signal<WorkflowSpec[]>([]);
  /** Community templates list for the sidebar */
  readonly communityTemplates = signal<WorkflowSpec[]>([]);

  messages = signal<ChatMessage[]>([]);

  /** Pinned views — persistent surfaces above the chat stream (P0-B) */
  readonly pinnedViews = signal<ViewSpec[]>(this._loadPinnedViews());
  readonly activePinnedView = signal<string>('');
  readonly pinnedExpanded = signal(true);
  readonly activePinnedViewSpec = computed(() => {
    const views = this.pinnedViews();
    const activeId = this.activePinnedView();
    return views.find(v => v.view_id === activeId) || null;
  });

  /** P0-B: Auto-persist pinned views to localStorage + debounced backend sync. */
  private _pinnedSyncTimer: ReturnType<typeof setTimeout> | null = null;
  private readonly _pinnedViewsPersist = effect(() => {
    const views = this.pinnedViews();
    // Immediate localStorage save
    try {
      if (views.length > 0) {
        localStorage.setItem(PINNED_VIEWS_KEY, JSON.stringify(views));
      } else {
        localStorage.removeItem(PINNED_VIEWS_KEY);
      }
    } catch { /* storage full — ignore */ }

    // Debounced backend sync (500ms)
    if (this._pinnedSyncTimer) clearTimeout(this._pinnedSyncTimer);
    if (this.sessionPersisted) {
      this._pinnedSyncTimer = setTimeout(() => {
        this.sessionService.savePinnedViews(this.sessionId, views).catch(() => {});
      }, 500);
    }
  });

  /** Whether the authenticated user has admin/owner privileges. */
  readonly isAdminMode = this.auth.isAdmin;

  /** Loaded AI persona config from tenant — drives welcome message & starter prompts */
  private personaName = DEFAULT_PERSONA_NAME;
  private personaWelcome = '';
  private personaStarters: string[] = [];

  /** Build the welcome message based on user role and loaded persona config. */
  private _buildInitialMessages(): ChatMessage[] {
    const user = this.auth.currentUser();
    const ctx: WelcomeMessageContext = {
      isLoggedIn: this.auth.isLoggedIn(),
      isAdmin: this.auth.isAdmin(),
      isGuest: this.auth.isGuest(),
      displayName: user?.displayName,
      email: user?.email,
      personaName: this.personaName,
      personaWelcome: this.personaWelcome,
      personaStarters: this.personaStarters,
    };
    return buildWelcomeMessages(ctx);
  }

  constructor() {

    this.route.paramMap.subscribe(params => {
      const token = params.get('token');
      if (token) {
        this.loadSharedWorkflow(token);
      }
    });

    // Fetch public tenant persona config (welcome message + starter prompts)
    const personaReady = this._loadPersona();

    // Load workflow run history when the workflow tab opens or workflow changes
    effect(() => {
      const tab = this.mainTab();
      const wf = this.currentWorkflow();
      if (tab === 'workflow' && wf?.id) {
        // Use untracked to prevent the async work from re-triggering this effect
        const workflowId = wf.id;
        queueMicrotask(() => this._loadRunHistoryById(workflowId));
      }
    });

    // Watch for auth readiness — auto-sign-in as guest and show welcome
    effect(() => {
      const loading = this.auth.isLoading();
      if (!loading && this.messages().length === 0) {
        // Auth finished loading — wait for persona config before building messages
        if (!this.auth.isLoggedIn()) {
          // No user at all — sign in anonymously for immediate access
          this.auth.signInAsGuest()
            .then(() => personaReady)
            .then(() => {
              this.messages.set(this._buildInitialMessages());
              this.loadCommunityTemplates();
            })
            .catch(() => {
              // Emulator might not be running — show welcome anyway
              this.messages.set(this._buildInitialMessages());
            });
        } else {
          // User already authenticated (page refresh, etc.)
          personaReady.then(() => {
            this.messages.set(this._buildInitialMessages());
            this.loadSessionHistory();
            this.loadSavedWorkflows();
            this.loadCommunityTemplates();
          });
        }
      }
    });
  }

  /** Load public AI persona config from the backend (no auth required). */
  private async _loadPersona(): Promise<void> {
    try {
      const config = await this.configService.getPublicBranding();
      const persona = config.ai_persona;
      if (persona) {
        this.personaName = persona.display_name || DEFAULT_PERSONA_NAME;
        this.personaWelcome = persona.welcome_message || '';
        this.personaStarters = persona.starter_prompts || [];
      }
    } catch {
      // Backend unreachable — keep defaults
    }
  }

  // ── Session management ───────────────────────────────────────────────

  async loadSessionHistory(): Promise<void> {
    try {
      const response = await this.sessionService.listSessions();
      this.sessionHistory.set(response.sessions ?? []);
    } catch {
      // Silently fail — sidebar shows empty list
    }
  }

  async loadSession(session: SessionListItem): Promise<void> {
    this.chatService.abort();
    this.isLoading.set(true);
    this.sessionId = session.sessionId ?? '';
    this.sessionPersisted = true;
    this.activeSessionId.set(session.sessionId ?? '');

    // Close sidebar on mobile
    if (this.isMobile()) {
      this.sidebarOpen.set(false);
    }

    try {
      const history = await this.sessionService.getHistory(session.sessionId ?? '');
      const restoredMessages: ChatMessage[] = (history.turns ?? []).map((turn, i) => ({
        id: `restored-${i}`,
        role: (turn.role as 'user' | 'assistant') ?? 'assistant',
        content: turn.content ?? '',
        timestamp: turn.timestamp ? new Date(turn.timestamp) : new Date(),
        uiComponents: [] as ComponentSpec[],
      }));

      this.messages.set(
        restoredMessages.length > 0
          ? restoredMessages
          : [{
              id: crypto.randomUUID(),
              role: 'assistant',
              content: MSG_SESSION_LOADED,
              timestamp: new Date(),
            }],
      );
    } catch {
      this.messages.set([{
        id: crypto.randomUUID(),
        role: 'assistant',
        content: MSG_SESSION_LOAD_ERROR,
        timestamp: new Date(),
      }]);
    } finally {
      this.isLoading.set(false);
    }
  }

  async deleteSession(session: SessionListItem, event: Event): Promise<void> {
    event.stopPropagation();
    try {
      await this.sessionService.deleteSession(session.sessionId ?? '');
      this.sessionHistory.update((s) => s.filter((h) => h.sessionId !== session.sessionId));
      if (this.sessionId === session.sessionId) {
        this.newConversation();
      }
    } catch {
      // Silently fail
    }
  }

  // ── Sidebar ──────────────────────────────────────────────────────────

  toggleSidebar(): void {
    this.sidebarOpen.update((v) => !v);
  }

  newConversation(): void {
    this.chatService.abort();
    this.sessionId = crypto.randomUUID();
    this.sessionPersisted = false;
    this.activeSessionId.set('');
    this.messages.set(this._buildInitialMessages());
    this.isLoading.set(false);
    this.authFormMessageId.set(null);
    // P0-B: Clear pinned views for the new conversation
    this.pinnedViews.set([]);
    this.activePinnedView.set('');
  }

  // ── Conversational Auth (inline in chat stream) ─────────────────────

  /** Inject an auth form as an inline chat message */
  openAuthForm(mode: 'signin' | 'signup' = 'signin'): void {
    this.authMode.set(mode);
    this.authEmail.set('');
    this.authPassword.set('');
    this.auth.clearError();

    const msgId = crypto.randomUUID();
    this.authFormMessageId.set(msgId);

    this.messages.update((msgs) => [
      ...msgs,
      {
        id: msgId,
        role: 'assistant',
        content: mode === 'signup'
          ? MSG_AUTH_SIGNUP
          : MSG_AUTH_SIGNIN,
        timestamp: new Date(),
        authForm: mode,
      },
    ]);
  }

  /** Toggle between signin/signup within the inline form */
  toggleAuthMode(): void {
    const newMode = this.authMode() === 'signin' ? 'signup' : 'signin';
    this.authMode.set(newMode);
    this.auth.clearError();
    // Update the message's authForm type
    const msgId = this.authFormMessageId();
    if (msgId) {
      this.messages.update((msgs) =>
        msgs.map((m) => m.id === msgId
          ? { ...m, authForm: newMode, content: newMode === 'signup'
              ? MSG_AUTH_SIGNUP
              : MSG_AUTH_SIGNIN }
          : m),
      );
    }
  }

  /** Submit the inline auth form */
  async submitAuth(): Promise<void> {
    const email = this.authEmail().trim();
    const password = this.authPassword();
    if (!email || !password) return;

    this.authLoading.set(true);
    try {
      if (this.authMode() === 'signup') {
        await this.auth.signUp(email, password);
      } else {
        await this.auth.signIn(email, password);
      }
      this._onAuthSuccess();
    } catch {
      // Error is shown via auth.error() signal in template
    } finally {
      this.authLoading.set(false);
    }
  }

  /** Sign in with Google — inline */
  async signInWithGoogle(): Promise<void> {
    this.authLoading.set(true);
    try {
      await this.auth.signInWithGoogle();
      this._onAuthSuccess();
    } catch {
      // Error is shown via auth.error() signal
    } finally {
      this.authLoading.set(false);
    }
  }

  /** Common post-auth handler — mark form as done, add welcome message */
  private _onAuthSuccess(): void {
    // Mark auth form message as submitted
    const msgId = this.authFormMessageId();
    if (msgId) {
      this.messages.update((msgs) =>
        msgs.map((m) => m.id === msgId ? { ...m, formSubmitted: true, authForm: undefined } : m),
      );
    }
    this.authFormMessageId.set(null);

    const user = this.auth.currentUser();
    this.messages.update((msgs) => [
      ...msgs,
      {
        id: crypto.randomUUID(),
        role: 'assistant',
        content: `✅ Welcome${user?.displayName ? ', ' + user.displayName : ''}! You're now signed in.`,
        timestamp: new Date(),
        uiComponents: [
          {
            type: 'info_banner',
            title: 'Signed in',
            body: `Logged in as ${user?.email ?? 'user'}`,
            style: 'success',
            suggestions: [
              { label: '🔍 Search', prompt: 'Search electronics' },
              { label: '➕ Add item', prompt: 'Add a new item' },
            ],
          },
        ],
      },
    ]);
    this.loadSessionHistory();
  }

  /** Sign out from within the chat */
  async signOutFromChat(): Promise<void> {
    await this.auth.signOutUser();
    // Re-enter as guest
    await this.auth.signInAsGuest().catch(() => {});
    this.newConversation();
  }

  /** Clear all conversations and reset to fresh state */
  clearConversations(): void {
    this.sessionHistory.set([]);
    this.showSettings.set(false);
    this.newConversation();
  }

  // ── Workflow ─────────────────────────────────────────────────────────

  /** Generate a workflow from a natural language prompt. */
  async generateWorkflow(prompt: string): Promise<void> {
    this.workflowGenerating.set(true);
    this.workflowStatus.set('Starting workflow generation...');
    this.currentWorkflow.set(null);

    const authToken = (await this.auth.getIdToken().catch(() => undefined)) ?? undefined;

    await this.workflowService.streamGenerate(
      prompt,
      {
        onStatus: (message) => {
          this.workflowStatus.set(message);
        },
        onComponent: (spec) => {
          this.currentWorkflow.set(spec as unknown as WorkflowSpec);
          this.mainTab.set('workflow');
          this.workflowStatus.set('');
        },
        onText: (text) => {
          // Add the summary as a chat message
          this.messages.update((msgs) => [
            ...msgs,
            {
              id: crypto.randomUUID(),
              role: 'assistant',
              content: text,
              timestamp: new Date(),
            },
          ]);
        },
        onDone: async () => {
          this.workflowGenerating.set(false);
          this.workflowStatus.set('');

          // Persist the generated workflow to the database.
          // The Gemini generation service returns a spec without an id,
          // so we must explicitly save it to get a persisted ID back.
          const wf = this.currentWorkflow();
          if (wf && !wf.id) {
            try {
              const authToken = (await this.auth.getIdToken().catch(() => undefined)) ?? undefined;
              const saved = await this.workflowService.saveWorkflow(wf, authToken);
              console.log(`[ChatShell] persisted generated workflow, id=${saved.id}`);
              // Update local state with the persisted spec (includes id)
              this.currentWorkflow.set(saved);
            } catch (e) {
              console.error('[ChatShell] failed to persist generated workflow:', e);
            }
          }

          // Refresh the sidebar workflow list
          this.loadSavedWorkflows();
        },
        onError: (message) => {
          this.workflowGenerating.set(false);
          this.workflowStatus.set('');
          this.messages.update((msgs) => [
            ...msgs,
            {
              id: crypto.randomUUID(),
              role: 'assistant',
              content: `❌ Workflow generation failed: ${message}`,
              timestamp: new Date(),
            },
          ]);
        },
      },
    );
  }

  /** Switch to the workflow tab to view the current workflow. */
  viewWorkflow(): void {
    if (this.currentWorkflow()) {
      this.mainTab.set('workflow');
    }
  }

  /** Load a template as the current workflow. */
  loadWorkflowTemplate(template: WorkflowSpec): void {
    // Strip the ID so that saving it creates a new workflow for the user
    const { id, ...templateWithoutId } = template;
    this.currentWorkflow.set(templateWithoutId as WorkflowSpec);
    this.mainTab.set('workflow');
  }

  /** Fetch saved workflows from the backend for the sidebar list. */
  async loadSavedWorkflows(): Promise<void> {
    console.log('[ChatShell] loadSavedWorkflows: fetching list...');
    try {
      const authToken = (await this.auth.getIdToken().catch(() => undefined)) ?? undefined;
      const workflows = await this.workflowService.listWorkflows(authToken);
      console.log(`[ChatShell] loadSavedWorkflows: got ${workflows.length} workflows`);
      this.savedWorkflows.set(workflows);
    } catch (err) {
      console.warn('[ChatShell] loadSavedWorkflows failed:', err);
    }
  }

  /** Fetch public community templates from the backend for the sidebar list. */
  async loadCommunityTemplates(): Promise<void> {
    console.log('[ChatShell] loadCommunityTemplates: fetching list...');
    try {
      const templates = await this.workflowService.listPublicTemplates();
      console.log(`[ChatShell] loadCommunityTemplates: got ${templates.length} templates`);
      this.communityTemplates.set(templates);
    } catch (err) {
      console.warn('[ChatShell] loadCommunityTemplates failed:', err);
    }
  }

  /** Load a saved workflow by fetching its full spec and opening the canvas. */
  async loadSavedWorkflow(workflowId: string): Promise<void> {
    console.log(`[ChatShell] loadSavedWorkflow: loading ${workflowId}...`);
    try {
      const authToken = (await this.auth.getIdToken().catch(() => undefined)) ?? undefined;
      const spec = await this.workflowService.getWorkflow(workflowId, authToken);
      if (spec) {
        console.log(`[ChatShell] loadSavedWorkflow: loaded "${spec.name}" with ${spec.agents?.length ?? 0} agents`);
        this.currentWorkflow.set(spec);
        this.mainTab.set('workflow');
      } else {
        console.warn(`[ChatShell] loadSavedWorkflow: workflow ${workflowId} returned null`);
      }
    } catch (err) {
      console.error('[ChatShell] loadSavedWorkflow failed:', err);
    }
  }

  // ── Run History ──────────────────────────────────────────────────────

  /** Load execution history for a workflow and push it to the canvas. */
  async loadRunHistory(): Promise<void> {
    const wf = this.currentWorkflow();
    if (!wf?.id) return;
    return this._loadRunHistoryById(wf.id);
  }

  /** Internal: load run history by explicit ID (avoids re-reading signals inside effects). */
  private async _loadRunHistoryById(workflowId: string): Promise<void> {
    console.log(`[ChatShell] _loadRunHistoryById: ${workflowId}`);
    try {
      const authToken = (await this.auth.getIdToken().catch(() => undefined)) ?? undefined;
      const runs = await this.workflowService.listRuns(workflowId, authToken);
      console.log(`[ChatShell] _loadRunHistoryById: got ${runs.length} runs`);
      this.workflowRunHistory.set(runs);
      this._workflowCanvas()?.setRunHistory(runs);
    } catch (err) {
      console.error('[ChatShell] Failed to load run history', err);
    }
  }

  /** Handle run selection from the canvas history dropdown. */
  async onSelectRun(runId: string): Promise<void> {
    const canvas = this._workflowCanvas();
    if (!canvas) return;
    try {
      const authToken = (await this.auth.getIdToken().catch(() => undefined)) ?? undefined;
      const detail = await this.workflowService.getRunDetails(runId, authToken);
      if (detail) {
        canvas.loadHistoricalRun(detail.runId, detail.nodes);
      }
    } catch (err) {
      console.error('Failed to load run detail', err);
    }
  }

  /** Detect if a message is a workflow intent. */
  private _isWorkflowIntent(msg: string): boolean {
    return isWorkflowIntent(msg);
  }

  // ── Messaging ────────────────────────────────────────────────────────

  async sendMessage(text: string): Promise<void> {
    const msg = text.trim();
    if (!msg || this.isLoading()) return;

    // Add user message with human-readable label if it's a hidden command token
    const displayContent = resolveCommandToDisplayLabel(msg);

    this.messages.update((msgs) => [
      ...msgs,
      { id: crypto.randomUUID(), role: 'user', content: displayContent, timestamp: new Date() },
    ]);

    // ── Workflow intent detection ──
    if (this._isWorkflowIntent(msg)) {
      this.isLoading.set(true);
      this.mainTab.set('chat'); // Stay on chat to show status
      await this.generateWorkflow(msg);
      this.isLoading.set(false);
      return;
    }

    this.isLoading.set(true);

    // Show typing indicator
    const typingId = '__typing__';
    this.messages.update((msgs) => [
      ...msgs,
      { id: typingId, role: 'assistant', content: '', timestamp: new Date(), isTyping: true },
    ]);

    if (this.useBackend()) {
      await this.sendViaBackend(msg, typingId);
    } else {
      await this.sendViaDemo(msg, typingId);
    }
  }

  /** Remove the typing indicator placeholder. */
  private _removeTyping(typingId: string): void {
    this.messages.update((msgs) => msgs.filter((m) => m.id !== typingId));
  }

  // ── Backend SSE streaming ───────────────────────────────────────────

  private async sendViaBackend(msg: string, typingId?: string): Promise<void> {
    const callbacks: StreamCallbacks = {
      updateMessages: (fn) => this.messages.update(fn),
      setLoading: (v) => this.isLoading.set(v),
      updatePinnedViews: (fn) => this.pinnedViews.update(fn),
      setActivePinnedView: (id) => this.activePinnedView.set(id),
      startAutoRefresh: (viewId, interval, query) =>
        this._startAutoRefresh(viewId, interval, query),
      onSessionCreated: () => {
        if (!this.sessionPersisted) {
          this.sessionPersisted = true;
          this.activeSessionId.set(this.sessionId);
          this.sessionService.createSession(this.sessionId).catch(() => {});
          this.loadSessionHistory();
        }
      },
    };

    await this.streamOrchestrator.sendViaBackend(
      msg,
      this.sessionId,
      typingId,
      callbacks,
    );
  }

  // ── Offline fallback ─────────────────────────────────────────────────

  private async sendViaDemo(msg: string, typingId?: string): Promise<void> {
    const callbacks: StreamCallbacks = {
      updateMessages: (fn: (msgs: ChatMessage[]) => ChatMessage[]) => this.messages.update(fn),
      setLoading: (v: boolean) => this.isLoading.set(v),
      updatePinnedViews: () => {},
      setActivePinnedView: () => {},
      startAutoRefresh: () => {},
      onSessionCreated: () => {},
    };
    await this.streamOrchestrator.sendViaDemo(msg, typingId, callbacks);
  }

  /** Remove a pinned view and select the next one if needed. */
  unpinView(viewId: string, event: Event): void {
    event.stopPropagation();
    this._stopAutoRefresh(viewId); // P1-B: stop any running timer
    this.pinnedViews.update(views => views.filter(v => v.view_id !== viewId));
    if (this.activePinnedView() === viewId) {
      const remaining = this.pinnedViews();
      this.activePinnedView.set(remaining.length > 0 ? remaining[0].view_id : '');
    }
    // Switch back to recent tab if no pinned views left
    if (this.pinnedViews().length === 0) {
      this.sidebarTab.set('recent');
    }
  }

  /** Navigate to a pinned view — scrolls to find it in the chat stream. */
  selectPinnedView(viewId: string): void {
    this.activePinnedView.set(viewId);
    // On mobile, close sidebar after selection
    if (this.isMobile()) {
      this.sidebarOpen.set(false);
    }
  }

  /** Bridge from AdminConfigPanelComponent save events to the existing saveConfigField method. */
  onConfigFieldSave(event: ConfigFieldSaveEvent): void {
    this.saveConfigField(event.panel, event.field, event.value, event.messageId);
  }

  // ── DSL component events ─────────────────────────────────────────────

  /**
   * When a user clicks an AI suggestion chip on a DSL component,
   * inject the prompt into the chat input and auto-send it.
   */
  onSuggestionClicked(prompt: string): void {
    if (prompt === CMD_SIGN_IN) {
      this.openAuthForm('signin');
      return;
    }
    if (prompt === CMD_SIGN_UP) {
      this.openAuthForm('signup');
      return;
    }
    if (prompt === CMD_ANALYTICS_DASHBOARD) {
      this.openAnalyticsDashboard();
      return;
    }
    // Admin config panel chips — lookup from constant map
    const panelType = CONFIG_CMD_MAP[prompt];
    if (panelType) {
      this.openConfigPanel(panelType as ConfigPanelType);
      return;
    }
    this.sendMessage(prompt);
  }

  /**
   * When a form within a message bubble is submitted,
   * add a confirmation reply and mark the original message's form as done.
   */
  async onFormSubmitted(event: FormSubmitEvent, messageId: string): Promise<void> {
    // Mark the form as submitted in the originating message
    this.messages.update((msgs) =>
      msgs.map((m) => (m.id === messageId ? { ...m, formSubmitted: true } : m)),
    );

    this.isLoading.set(true);

    try {
      const response = await this.actionsService.execute({
        action: event.action,
        values: event.values as Record<string, unknown>,
      });

      const suggestions = response.suggestions ?? [
        { label: 'Add another', prompt: 'Add a new item' },
        { label: 'View all', prompt: 'Show all items' },
      ];

      this.messages.update((msgs) => [
        ...msgs,
        {
          id: crypto.randomUUID(),
          role: 'assistant',
          content: '',
          timestamp: new Date(),
          uiComponents: [
            {
              type: 'action_confirm',
              action: event.action,
              message: response.message || `Successfully executed "${event.action}".`,
              confirm_label: 'Great!',
              suggestions,
            },
          ],
        },
      ]);
    } catch (err: unknown) {
      const errorMsg = err instanceof Error ? err.message : 'Something went wrong. Please try again.';
      this.messages.update((msgs) => [
        ...msgs,
        {
          id: crypto.randomUUID(),
          role: 'assistant',
          content: '',
          timestamp: new Date(),
          uiComponents: [
            {
              type: 'info_banner',
              title: 'Action Failed',
              body: errorMsg,
              style: 'error',
              suggestions: [
                { label: 'Try again', prompt: `Retry: ${event.action}` },
              ],
            },
          ],
        },
      ]);
    } finally {
      this.isLoading.set(false);
    }
  }

  /**
   * Generic action handler — save, share, view external, etc.
   * P0-A: Wired to ActionsService for real backend execution.
   */
  async onActionClicked(event: { action: string; item_id?: string }): Promise<void> {
    // Client-side-only actions — no backend call needed
    if (event.action === 'view_external') {
      return;
    }
    if (event.action === 'share_item') {
      if (event.item_id) {
        await navigator.clipboard?.writeText(`${window.location.origin}/item/${event.item_id}`);
        this.messages.update((msgs) => [
          ...msgs,
          {
            id: crypto.randomUUID(),
            role: 'assistant' as const,
            content: '',
            timestamp: new Date(),
            uiComponents: [
              {
                type: 'info_banner' as const,
                title: 'Link Copied',
                body: 'A shareable link has been copied to your clipboard.',
                style: 'success' as const,
              },
            ],
          },
        ]);
      }
      return;
    }

    this.isLoading.set(true);

    try {
      const response = await this.actionsService.execute({
        action: event.action,
        values: event.item_id ? { item_id: event.item_id } : {},
        metadata: { session_id: this.sessionId },
      });

      const suggestions = response.suggestions?.length
        ? response.suggestions
        : [
            { label: 'Continue browsing', prompt: 'Search products' },
          ];

      this.messages.update((msgs) => [
        ...msgs,
        {
          id: crypto.randomUUID(),
          role: 'assistant' as const,
          content: '',
          timestamp: new Date(),
          uiComponents: [
            {
              type: 'info_banner' as const,
              title: response.success ? 'Action Complete' : 'Action Failed',
              body: response.message ?? '',
              style: (response.success ? 'success' : 'error') as 'success' | 'error',
              suggestions,
            },
          ],
        },
      ]);
    } catch (err: unknown) {
      const errorMsg =
        err instanceof Error ? err.message : 'Something went wrong. Please try again.';
      this.messages.update((msgs) => [
        ...msgs,
        {
          id: crypto.randomUUID(),
          role: 'assistant' as const,
          content: '',
          timestamp: new Date(),
          uiComponents: [
            {
              type: 'info_banner' as const,
              title: 'Action Failed',
              body: errorMsg,
              style: 'error' as const,
              suggestions: [{ label: 'Try again', prompt: `Retry: ${event.action}` }],
            },
          ],
        },
      ]);
    } finally {
      this.isLoading.set(false);
    }
  }

  // ── Admin Config Panels (T9.6) ──────────────────────────────────────

  /** Open an admin config panel inline in the chat */
  async openConfigPanel(panel: ConfigPanelType): Promise<void> {
    await this.configPanelOrchestrator.openConfigPanel(panel, {
      updateMessages: (fn: (msgs: ChatMessage[]) => ChatMessage[]) => this.messages.update(fn),
      appendMessages: (msgs: ChatMessage[]) => this.messages.update((prev) => [...prev, ...msgs]),
    });
  }

  /** Save a config panel field update */
  async saveConfigField(
    panel: ConfigPanelType,
    field: string,
    value: unknown,
    messageId: string,
  ): Promise<void> {
    await this.configPanelOrchestrator.saveConfigField(panel, field, value, messageId, {
      updateMessages: (fn: (msgs: ChatMessage[]) => ChatMessage[]) => this.messages.update(fn),
      appendMessages: (msgs: ChatMessage[]) => this.messages.update((prev) => [...prev, ...msgs]),
      applyBranding: (branding: BrandingConfig) => this.themeService.branding.set(branding),
    });
  }

  /** Build suggestion chips relevant to a config panel */
  private _configSuggestions(currentPanel: string): Array<{ label: string; prompt: string }> {
    return this.configPanelOrchestrator.getConfigSuggestions(currentPanel);
  }



  /** Upload a logo file (T10.2) */
  async uploadBrandingLogo(file: File, messageId: string): Promise<void> {
    try {
      const result = await this.configService.uploadLogo(file);
      // Update branding in the current panel
      this.messages.update((msgs) =>
        msgs.map((m) => {
          if (m.id === messageId && m.configData && 'logo_url' in (m.configData as BrandingConfig)) {
            return { ...m, configData: { ...(m.configData as BrandingConfig), logo_url: result.logo_url } };
          }
          return m;
        }),
      );
      this.messages.update((msgs) => [
        ...msgs,
        {
          id: crypto.randomUUID(),
          role: 'assistant',
          content: `✅ Logo uploaded (${result.filename}, ${Math.round(parseInt(result.size_bytes) / 1024)}KB).`,
          timestamp: new Date(),
        },
      ]);
    } catch (err: unknown) {
      const errorMsg = err instanceof Error ? err.message : 'Logo upload failed.';
      this.messages.update((msgs) => [
        ...msgs,
        {
          id: crypto.randomUUID(),
          role: 'assistant',
          content: `❌ ${errorMsg}`,
          timestamp: new Date(),
        },
      ]);
    }
  }

  /** Trigger the hidden file input for logo upload */
  triggerLogoUpload(messageId: string): void {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = 'image/png,image/jpeg,image/svg+xml,image/webp';
    input.onchange = () => {
      if (input.files?.[0]) {
        this.uploadBrandingLogo(input.files[0], messageId);
      }
    };
    input.click();
  }

  // ── Analytics Dashboard (Phase 12) ──────────────────────────────────

  /** Open the analytics dashboard inline in the chat */
  async openAnalyticsDashboard(): Promise<void> {
    await this.analyticsOrchestrator.openDashboard({
      updateMessages: (fn: (msgs: ChatMessage[]) => ChatMessage[]) => this.messages.update(fn),
      appendMessages: (msgs: ChatMessage[]) => this.messages.update((prev) => [...prev, ...msgs]),
    });
  }

  // ── P0-B: Pinned View Persistence Helpers ─────────────────────────────

  /** Load pinned views from localStorage (called during signal initialization). */
  private _loadPinnedViews(): ViewSpec[] {
    try {
      const stored = localStorage.getItem(PINNED_VIEWS_KEY);
      if (stored) {
        const views = JSON.parse(stored) as ViewSpec[];
        if (Array.isArray(views) && views.length > 0) {
          // Defer setting the active view to avoid signal-in-signal-init
          queueMicrotask(() => this.activePinnedView.set(views[0].view_id));
          return views;
        }
      }
    } catch { /* ignore corrupt data */ }
    return [];
  }

  // ── P1-B: Auto-Refresh Timers ────────────────────────────────────────

  private readonly _refreshTimers = new Map<string, ReturnType<typeof setInterval>>();

  /**
   * Start periodic auto-refresh for a pinned view.
   * Re-sends the original prompt as a background query (not persisted).
   */
  private _startAutoRefresh(viewId: string, intervalSec: number, sourcePrompt: string): void {
    this._stopAutoRefresh(viewId);
    const timer = setInterval(async () => {
      try {
        await this.chatService.streamMessage(
          { sessionId: this.sessionId, message: sourcePrompt, background: true },
          {
            onComponent: (comp) => {
              if (comp.type === 'view' && (comp as any).view_id === viewId) {
                this.pinnedViews.update(views =>
                  views.map(v => v.view_id === viewId ? (comp as ViewSpec) : v),
                );
              }
            },
          },
        );
      } catch { /* background refresh failed — silent */ }
    }, intervalSec * 1000);
    this._refreshTimers.set(viewId, timer);
  }

  /** Stop auto-refresh for a specific view. */
  private _stopAutoRefresh(viewId: string): void {
    const timer = this._refreshTimers.get(viewId);
    if (timer) {
      clearInterval(timer);
      this._refreshTimers.delete(viewId);
    }
  }

  /** Stop all auto-refresh timers. */
  private _stopAllAutoRefresh(): void {
    this._refreshTimers.forEach((_, viewId) => this._stopAutoRefresh(viewId));
  }

  // ── Workflow Execution ──────────────────────────────────────────────────

  private _workflowCanvas = viewChild<WorkflowCanvasComponent>('workflowCanvas');
  private _execAbort: AbortController | null = null;
  private _toolsLoaded = false;

  /** Load tool catalog into canvas when it first appears. */
  private _loadToolCatalog = effect(() => {
    const canvas = this._workflowCanvas();
    const wf = this.currentWorkflow();
    if (canvas && wf && !this._toolsLoaded) {
      this._toolsLoaded = true;
      this.auth.getIdToken().then(token => {
        this.workflowService.getAvailableTools(token ?? undefined).then(catalog => {
          const tools = catalog.tools ?? [];
          canvas.loadToolCatalog(tools as ToolDefinition[]);
          console.log(`[ChatShell] loaded ${tools.length} tools into canvas`);
        }).catch(e => console.warn('[ChatShell] failed to load tool catalog:', e));
      });
    }
    // Reset when no workflow
    if (!wf) this._toolsLoaded = false;
  });

  /** Run the current workflow via the backend SSE /execute endpoint. */
  onRunWorkflow(): void {
    const spec = this.currentWorkflow();
    const canvas = this._workflowCanvas();
    if (!spec || !canvas) return;

    if (spec.inputs && spec.inputs.length > 0) {
      const dialogRef = this.dialog.open(WorkflowRunDialogComponent, {
        data: { inputs: spec.inputs },
        width: '400px',
      });

      dialogRef.afterClosed().subscribe((result) => {
        if (result) {
          this._executeWorkflow(spec, canvas, result);
        }
      });
    } else {
      this._executeWorkflow(spec, canvas);
    }
  }

  /** Execute the workflow starting from a specific node (partial re-execution). */
  onRunFromNode(nodeId: string): void {
    const spec = this.currentWorkflow();
    const canvas = this._workflowCanvas();
    if (!spec || !canvas) return;

    if (spec.inputs && spec.inputs.length > 0) {
      const dialogRef = this.dialog.open(WorkflowRunDialogComponent, {
        data: { inputs: spec.inputs },
        width: '400px',
      });

      dialogRef.afterClosed().subscribe((result) => {
        if (result) {
          this._executeWorkflow(spec, canvas, result, nodeId);
        }
      });
    } else {
      this._executeWorkflow(spec, canvas, undefined, nodeId);
    }
  }

  private _executeWorkflow(spec: WorkflowSpec, canvas: WorkflowCanvasComponent, inputVariables?: Record<string, unknown>, startNodeId?: string): void {
    if (startNodeId) {
      console.log(`[ChatShell] partial re-exec from node "${startNodeId}"`);
    }
    canvas.resetExecution();
    canvas.setExecutionStatus('running');
    this._execAbort = new AbortController();

    this.workflowService.streamExecute(spec, '', {
      onExecutionStart: (state) => {
        canvas.setExecutionStatus('running');
        if (state.nodes) {
          Object.values(state.nodes).forEach(n => {
            if (n.status === 'skipped') {
              canvas.setNodeStatus(n.node_id, 'skipped');
            }
          });
        }
      },
      onNodeStart: (nId: string) => canvas.setNodeStatus(nId, 'running'),
      onNodeComplete: (nId: string, _label: string, durationMs: number) => {
        canvas.setNodeStatus(nId, 'completed', durationMs);
      },
      onNodeError: (nId: string, _label: string, error: string) => {
        canvas.setNodeStatus(nId, 'error');
        console.error(`Node ${nId} error:`, error);
      },
      onExecutionComplete: () => {
        canvas.setExecutionStatus('completed');
        // Refresh run history after completion so the new run appears in the dropdown
        this.loadRunHistory();
      },
      onExecutionError: (err: string) => {
        canvas.setExecutionStatus('error');
        console.error('Workflow execution error:', err);
        // Also refresh on error so the failed run appears in history
        this.loadRunHistory();
      },
    }, undefined, false, startNodeId, undefined, inputVariables);
  }

  /** Abort a running workflow execution. */
  onStopExecution(): void {
    this._execAbort?.abort();
    this._execAbort = null;
    this._workflowCanvas()?.setExecutionStatus('idle');
  }

  // ── Debounced Auto-Save ────────────────────────────────────────────────

  private _autoSaveTimer: ReturnType<typeof setTimeout> | null = null;
  private static readonly AUTO_SAVE_DELAY = 1500;

  /** Handle spec changes from the canvas — update local state and debounce persist. */
  onWorkflowSpecChange(updated: WorkflowSpec): void {
    this.currentWorkflow.set(updated);
    this._scheduleAutoSave(updated);
  }

  private _scheduleAutoSave(spec: WorkflowSpec): void {
    if (this._autoSaveTimer) clearTimeout(this._autoSaveTimer);

    const canvas = this._workflowCanvas();
    canvas?.saveStatus.set('idle');

    this._autoSaveTimer = setTimeout(async () => {
      if (!spec.id) return; // Not yet persisted
      canvas?.saveStatus.set('saving');
      try {
        const token = await this.auth.getIdToken();
        await this.workflowService.updateWorkflow(spec.id, spec, token ?? undefined);
        canvas?.saveStatus.set('saved');
        console.log(`[ChatShell] auto-saved workflow ${spec.id}`);
        // Reset "saved" indicator after 3s
        setTimeout(() => canvas?.saveStatus.set('idle'), 3000);
      } catch (e) {
        console.error('[ChatShell] auto-save failed:', e);
        canvas?.saveStatus.set('error');
      }
    }, ChatShellComponent.AUTO_SAVE_DELAY);
  }

  // ── AI Prompt Regeneration ────────────────────────────────────────────

  async onRegeneratePrompt(event: {
    nodeId: string;
    nodeLabel: string;
    nodeDescription: string;
    currentPrompt: string;
    instruction: string;
  }): Promise<void> {
    const wf = this.currentWorkflow();
    if (!wf) return;

    console.log(`[ChatShell] regenerate prompt for node=${event.nodeId}, instruction="${event.instruction.slice(0, 50)}"`);
    try {
      const token = await this.auth.getIdToken();
      const improved = await this.workflowService.regeneratePrompt(
        {
          node_id: event.nodeId,
          node_label: event.nodeLabel,
          node_description: event.nodeDescription,
          current_prompt: event.currentPrompt,
          instruction: event.instruction,
          workflow_context: wf as unknown as Record<string, unknown>,
        },
        token ?? undefined,
      );
      console.log(`[ChatShell] regenerated prompt: ${improved.length} chars`);
      this._workflowCanvas()?.applyRegeneratedPrompt(event.nodeId, improved);
    } catch (e) {
      console.error('[ChatShell] prompt regeneration failed:', e);
      this._workflowCanvas()?.regenerationFailed();
    }
  }

  // ── Delete / Duplicate Workflow ────────────────────────────────────────

  async onDeleteWorkflow(workflowId: string): Promise<void> {
    if (!workflowId) return;
    if (!confirm('Delete this workflow and all execution history? This cannot be undone.')) return;

    try {
      const token = await this.auth.getIdToken();
      await this.workflowService.deleteWorkflow(workflowId, token ?? undefined);
      console.log(`[ChatShell] deleted workflow ${workflowId}`);
      // Clear current workflow & switch to chat
      this.currentWorkflow.set(null);
      this.mainTab.set('chat');
      // Refresh sidebar list
      this.loadSavedWorkflows();
    } catch (e) {
      console.error('[ChatShell] delete failed:', e);
    }
  }

  async onDuplicateWorkflow(workflowId: string): Promise<void> {
    if (!workflowId) return;
    try {
      const token = await this.auth.getIdToken();
      const newId = await this.workflowService.duplicateWorkflow(workflowId, token ?? undefined);
      console.log(`[ChatShell] duplicated workflow ${workflowId} → ${newId}`);
      // Load the duplicate
      this.loadSavedWorkflow(newId);
      // Refresh sidebar list
      this.loadSavedWorkflows();
    } catch (e) {
      console.error('[ChatShell] duplicate failed:', e);
    }
  }

  async onShareWorkflow(workflowId: string): Promise<void> {
    if (!workflowId) return;
    this.isLoading.set(true);
    try {
      const authToken = (await this.auth.getIdToken().catch(() => undefined)) ?? undefined;
      const shareToken = await this.workflowService.shareWorkflow(workflowId, authToken);
      const shareUrl = `${window.location.origin}/shared/${shareToken}`;
      
      await navigator.clipboard?.writeText(shareUrl);
      
      this.messages.update((msgs) => [
        ...msgs,
        {
          id: crypto.randomUUID(),
          role: 'assistant',
          content: '',
          timestamp: new Date(),
          uiComponents: [
            {
              type: 'info_banner',
              title: 'Workflow Shared',
              body: `A shareable link has been generated and copied to your clipboard: ${shareUrl}`,
              style: 'success',
            },
          ],
        },
      ]);
      this.mainTab.set('chat');
    } catch (err) {
      console.error('[ChatShell] share workflow failed:', err);
      const errorMsg = err instanceof Error ? err.message : 'Failed to share workflow.';
      this.messages.update((msgs) => [
        ...msgs,
        {
          id: crypto.randomUUID(),
          role: 'assistant',
          content: '',
          timestamp: new Date(),
          uiComponents: [
            {
              type: 'info_banner',
              title: 'Share Failed',
              body: errorMsg,
              style: 'error',
            },
          ],
        },
      ]);
      this.mainTab.set('chat');
    } finally {
      this.isLoading.set(false);
    }
  }

  async loadSharedWorkflow(token: string): Promise<void> {
    console.log(`[ChatShell] loadSharedWorkflow: loading token ${token}...`);
    try {
      const authToken = (await this.auth.getIdToken().catch(() => undefined)) ?? undefined;
      const spec = await this.workflowService.getSharedWorkflow(token);
      if (spec) {
        console.log(`[ChatShell] loadSharedWorkflow: loaded shared workflow "${spec.name}"`);
        this.currentWorkflow.set(spec);
        this.mainTab.set('workflow');
      } else {
        console.warn(`[ChatShell] loadSharedWorkflow: returned null for token ${token}`);
        this.messages.update((msgs) => [
          ...msgs,
          {
            id: crypto.randomUUID(),
            role: 'assistant',
            content: `❌ The shared workflow link is invalid or has expired.`,
            timestamp: new Date(),
          },
        ]);
      }
    } catch (err) {
      console.error('[ChatShell] loadSharedWorkflow failed:', err);
      this.messages.update((msgs) => [
        ...msgs,
        {
          id: crypto.randomUUID(),
          role: 'assistant',
          content: `❌ Failed to load shared workflow. Please try again.`,
          timestamp: new Date(),
        },
      ]);
    }
  }

  ngOnDestroy(): void {
    this._stopAllAutoRefresh();
    if (this._pinnedSyncTimer) clearTimeout(this._pinnedSyncTimer);
    if (this._autoSaveTimer) clearTimeout(this._autoSaveTimer);
    this._execAbort?.abort();
  }
}
