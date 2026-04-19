import { Component, signal, inject, ElementRef, viewChild, effect } from '@angular/core';
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
  type FormInputSpec,
} from '@synaptiq/constants';
import { DslRendererComponent, FormSubmitEvent } from '@synaptiq/dsl-renderer';
import { ActionsService, ChatService, SessionService, SessionListItem } from '@synaptiq/chat';
import { ENVIRONMENT } from '@synaptiq/utils';

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
}

// ---------------------------------------------------------------------------
// Demo specs — used as offline fallback when backend is unavailable
// ---------------------------------------------------------------------------

const DEMO_COMPONENTS: Record<string, ComponentSpec[]> = {
  add: [
    {
      type: 'form_input',
      title: 'Add New Product',
      description: 'Fill in the details below to create a catalog item.',
      fields: [
        { field: 'name', label: 'Product Name', type: 'text', required: true, placeholder: 'e.g. Widget Pro' },
        { field: 'price', label: 'Price', type: 'currency', currency_code: 'USD', required: true },
        {
          field: 'category', label: 'Category', type: 'select', options: [
            { label: 'Electronics', value: 'electronics' },
            { label: 'Clothing', value: 'clothing' },
            { label: 'Home & Garden', value: 'home_garden' },
          ],
        },
        { field: 'description', label: 'Description', type: 'textarea', placeholder: 'Short product description…' },
        { field: 'in_stock', label: 'In Stock', type: 'toggle', default_value: true },
      ],
      submit_action: 'create_item',
      submit_label: 'Add Product',
      cancellable: true,
      suggestions: [
        { label: 'Import from CSV', prompt: 'Import products from CSV', icon: 'upload_file' },
      ],
    } satisfies FormInputSpec,
  ],
  compare: [
    {
      type: 'comparison_table',
      items: [
        { item_id: 'sku-001', data: { name: 'Widget Pro', price: 29.99, rating: 4.5 } },
        { item_id: 'sku-002', data: { name: 'Widget Max', price: 49.99, rating: 4.8 } },
        { item_id: 'sku-003', data: { name: 'Widget Lite', price: 14.99, rating: 4.1 } },
      ],
      fields: ['name', 'price', 'rating'],
      suggestions: [
        { label: 'Sort by price ↑', prompt: 'Sort products by price ascending' },
        { label: 'Show top rated', prompt: 'Show me the top rated products' },
      ],
    },
  ],
  search: [
    {
      type: 'result_count',
      shown: 3,
      total: 42,
      suggestions: [
        { label: 'Narrow by category', prompt: 'Filter by category electronics' },
        { label: 'Show all results', prompt: 'Show all 42 results' },
      ],
    },
    {
      type: 'item_grid',
      items: [
        { item_id: 'sku-101', data: { name: 'Smart Speaker', price: 79.99, image: '' } },
        { item_id: 'sku-102', data: { name: 'Wireless Charger', price: 34.99, image: '' } },
        { item_id: 'sku-103', data: { name: 'USB-C Hub', price: 24.99, image: '' } },
      ],
      columns: 3,
      suggestions: [
        { label: 'View as table', prompt: 'Show results as a table' },
        { label: 'Compare these', prompt: 'Compare these 3 items' },
      ],
    },
  ],
};

/**
 * Resolve a user message to demo components by keyword matching.
 * Returns undefined if no demo data matches (fallback to text reply).
 */
function resolveDemoComponents(text: string): ComponentSpec[] | undefined {
  const lower = text.toLowerCase();
  if (lower.includes('add') || lower.includes('new product') || lower.includes('create')) return DEMO_COMPONENTS['add'];
  if (lower.includes('compare') || lower.includes('versus') || lower.includes('vs')) return DEMO_COMPONENTS['compare'];
  if (lower.includes('search') || lower.includes('find') || lower.includes('show')) return DEMO_COMPONENTS['search'];
  return undefined;
}

// ---------------------------------------------------------------------------
// Component
// ---------------------------------------------------------------------------

