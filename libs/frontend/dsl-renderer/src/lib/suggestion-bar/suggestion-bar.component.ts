/**
 * SuggestionBarComponent — contextual AI suggestion chips (T7C.1)
 *
 * Renders AISuggestion[] as Material chip set below any DSL component.
 * When a chip is clicked, emits the `prompt` string for injection into
 * the chat input.
 *
 * Angular signals API: input() / output()
 */
import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { AISuggestion } from '@synaptiq/constants';

@Component({
  selector: 'syn-suggestion-bar',
  standalone: true,
  imports: [CommonModule, MatChipsModule, MatIconModule],
  templateUrl: './suggestion-bar.component.html',
  styleUrl: './suggestion-bar.component.scss',
})
export class SuggestionBarComponent {
  readonly suggestions = input<AISuggestion[]>([]);
  readonly suggestionClicked = output<string>();

  onChipClick(suggestion: AISuggestion) {
    this.suggestionClicked.emit(suggestion.prompt);
  }
}
