/**
 * ActionConfirmComponent — confirmation dialog card (T7.12 — REQ-6.8)
 *
 * Angular signals API: input() / output()
 */
import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ActionConfirmSpec } from '@synaptiq/constants';
import { SuggestionBarComponent } from '../suggestion-bar/suggestion-bar.component';

@Component({
  selector: 'syn-action-confirm',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, MatIconModule, SuggestionBarComponent],
  templateUrl: './action-confirm.component.html',
  styleUrl: './action-confirm.component.scss',
})
export class ActionConfirmComponent {
  readonly spec = input.required<ActionConfirmSpec>();
  readonly confirmed = output<{ action: string; item_id?: string }>();
  readonly cancelled = output<void>();
  readonly suggestionClicked = output<string>();

  onConfirm() {
    this.confirmed.emit({ action: this.spec().action, item_id: this.spec().item_id });
  }

  onCancel() {
    this.cancelled.emit();
  }

  onSuggestion(prompt: string) {
    this.suggestionClicked.emit(prompt);
  }
}
