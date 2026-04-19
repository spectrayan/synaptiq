/**
 * FilterSummaryComponent — active filters display (T7.9 / T7C — REQ-6.5)
 *
 * Angular signals API: input() / output()
 */
import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { FilterSummarySpec } from '@synaptiq/constants';
import { SuggestionBarComponent } from '../suggestion-bar/suggestion-bar.component';

@Component({
  selector: 'syn-filter-summary',
  standalone: true,
  imports: [CommonModule, MatChipsModule, MatIconModule, SuggestionBarComponent],
  templateUrl: './filter-summary.component.html',
  styleUrl: './filter-summary.component.scss',
})
export class FilterSummaryComponent {
  readonly spec = input.required<FilterSummarySpec>();
  readonly filterRemoved = output<string>();
  readonly suggestionClicked = output<string>();

  removeFilter(key: string) {
    this.filterRemoved.emit(key);
  }

  onSuggestion(prompt: string) {
    this.suggestionClicked.emit(prompt);
  }

  formatLabel(key: string): string {
    return key.replace(/_/g, ' ').replace(/\b\w/g, c => c.toUpperCase());
  }

  formatValue(val: unknown): string {
    if (Array.isArray(val)) return val.join(', ');
    return String(val);
  }
}