@Component({
  selector: 'sq-chat-shell',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, MatButtonModule, MatTooltipModule, DslRendererComponent],
  templateUrl: './chat-shell.component.html',
  styleUrl: './chat-shell.component.scss',
})
export class ChatShellComponent {
  private readonly breakpoints = inject(BreakpointObserver);
  private readonly actionsService = inject(ActionsService);
  private readonly chatService = inject(ChatService);
  private readonly sessionService = inject(SessionService);
  private readonly env = inject(ENVIRONMENT);
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

  /** Session history for the sidebar */
  sessionHistory = signal<SessionListItem[]>([]);
  activeSessionId = signal<string>('');

  messages = signal<ChatMessage[]>([
    {
      id: '1',
      role: 'assistant',
      content:
        "Hi! I'm Synaptiq. Ask me anything about your product catalog — I can search, compare, and analyse items for you.\n\nTry these to see DSL components in action:",
      timestamp: new Date(),
      uiComponents: [
        {
          type: 'info_banner',
          title: 'Quick start',
          body: 'Say \"add a product\", \"search electronics\", or \"compare widgets\" to see rich components.',
          style: 'info',
          suggestions: [
            { label: 'Add a product', prompt: 'Add a new product' },
            { label: 'Search catalog', prompt: 'Search electronics' },
            { label: 'Compare items', prompt: 'Compare widgets' },
          ],
        },
      ],
    },
  ]);

  constructor() {
    // Auto-scroll to bottom on new messages
    effect(() => {
      this.messages();
      setTimeout(() => this.messagesEnd()?.nativeElement?.scrollIntoView({ behavior: 'smooth' }), 50);
    });

    // Load session history on startup
    this.loadSessionHistory();
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
    this.messages.set([
      {
        id: crypto.randomUUID(),
        role: 'assistant',
        content: "Starting a new conversation. How can I help you with your catalog?",
        timestamp: new Date(),
        uiComponents: [
          {
            type: 'info_banner',
            title: 'New conversation',
            body: 'Ask me to search, add, compare, or analyze your catalog items.',
            style: 'info',
            suggestions: [
              { label: 'Add a product', prompt: 'Add a new product' },
              { label: 'Search catalog', prompt: 'Search electronics' },
              { label: 'Compare items', prompt: 'Compare widgets' },
            ],
          },
        ],
      },
    ]);
    this.isLoading.set(false);
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

    if (this.useBackend()) {
      await this.sendViaBackend(msg);
    } else {
      await this.sendViaDemo(msg);
    }
  }

  // ── Backend SSE streaming ───────────────────────────────────────────

  private async sendViaBackend(msg: string): Promise<void> {
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
          // Show error as info_banner, fallback to demo if backend unreachable
          if (errorMessage.includes('Failed to fetch') || errorMessage.includes('NetworkError')) {
            // Backend is down — switch to demo mode
            this.useBackend.set(false);
            // Remove the empty placeholder
            this.messages.update((msgs) => msgs.filter((m) => m.id !== assistantMsgId));
            // Retry with demo mode
            this.sendViaDemo(msg);
            return;
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

  // ── Demo/offline fallback ────────────────────────────────────────────

  private async sendViaDemo(msg: string): Promise<void> {
    // Simulate network delay
    await new Promise((r) => setTimeout(r, 600));

    const components = resolveDemoComponents(msg);
    const replyText = components
      ? "Here's what I found:"
      : `I searched the catalog for: "${msg}" — the backend isn't connected yet. Try "add a product", "search electronics", or "compare widgets".`;

    this.messages.update((msgs) => [
      ...msgs,
      {
        id: crypto.randomUUID(),
        role: 'assistant',
        content: replyText,
        timestamp: new Date(),
        uiComponents: components,
      },
    ]);
    this.isLoading.set(false);
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
        { label: 'Add another', prompt: 'Add a new product' },
        { label: 'View catalog', prompt: 'Show all products' },
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
}
