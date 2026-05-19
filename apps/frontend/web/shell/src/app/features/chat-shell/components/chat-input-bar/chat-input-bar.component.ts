import { Component, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { type KnowledgeCategoryResponse } from '@synaptiq/client';

@Component({
  selector: 'sq-chat-input-bar',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule],
  templateUrl: './chat-input-bar.component.html',
  styleUrl: './chat-input-bar.component.scss',
})
export class ChatInputBarComponent {
  readonly isLoading = input(false);
  readonly isAdminMode = input(false);

  /** Knowledge bases currently attached to the chat for RAG grounding. */
  readonly attachedKbs = input<KnowledgeCategoryResponse[]>([]);

  readonly sendMessage = output<string>();
  readonly detachKb = output<string>();

  /** Local input value managed internally. */
  readonly inputValue = signal('');

  onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.send();
    }
  }

  send(): void {
    const msg = this.inputValue().trim();
    if (!msg || this.isLoading()) return;
    this.sendMessage.emit(msg);
    this.inputValue.set('');
  }
}
