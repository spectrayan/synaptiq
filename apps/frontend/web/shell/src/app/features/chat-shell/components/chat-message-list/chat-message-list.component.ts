import { Component, input, output, viewChild, ElementRef, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { DslRendererComponent, FormSubmitEvent } from '@synaptiq/dsl-renderer';
import { MarkdownPipe } from '../../../../core/markdown.pipe';
import { ChatMessage } from '../../chat-message.model';
import { AdminConfigPanelComponent, ConfigFieldSaveEvent } from '../admin-config-panel/admin-config-panel.component';

@Component({
  selector: 'sq-chat-message-list',
  standalone: true,
  imports: [CommonModule, MatIconModule, DslRendererComponent, AdminConfigPanelComponent, MarkdownPipe],
  templateUrl: './chat-message-list.component.html',
  styleUrl: './chat-message-list.component.scss',
})
export class ChatMessageListComponent {
  readonly messages = input<ChatMessage[]>([]);

  // ── Auth form inputs (for inline auth rendering) ──
  readonly authMode = input<'signin' | 'signup'>('signin');
  readonly authEmail = input('');
  readonly authPassword = input('');
  readonly authLoading = input(false);
  readonly authError = input<string | null>(null);

  // ── Outputs ──
  readonly suggestionClicked = output<string>();
  readonly formSubmitted = output<{ event: FormSubmitEvent; messageId: string }>();
  readonly actionClicked = output<{ action: string; item_id?: string }>();
  readonly configFieldSave = output<ConfigFieldSaveEvent>();
  readonly triggerLogoUpload = output<string>();

  // Auth form outputs
  readonly authEmailChange = output<string>();
  readonly authPasswordChange = output<string>();
  readonly authSubmit = output<void>();
  readonly authGoogleSignIn = output<void>();
  readonly authToggleMode = output<void>();

  private messagesEnd = viewChild<ElementRef<HTMLDivElement>>('messagesEnd');

  constructor() {
    // Auto-scroll to bottom on new messages
    effect(() => {
      this.messages();
      setTimeout(() => this.messagesEnd()?.nativeElement?.scrollIntoView({ behavior: 'smooth' }), 50);
    });
  }

  trackById(_: number, msg: ChatMessage): string {
    return msg.id;
  }

  onSuggestionClicked(prompt: string): void {
    this.suggestionClicked.emit(prompt);
  }

  onFormSubmitted(event: FormSubmitEvent, messageId: string): void {
    this.formSubmitted.emit({ event, messageId });
  }

  onActionClicked(event: { action: string; item_id?: string }): void {
    this.actionClicked.emit(event);
  }

  onConfigFieldSave(event: ConfigFieldSaveEvent): void {
    this.configFieldSave.emit(event);
  }

  onTriggerLogoUpload(messageId: string): void {
    this.triggerLogoUpload.emit(messageId);
  }
}
