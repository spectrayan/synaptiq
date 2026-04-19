/**
 * InfoBannerComponent — status/info banner (T7.13 — REQ-6.9)
 *
 * Angular signals API: input() / output() / computed()
 */
import { Component, computed, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { InfoBannerSpec } from '@synaptiq/constants';
import { SuggestionBarComponent } from '../suggestion-bar/suggestion-bar.component';

@Component({
  selector: 'syn-info-banner',
  standalone: true,
  imports: [CommonModule, MatIconModule, SuggestionBarComponent],
  templateUrl: './info-banner.component.html',
  styleUrl: './info-banner.component.scss',
})
export class InfoBannerComponent {
  readonly spec = input.required<InfoBannerSpec>();
  readonly suggestionClicked = output<string>();

  readonly icon = computed(() => {
    const icons: Record<string, string> = {
      info: 'info',
      success: 'check_circle',
      warning: 'warning',
      error: 'error',
    };
    return icons[this.spec().style || 'info'] || 'info';
  });

  onSuggestion(prompt: string) {
    this.suggestionClicked.emit(prompt);
  }
}
