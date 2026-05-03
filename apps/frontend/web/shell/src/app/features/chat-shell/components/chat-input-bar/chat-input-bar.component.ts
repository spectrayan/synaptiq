import { Component, input, output, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';

@Component({
  selector: 'sq-chat-input-bar',
  standalone: true,
  imports: [MatIconModule, MatButtonModule, MatTooltipModule],
  templateUrl: './chat-input-bar.component.html',
  styleUrl: './chat-input-bar.component.scss',
})
export class ChatInputBarComponent {
  readonly isLoading = input(false);
  readonly isAdminMode = input(false);

  readonly sendMessage = output<string>();

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
