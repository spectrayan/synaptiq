import { Component, input, output } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';

@Component({
  selector: 'sq-chat-topbar',
  standalone: true,
  imports: [MatIconModule, MatButtonModule, MatTooltipModule],
  templateUrl: './chat-topbar.component.html',
  styleUrl: './chat-topbar.component.scss',
})
export class ChatTopbarComponent {
  readonly sidebarOpen = input(true);
  readonly isMobile = input(false);
  readonly isAdminMode = input(false);
  readonly isAuthenticated = input(false);
  readonly useBackend = input(true);
  readonly isDark = input(false);

  // Tab state
  readonly activeTab = input<'chat' | 'workflow'>('chat');
  readonly workflowGenerating = input(false);
  readonly hasActiveWorkflow = input(false);

  readonly toggleSidebar = output<void>();
  readonly openAuth = output<'signin' | 'signup'>();
  readonly toggleTheme = output<void>();
  readonly openSettings = output<void>();
  readonly tabChange = output<'chat' | 'workflow'>();
}
