/**
 * ComparisonTableComponent — side-by-side comparison (T7.8 / T7C — REQ-6.4)
 *
 * Angular signals API: input() / output() / computed()
 */
import { Component, computed, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { ComparisonTableSpec } from '@synaptiq/constants';
import { SuggestionBarComponent } from '../suggestion-bar/suggestion-bar.component';

@Component({
  selector: 'syn-comparison-table',
  standalone: true,
  imports: [CommonModule, MatTableModule, SuggestionBarComponent],
  templateUrl: './comparison-table.component.html',
  styleUrl: './comparison-table.component.scss',
})
export class ComparisonTableComponent {
  readonly spec = input.required<ComparisonTableSpec>();
  readonly suggestionClicked = output<string>();

  readonly comparisonFields = computed(() => {
    const s = this.spec();
    if (s.fields?.length) return s.fields;
    const allKeys = new Set<string>();
    for (const item of s.items || []) {
      Object.keys(item.data || {}).forEach(k => allKeys.add(k));
    }
    const skip = new Set(['image', 'image_url', 'thumbnail']);
    return [...allKeys].filter(k => !skip.has(k));
  });

  readonly displayedColumns = computed(() => {
    return ['field', ...this.spec().items.map(i => i.item_id)];
  });

  itemTitle(item: { data: Record<string, unknown> }): string {
    const d = item.data || {};
    return (d['name'] || d['title'] || d['product_name'] || 'Item') as string;
  }

  formatValue(value: unknown): string {
    if (value == null) return '—';
    if (Array.isArray(value)) return value.join(', ');
    return String(value);
  }

  onSuggestion(prompt: string) {
    this.suggestionClicked.emit(prompt);
  }
}
