import { Component, signal, inject, ElementRef, viewChild, effect, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { toSignal } from '@angular/core/rxjs-interop';
import { map } from 'rxjs';
import {
  ComponentSpec,
  type ViewSpec,
} from '@synaptiq/constants';
import { DslRendererComponent, FormSubmitEvent } from '@synaptiq/dsl-renderer';
import {
  ActionsService,
  AnalyticsService,
  ChatService,
  ConfigService,
  SessionService,
  SessionListItem,
  type AnalyticsSummary,
  type TokenUsageSummary,
  type AIPersonaConfig,
  type LLMProviderConfig,
  type AIGuardrailsConfig,
  type ComponentEnablement,
  type ActionsConfig,
  type BrandingConfig,
  type BrandingPatchResponse,
  type ThemePreset,
  type PersonalizationConfig,
  type ContrastCheck,
} from '@synaptiq/chat';
import { AuthService } from '@synaptiq/auth';
import { ENVIRONMENT } from '@synaptiq/utils';
import { ThemeService } from '../../core/theme.service';
import { MarkdownPipe } from '../../core/markdown.pipe';

// ---------------------------------------------------------------------------
// Message model
// ---------------------------------------------------------------------------

export interface ChatMessage {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: Date;
  /** DSL components rendered inline (e.g. item cards, forms, tables). */
  uiComponents?: ComponentSpec[];
  /** True after a form within this message has been submitted. */
  formSubmitted?: boolean;
  /** Status message shown during streaming (e.g., "Using keyword search..."). */
  statusText?: string;
  /** Inline auth form embedded in this message. */
  authForm?: 'signin' | 'signup';
  /** Admin config panel type for inline rendering (T9.6). */
  configPanel?: 'persona' | 'provider' | 'guardrails' | 'components' | 'actions' | 'branding' | 'themes' | 'personalization';
  /** Loaded config data for an admin config panel. */
  configData?: AIPersonaConfig | LLMProviderConfig | AIGuardrailsConfig | ComponentEnablement | ActionsConfig | BrandingConfig | ThemePreset[] | PersonalizationConfig;
  /** WCAG contrast check result (branding panel). */
  contrastCheck?: ContrastCheck;
  /** Analytics dashboard data (Phase 12). */
  analyticsData?: { summary?: AnalyticsSummary; tokens?: TokenUsageSummary };
  /** Whether this is a typing indicator placeholder. */
  isTyping?: boolean;
}

// ---------------------------------------------------------------------------
// Component
// ---------------------------------------------------------------------------

@Component({
  selector: 'sq-chat-shell',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, MatButtonModule, MatTooltipModule, DslRendererComponent, MarkdownPipe],
  templateUrl: './chat-shell.component.html',
  styleUrl: './chat-shell.component.scss',
})
export class ChatShellComponent {
  private readonly breakpoints = inject(BreakpointObserver);
  private readonly actionsService = inject(ActionsService);
  private readonly analyticsService = inject(AnalyticsService);
  private readonly chatService = inject(ChatService);
  private readonly configService = inject(ConfigService);
  private readonly sessionService = inject(SessionService);
  readonly auth = inject(AuthService);
  private readonly env = inject(ENVIRONMENT);
  readonly themeService = inject(ThemeService);

  /** Exposed for settings panel template */
  readonly apiBaseUrl = this.env.apiBaseUrl;
  private messagesEnd = viewChild<ElementRef<HTMLDivElement>>('messagesEnd');

  /** Active session ID for the current conversation. */
  private sessionId: string = crypto.randomUUID();
  /** Whether the current session has been persisted to the backend. */
  private sessionPersisted = false;

  isMobile = toSignal(
    this.breakpoints.observe([Breakpoints.XSmall, Breakpoints.Small]).pipe(map((r) => r.matches)),
    { initialValue: false },
  );

  sidebarOpen = signal(true);
  inputValue = signal('');
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

