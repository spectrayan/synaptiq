/**
 * EmptyStateComponent — no-results placeholder (T7.11 — REQ-6.7)
 *
 * Angular signals API: input() / output()
 */
import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { EmptyStateSpec } from '@synaptiq/constants';
import { SuggestionBarComponent } from '../suggestion-bar/suggestion-bar.component';

@Component({
  selector: 'syn-empty-state',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatButtonModule, SuggestionBarComponent],
  templateUrl: './empty-state.component.html',
  styleUrl: './empty-state.component.scss',
})
export class EmptyStateComponent {
  readonly spec = input.required<EmptyStateSpec>();
  readonly actionClicked = output<string>();
  readonly suggestionClicked = output<string>();

  onAction(action: string) {
    this.actionClicked.emit(action);
  }

  onSuggestion(prompt: string) {
    this.suggestionClicked.emit(prompt);
  }
}
