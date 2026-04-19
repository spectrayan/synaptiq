import { Component, signal, inject, ElementRef, viewChild, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { toSignal } from '@angular/core/rxjs-interop';
import { map } from 'rxjs';

export interface ChatMessage {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: Date;
  uiComponents?: unknown[];
}

@Component({
  selector: 'sq-chat-shell',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, MatButtonModule, MatTooltipModule],
  templateUrl: './chat-shell.component.html',
  styleUrl: './chat-shell.component.scss',
})
export class ChatShellComponent {
  private breakpoints = inject(BreakpointObserver);
  private messagesEnd = viewChild<ElementRef<HTMLDivElement>>('messagesEnd');

  isMobile = toSignal(
    this.breakpoints.observe([Breakpoints.XSmall, Breakpoints.Small]).pipe(map((r) => r.matches)),
    { initialValue: false },
  );

  sidebarOpen = signal(true);
  inputValue = signal('');
  isLoading = signal(false);

  messages = signal<ChatMessage[]>([
    {
      id: '1',
      role: 'assistant',
      content: "Hi! I'm Synaptiq. Ask me anything about your product catalog — I can search, compare, and analyse items for you.",
      timestamp: new Date(),
    },
  ]);

  constructor() {
    // Auto-scroll to bottom on new messages
    effect(() => {
      this.messages();
      setTimeout(() => this.messagesEnd()?.nativeElement.scrollIntoView({ behavior: 'smooth' }), 50);
    });
  }

  toggleSidebar(): void {
    this.sidebarOpen.update((v) => !v);
  }

  async sendMessage(): Promise<void> {
    const text = this.inputValue().trim();
    if (!text || this.isLoading()) return;

    this.messages.update((msgs) => [
      ...msgs,
      { id: crypto.randomUUID(), role: 'user', content: text, timestamp: new Date() },
    ]);
    this.inputValue.set('');
    this.isLoading.set(true);

    // TODO: wire to ChatService → API SSE stream
    await new Promise((r) => setTimeout(r, 900));
    this.messages.update((msgs) => [
      ...msgs,
      {
        id: crypto.randomUUID(),
        role: 'assistant',
        content: `Searching catalog for: "${text}"…`,
        timestamp: new Date(),
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
}