  messages = signal<ChatMessage[]>([]);

  /** Pinned views — persistent surfaces above the chat stream */
  readonly pinnedViews = signal<ViewSpec[]>([]);
  readonly activePinnedView = signal<string>('');
  readonly pinnedExpanded = signal(true);
  readonly activePinnedViewSpec = computed(() => {
    const views = this.pinnedViews();
    const activeId = this.activePinnedView();
    return views.find(v => v.view_id === activeId) || null;
  });

  /** Whether the authenticated user has admin/owner privileges. */
  readonly isAdminMode = this.auth.isAdmin;

  /** Loaded AI persona config from tenant — drives welcome message & starter prompts */
  private personaName = 'Synaptiq';
  private personaWelcome = '';
  private personaStarters: string[] = [];

  /** Build the welcome message based on user role and loaded persona config. */
  private _buildInitialMessages(): ChatMessage[] {
    const isAdmin = this.auth.isAdmin();
    const isGuest = this.auth.isGuest();
    const isLoggedIn = this.auth.isLoggedIn();
    const user = this.auth.currentUser();
    const name = this.personaName || 'Synaptiq';

    // Build starter prompt chips from persona config
    const starterChips: Array<{ label: string; prompt: string }> = this.personaStarters.map(
      (prompt) => ({ label: prompt, prompt }),
    );

    // Not logged in at all (emulator down, first visit, etc.)
    if (!isLoggedIn) {
      const welcomeText = this.personaWelcome
        || `Hi! 👋 I'm **${name}** — your AI-powered assistant. Sign in to save your conversations and access all features, or start exploring right away!`;
      return [
        {
          id: '1',
          role: 'assistant',
          content: welcomeText,
          timestamp: new Date(),
          uiComponents: [
            {
              type: 'info_banner',
              title: 'Quick start',
              body: 'Try the suggestions below to explore, or sign in via the topbar.',
              style: 'info',
              suggestions: [
                { label: 'Sign in', prompt: '__SIGN_IN__' },
                { label: 'Sign up', prompt: '__SIGN_UP__' },
                ...starterChips,
              ],
            },
          ],
        },
      ];
    }

    if (isAdmin) {
      return [
        {
          id: '1',
          role: 'assistant',
          content:
            `Welcome back${user?.displayName ? ', ' + user.displayName : ''}! I'm ${name} in **admin mode**. I can help you manage your data, configure your workspace, view analytics, and more — all through this chat.\n\nWhat would you like to do?`,
          timestamp: new Date(),
          uiComponents: [
            {
              type: 'info_banner',
              title: 'Admin Actions',
              body: 'Configure your workspace, manage data, or check usage analytics.',
              style: 'info',
              suggestions: [
                { label: '📊 Analytics', prompt: '__ANALYTICS_DASHBOARD__' },
                { label: '🤖 AI Config', prompt: '__CONFIG_PERSONA__' },
                { label: '🔧 Provider', prompt: '__CONFIG_PROVIDER__' },
                { label: '🛡️ Guardrails', prompt: '__CONFIG_GUARDRAILS__' },
                { label: '🧩 Components', prompt: '__CONFIG_COMPONENTS__' },
                { label: '⚡ Actions', prompt: '__CONFIG_ACTIONS__' },
                { label: '🎨 Branding', prompt: '__CONFIG_BRANDING__' },
                { label: '🎭 Themes', prompt: '__CONFIG_THEMES__' },
                { label: '👤 Personalization', prompt: '__CONFIG_PERSONALIZATION__' },
                { label: '📋 View Schema', prompt: 'Show my schema' },
              ],
            },
          ],
        },
      ];
    }

    if (isGuest) {
      const welcomeText = this.personaWelcome
        || `👋 Hi! I'm **${name}** — your AI-powered assistant. You're currently browsing as a **guest**.\n\nYou can explore and chat right away. To save your conversations and unlock admin features, sign in anytime.`;
      return [
        {
          id: '1',
          role: 'assistant',
          content: welcomeText,
          timestamp: new Date(),
          uiComponents: [
            {
              type: 'info_banner',
              title: 'Quick start',
              body: 'Try any of these to get started:',
              style: 'info',
              suggestions: [
                { label: 'Sign in', prompt: '__SIGN_IN__' },
                { label: 'Sign up', prompt: '__SIGN_UP__' },
                ...starterChips,
              ],
            },
          ],
        },
      ];
    }

    // Authenticated non-admin user
    const welcomeText = this.personaWelcome
      || `Hi${user?.displayName ? ' ' + user.displayName : ''}! 👋 I'm ${name}. Ask me anything — I can search, analyse, and visualise your data for you.`;
    return [
      {
        id: '1',
        role: 'assistant',
        content: welcomeText,
        timestamp: new Date(),
        uiComponents: [
          {
            type: 'info_banner',
            title: 'Quick start',
            body: 'Try the suggestions below to get started.',
            style: 'info',
            suggestions: starterChips,
          },
        ],
      },
    ];
  }

