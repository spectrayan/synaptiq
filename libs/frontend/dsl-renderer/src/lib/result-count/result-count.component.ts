/**
 * ResultCountComponent — query result count display (T7.10 — REQ-6.6)
 *
 * Angular signals API: input() / output()
 */
import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ResultCountSpec } from '@synaptiq/constants';
import { SuggestionBarComponent } from '../suggestion-bar/suggestion-bar.component';

@Component({
  selector: 'syn-result-count',
  standalone: true,
  imports: [CommonModule, SuggestionBarComponent],
  templateUrl: './result-count.component.html',
  styleUrl: './result-count.component.scss',
})
export class ResultCountComponent {
  readonly spec = input.required<ResultCountSpec>();
  readonly suggestionClicked = output<string>();

  onSuggestion(prompt: string) {
    this.suggestionClicked.emit(prompt);
  }
}
