import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';

@Component({
  selector: 'sq-chat-settings-drawer',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule],
  templateUrl: './chat-settings-drawer.component.html',
  styleUrl: './chat-settings-drawer.component.scss',
})
export class ChatSettingsDrawerComponent {
  readonly isOpen = input(false);
  readonly isDark = input(false);
  readonly useBackend = input(true);
  readonly apiBaseUrl = input('');
  readonly isAuthenticated = input(false);
  readonly userEmail = input<string | null>(null);

  readonly close = output<void>();
  readonly toggleTheme = output<void>();
  readonly signOut = output<void>();
  readonly openAuth = output<'signin' | 'signup'>();
  readonly clearConversations = output<void>();
}