  constructor() {
    // Auto-scroll to bottom on new messages
    effect(() => {
      this.messages();
      setTimeout(() => this.messagesEnd()?.nativeElement?.scrollIntoView({ behavior: 'smooth' }), 50);
    });

    // Fetch public tenant persona config (welcome message + starter prompts)
    const personaReady = this._loadPersona();

    // Watch for auth readiness — auto-sign-in as guest and show welcome
    effect(() => {
      const loading = this.auth.isLoading();
      if (!loading && this.messages().length === 0) {
        // Auth finished loading — wait for persona config before building messages
        if (!this.auth.isLoggedIn()) {
          // No user at all — sign in anonymously for immediate access
          this.auth.signInAsGuest()
            .then(() => personaReady)
            .then(() => this.messages.set(this._buildInitialMessages()))
            .catch(() => {
              // Emulator might not be running — show welcome anyway
              this.messages.set(this._buildInitialMessages());
            });
        } else {
          // User already authenticated (page refresh, etc.)
          personaReady.then(() => {
            this.messages.set(this._buildInitialMessages());
            this.loadSessionHistory();
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
        this.personaName = persona.display_name || 'Synaptiq';
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
      this.sessionHistory.set(response.sessions);
    } catch {
      // Silently fail — sidebar shows empty list
    }
  }

  async loadSession(session: SessionListItem): Promise<void> {
    this.chatService.abort();
    this.isLoading.set(true);
    this.sessionId = session.session_id;
    this.sessionPersisted = true;
    this.activeSessionId.set(session.session_id);

    // Close sidebar on mobile
    if (this.isMobile()) {
      this.sidebarOpen.set(false);
    }

    try {
      const history = await this.sessionService.getHistory(session.session_id);
      const restoredMessages: ChatMessage[] = history.turns.map((turn, i) => ({
        id: `restored-${i}`,
        role: turn.role,
        content: turn.content,
        timestamp: turn.timestamp ? new Date(turn.timestamp) : new Date(),
        uiComponents: (turn.components as ComponentSpec[]) ?? [],
      }));

      this.messages.set(
        restoredMessages.length > 0
          ? restoredMessages
          : [{
              id: crypto.randomUUID(),
              role: 'assistant',
              content: 'Conversation loaded. How can I help?',
              timestamp: new Date(),
            }],
      );
    } catch {
      this.messages.set([{
        id: crypto.randomUUID(),
        role: 'assistant',
        content: 'Could not load the conversation. Starting fresh.',
        timestamp: new Date(),
      }]);
    } finally {
      this.isLoading.set(false);
    }
  }

  async deleteSession(session: SessionListItem, event: Event): Promise<void> {
    event.stopPropagation();
    try {
      await this.sessionService.deleteSession(session.session_id);
      this.sessionHistory.update((s) => s.filter((h) => h.session_id !== session.session_id));
      if (this.sessionId === session.session_id) {
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
          ? '📝 Create your account below to save conversations and unlock all features.'
          : '🔐 Sign in to your account below.',
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
              ? '📝 Create your account below to save conversations and unlock all features.'
              : '🔐 Sign in to your account below.' }
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

  // ── Messaging ────────────────────────────────────────────────────────

  async sendMessage(text?: string): Promise<void> {
    const msg = (text ?? this.inputValue()).trim();
    if (!msg || this.isLoading()) return;

    // Add user message
    this.messages.update((msgs) => [
      ...msgs,
      { id: crypto.randomUUID(), role: 'user', content: msg, timestamp: new Date() },
    ]);
    this.inputValue.set('');
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
    // Remove typing indicator — we'll show real content now
    if (typingId) this._removeTyping(typingId);

    // Create a placeholder assistant message that we'll update with streaming tokens
    const assistantMsgId = crypto.randomUUID();
    this.messages.update((msgs) => [
      ...msgs,
      {
        id: assistantMsgId,
        role: 'assistant',
        content: '',
        timestamp: new Date(),
        uiComponents: [],
      },
    ]);

    await this.chatService.streamMessage(
      {
        session_id: this.sessionId,
        message: msg,
      },
      {
        onToken: (token) => {
          // Append token to the assistant message content (typewriter effect)
          this.messages.update((msgs) =>
            msgs.map((m) =>
              m.id === assistantMsgId ? { ...m, content: m.content + token } : m,
            ),
          );
        },

        onComponent: (component) => {
          // Check if this is a pinned view — hoist it to the pinned views strip
          if (component.type === 'view' && (component as any).pinned) {
            const viewSpec = component as any;
            this.pinnedViews.update(views => {
              // Replace existing view with same ID, or add new
              const existing = views.findIndex(v => v.view_id === viewSpec.view_id);
              if (existing >= 0) {
                const updated = [...views];
                updated[existing] = viewSpec;
                return updated;
              }
              return [...views, viewSpec];
            });
            this.activePinnedView.set(viewSpec.view_id);
            this.pinnedExpanded.set(true);
            return; // Don't add pinned views to inline components
          }

          // Push DSL component into the assistant message
          this.messages.update((msgs) =>
            msgs.map((m) =>
              m.id === assistantMsgId
                ? { ...m, uiComponents: [...(m.uiComponents ?? []), component] }
                : m,
            ),
          );
        },

        onStatus: (statusMessage) => {
          // Update status text on the assistant message
          this.messages.update((msgs) =>
            msgs.map((m) =>
              m.id === assistantMsgId ? { ...m, statusText: statusMessage } : m,
            ),
          );
        },

        onTextReplace: (text) => {
          // Replace streamed text with cleaned version (raw JSON stripped out)
          this.messages.update((msgs) =>
            msgs.map((m) =>
              m.id === assistantMsgId ? { ...m, content: text } : m,
            ),
          );
        },

        onStepStart: (event) => {
          // Show tool invocation as a status indicator on the message
          this.messages.update((msgs) =>
            msgs.map((m) =>
              m.id === assistantMsgId
                ? { ...m, statusText: event.description }
                : m,
            ),
          );
        },

        onStepComplete: () => {
          // Clear the step status — the model will stream text next
          this.messages.update((msgs) =>
            msgs.map((m) =>
              m.id === assistantMsgId
                ? { ...m, statusText: undefined }
                : m,
            ),
          );
        },

        onDone: () => {
          // Clear status text and loading state
          this.messages.update((msgs) =>
            msgs.map((m) =>
              m.id === assistantMsgId ? { ...m, statusText: undefined } : m,
            ),
          );
          this.isLoading.set(false);

          // Auto-persist session after first successful exchange
          if (!this.sessionPersisted) {
            this.sessionPersisted = true;
            this.activeSessionId.set(this.sessionId);
            this.sessionService.createSession(this.sessionId).catch(() => {});
            // Refresh sidebar to show new session
            this.loadSessionHistory();
          }
        },

        onError: (errorMessage) => {
          // Show error as info_banner
          if (errorMessage.includes('Failed to fetch') || errorMessage.includes('NetworkError')) {
            errorMessage = 'Backend is offline. Please start the API server and try again.';
          }

          this.messages.update((msgs) =>
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
                        body: errorMessage,
                        style: 'error' as const,
                        suggestions: [
                          { label: 'Try again', prompt: msg },
                        ],
                      },
                    ],
                  }
                : m,
            ),
          );
          this.isLoading.set(false);
        },
      },
    );
  }

  // ── Offline fallback ─────────────────────────────────────────────────

  private async sendViaDemo(msg: string, typingId?: string): Promise<void> {
    // Simulate network delay
    await new Promise((r) => setTimeout(r, 600));

    // Remove typing indicator
    if (typingId) this._removeTyping(typingId);

    this.messages.update((msgs) => [
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
            body: 'The API server is not connected. Please start the backend to use Synaptiq.',
            style: 'warning' as const,
            suggestions: [
              { label: 'Retry', prompt: msg },
            ],
          },
        ],
      },
    ]);
    this.isLoading.set(false);
  }

  /** Remove a pinned view and select the next one if needed. */
  unpinView(viewId: string, event: Event): void {
    event.stopPropagation();
    this.pinnedViews.update(views => views.filter(v => v.view_id !== viewId));
    if (this.activePinnedView() === viewId) {
      const remaining = this.pinnedViews();
      this.activePinnedView.set(remaining.length > 0 ? remaining[0].view_id : '');
    }
  }

  onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  trackById(_: number, msg: ChatMessage): string {
    return msg.id;
  }

  // ── DSL component events ─────────────────────────────────────────────

  /**
   * When a user clicks an AI suggestion chip on a DSL component,
   * inject the prompt into the chat input and auto-send it.
   */
  onSuggestionClicked(prompt: string): void {
    if (prompt === '__SIGN_IN__') {
      this.openAuthForm('signin');
      return;
    }
    if (prompt === '__SIGN_UP__') {
      this.openAuthForm('signup');
      return;
    }
    // Analytics dashboard chip (Phase 12)
    if (prompt === '__ANALYTICS_DASHBOARD__') { this.openAnalyticsDashboard(); return; }
    // Admin config panel chips
    if (prompt === '__CONFIG_PERSONA__') { this.openConfigPanel('persona'); return; }
    if (prompt === '__CONFIG_PROVIDER__') { this.openConfigPanel('provider'); return; }
    if (prompt === '__CONFIG_GUARDRAILS__') { this.openConfigPanel('guardrails'); return; }
    if (prompt === '__CONFIG_COMPONENTS__') { this.openConfigPanel('components'); return; }
    if (prompt === '__CONFIG_ACTIONS__') { this.openConfigPanel('actions'); return; }
    if (prompt === '__CONFIG_BRANDING__') { this.openConfigPanel('branding'); return; }
    if (prompt === '__CONFIG_THEMES__') { this.openConfigPanel('themes'); return; }
    if (prompt === '__CONFIG_PERSONALIZATION__') { this.openConfigPanel('personalization'); return; }
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
   */
  onActionClicked(event: { action: string; item_id?: string }): void {
    this.messages.update((msgs) => [
      ...msgs,
      {
        id: crypto.randomUUID(),
        role: 'assistant',
        content: `Action "${event.action}" triggered${event.item_id ? ` for item ${event.item_id}` : ''}.`,
        timestamp: new Date(),
      },
    ]);
  }

  // ── Admin Config Panels (T9.6) ──────────────────────────────────────

  /** Open an admin config panel inline in the chat */
  async openConfigPanel(panel: 'persona' | 'provider' | 'guardrails' | 'components' | 'actions' | 'branding' | 'themes' | 'personalization'): Promise<void> {
    const msgId = crypto.randomUUID();
    const panelTitles: Record<string, string> = {
      persona: '🤖 AI Persona Configuration',
      provider: '🔧 LLM Provider Settings',
      guardrails: '🛡️ AI Guardrails',
      components: '🧩 Component Enablement',
      actions: '⚡ Action Settings',
      branding: '🎨 Branding & Colors',
      themes: '🎭 Theme Presets',
      personalization: '👤 End-User Personalization',
    };

    // Show loading state
    this.messages.update((msgs) => [
      ...msgs,
      {
        id: msgId,
        role: 'assistant',
        content: `Loading ${panelTitles[panel]}...`,
        timestamp: new Date(),
        configPanel: panel,
      },
    ]);

    try {
      let configData: AIPersonaConfig | LLMProviderConfig | AIGuardrailsConfig | ComponentEnablement | ActionsConfig | BrandingConfig | ThemePreset[] | PersonalizationConfig;
      let contrastCheck: ContrastCheck | undefined;

      switch (panel) {
        case 'persona':
          configData = await this.configService.getAIPersona();
          break;
        case 'provider':
          configData = await this.configService.getLLMProvider();
          break;
        case 'guardrails':
          configData = await this.configService.getAIGuardrails();
          break;
        case 'components':
          configData = await this.configService.getComponents();
          break;
        case 'actions':
          configData = await this.configService.getActions();
          break;
        case 'branding': {
          const brandResp = await this.configService.getBranding();
          configData = brandResp;
          // Run inline contrast check
          try {
            contrastCheck = await this.configService.checkContrast(
              brandResp.primary_color,
              brandResp.background_style === 'light' ? 'light' : 'dark',
            );
          } catch { /* non-critical */ }
          break;
        }
        case 'themes':
          configData = await this.configService.getThemes();
          break;
        case 'personalization':
          configData = await this.configService.getPersonalization();
          break;
      }

      this.messages.update((msgs) =>
        msgs.map((m) =>
          m.id === msgId
            ? {
                ...m,
                content: panelTitles[panel],
                configData,
                contrastCheck,
                uiComponents: [
                  {
                    type: 'info_banner' as const,
                    title: panelTitles[panel],
                    body: 'Current settings loaded. Use the panel below to modify.',
                    style: 'info' as const,
                    suggestions: this._configSuggestions(panel),
                  },
                ],
              }
            : m,
        ),
      );
    } catch (err: unknown) {
      const errorMsg = err instanceof Error ? err.message : 'Failed to load configuration.';
      this.messages.update((msgs) =>
        msgs.map((m) =>
          m.id === msgId
            ? {
                ...m,
                content: '',
                configPanel: undefined,
                uiComponents: [
                  {
                    type: 'info_banner' as const,
                    title: 'Config Error',
                    body: errorMsg,
                    style: 'error' as const,
                    suggestions: [{ label: 'Retry', prompt: `__CONFIG_${panel.toUpperCase()}__` }],
                  },
                ],
              }
            : m,
        ),
      );
    }
  }

  /** Save a config panel field update */
  async saveConfigField(
    panel: 'persona' | 'provider' | 'guardrails' | 'components' | 'actions' | 'branding' | 'themes' | 'personalization',
    field: string,
    value: unknown,
    messageId: string,
  ): Promise<void> {
    try {
      let updated: AIPersonaConfig | LLMProviderConfig | AIGuardrailsConfig | ComponentEnablement | ActionsConfig | BrandingConfig | ThemePreset[] | PersonalizationConfig;
      let contrastCheck: ContrastCheck | undefined;

      switch (panel) {
        case 'persona':
          updated = await this.configService.patchAIPersona({ [field]: value } as Partial<AIPersonaConfig>);
          break;
        case 'provider':
          updated = await this.configService.patchLLMProvider({ [field]: value } as Partial<LLMProviderConfig>);
          break;
        case 'guardrails':
          updated = await this.configService.patchAIGuardrails({ [field]: value } as Partial<AIGuardrailsConfig>);
          break;
        case 'components':
          updated = await this.configService.patchComponents({ [field]: value } as Partial<ComponentEnablement>);
          break;
        case 'actions':
          updated = await this.configService.patchActions({ [field]: value } as Partial<ActionsConfig>);
          break;
        case 'branding': {
          const resp = await this.configService.patchBranding({ [field]: value } as Partial<BrandingConfig>);
          contrastCheck = resp.contrast_check;
          // eslint-disable-next-line @typescript-eslint/no-unused-vars
          const { contrast_check: _cc, ...brandingOnly } = resp;
          updated = brandingOnly as BrandingConfig;
          // Live-reload branding in the theme service
          this.themeService.branding.set(updated as BrandingConfig);
          break;
        }
        case 'themes':
          updated = await this.configService.getThemes(); // refresh full list
          break;
        case 'personalization':
          updated = await this.configService.patchPersonalization({ [field]: value } as Partial<PersonalizationConfig>);
          break;
      }

      // Update the config data in the message
      this.messages.update((msgs) =>
        msgs.map((m) => m.id === messageId ? { ...m, configData: updated, contrastCheck } : m),
      );

      // Add a success confirmation message
      this.messages.update((msgs) => [
        ...msgs,
        {
          id: crypto.randomUUID(),
          role: 'assistant',
          content: `✅ Updated **${field}** successfully.`,
          timestamp: new Date(),
          uiComponents: [
            {
              type: 'info_banner' as const,
              title: 'Config Saved',
              body: `The ${field} setting has been updated.`,
              style: 'success' as const,
              suggestions: this._configSuggestions(panel),
            },
          ],
        },
      ]);
    } catch (err: unknown) {
      const errorMsg = err instanceof Error ? err.message : 'Failed to save configuration.';
      this.messages.update((msgs) => [
        ...msgs,
        {
          id: crypto.randomUUID(),
          role: 'assistant',
          content: '',
          timestamp: new Date(),
          uiComponents: [
            {
              type: 'info_banner' as const,
              title: 'Save Failed',
              body: errorMsg,
              style: 'error' as const,
              suggestions: [{ label: 'Retry', prompt: `__CONFIG_${panel.toUpperCase()}__` }],
            },
          ],
        },
      ]);
    }
  }

  /** Build suggestion chips relevant to a config panel */
  private _configSuggestions(currentPanel: string): Array<{ label: string; prompt: string }> {
    const all = [
      { label: '📊 Analytics', prompt: '__ANALYTICS_DASHBOARD__', key: 'analytics' },
      { label: '🤖 AI Config', prompt: '__CONFIG_PERSONA__', key: 'persona' },
      { label: '🔧 Provider', prompt: '__CONFIG_PROVIDER__', key: 'provider' },
      { label: '🛡️ Guardrails', prompt: '__CONFIG_GUARDRAILS__', key: 'guardrails' },
      { label: '🧩 Components', prompt: '__CONFIG_COMPONENTS__', key: 'components' },
      { label: '⚡ Actions', prompt: '__CONFIG_ACTIONS__', key: 'actions' },
      { label: '🎨 Branding', prompt: '__CONFIG_BRANDING__', key: 'branding' },
      { label: '🎭 Themes', prompt: '__CONFIG_THEMES__', key: 'themes' },
      { label: '👤 Personalization', prompt: '__CONFIG_PERSONALIZATION__', key: 'personalization' },
    ];
    return all.filter((s) => s.key !== currentPanel).map(({ label, prompt }) => ({ label, prompt }));
  }

  /** Toggle an action's enabled state (immutable list update for template) */
  toggleAction(
    actions: Array<{ action_id: string; enabled: boolean; label: string }>,
    actionId: string,
  ): Array<{ action_id: string; enabled: boolean; label: string }> {
    return actions.map((a) =>
      a.action_id === actionId ? { ...a, enabled: !a.enabled } : a,
    );
  }

  /** Update an action's label (immutable list update for template) */
  updateActionLabel(
    actions: Array<{ action_id: string; enabled: boolean; label: string }>,
    actionId: string,
    newLabel: string,
  ): Array<{ action_id: string; enabled: boolean; label: string }> {
    return actions.map((a) =>
      a.action_id === actionId ? { ...a, label: newLabel } : a,
    );
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
    const msgId = crypto.randomUUID();

    // Show loading state
    this.messages.update((msgs) => [
      ...msgs,
      {
        id: msgId,
        role: 'assistant',
        content: '📊 Loading analytics dashboard…',
        timestamp: new Date(),
      },
    ]);

    try {
      const [summary, tokens] = await Promise.all([
        this.analyticsService.getSummary(),
        this.analyticsService.getTokenUsage(),
      ]);

      // Build analytics DSL components
      const analyticsComponents: ComponentSpec[] = [
        {
          type: 'info_banner' as const,
          title: '📊 Usage Overview (Last 30 Days)',
          body: [
            `**${summary.total_conversations}** conversations · **${summary.total_messages}** messages`,
            `**${summary.unique_users}** unique users · **${summary.avg_messages_per_session}** avg msgs/session`,
            `**${summary.total_actions}** actions performed`,
          ].join('\n'),
          style: 'info' as const,
          suggestions: [
            { label: '🔄 Refresh', prompt: '__ANALYTICS_DASHBOARD__' },
            { label: '🤖 AI Config', prompt: '__CONFIG_PERSONA__' },
            { label: '🔧 Provider', prompt: '__CONFIG_PROVIDER__' },
          ],
        },
        {
          type: 'comparison_table' as const,
          items: [
            {
              item_id: 'token-usage',
              data: {
                'Input Tokens': tokens.total_tokens_input.toLocaleString(),
                'Output Tokens': tokens.total_tokens_output.toLocaleString(),
                'Total Tokens': tokens.total_tokens.toLocaleString(),
                'Est. Cost': `$${tokens.estimated_cost_usd.toFixed(4)}`,
                'Plan Limit': tokens.plan_token_limit ? tokens.plan_token_limit.toLocaleString() : 'Unlimited',
                'Usage': tokens.plan_token_limit ? `${tokens.usage_percent}%` : 'N/A',
              },
            },
          ],
          fields: ['Input Tokens', 'Output Tokens', 'Total Tokens', 'Est. Cost', 'Plan Limit', 'Usage'],
          suggestions: [],
        },
      ];

      // Add action breakdown if present
      if (Object.keys(summary.action_rates).length > 0) {
        analyticsComponents.push({
          type: 'comparison_table' as const,
          items: Object.entries(summary.action_rates).map(([action, count]) => ({
            item_id: action,
            data: { Action: action, Count: count },
          })),
          fields: ['Action', 'Count'],
          suggestions: [],
        });
      }

      this.messages.update((msgs) =>
        msgs.map((m) =>
          m.id === msgId
            ? {
                ...m,
                content: '📊 **Analytics Dashboard**',
                analyticsData: { summary, tokens },
                uiComponents: analyticsComponents,
              }
            : m,
        ),
      );
    } catch (err: unknown) {
      const errorMsg = err instanceof Error ? err.message : 'Failed to load analytics.';
      this.messages.update((msgs) =>
        msgs.map((m) =>
          m.id === msgId
            ? {
                ...m,
                content: '',
                uiComponents: [
                  {
                    type: 'info_banner' as const,
                    title: 'Analytics Error',
                    body: errorMsg,
                    style: 'error' as const,
                    suggestions: [{ label: 'Retry', prompt: '__ANALYTICS_DASHBOARD__' }],
                  },
                ],
              }
            : m,
        ),
      );
    }
  }
}
