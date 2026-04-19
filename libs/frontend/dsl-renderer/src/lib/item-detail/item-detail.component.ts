/**
 * ItemDetailComponent — expanded single item view (T7.7 / T7C — REQ-6.3)
 *
 * Uses mat-card for the detail layout with field list and actions.
 *
 * Angular signals API: input() / output() / computed()
 */
import { Component, computed, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatListModule } from '@angular/material/list';
import { ItemDetailSpec } from '@synaptiq/constants';
import { SuggestionBarComponent } from '../suggestion-bar/suggestion-bar.component';

@Component({
  selector: 'syn-item-detail',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, MatListModule, SuggestionBarComponent],
  templateUrl: './item-detail.component.html',
  styleUrl: './item-detail.component.scss',
})
export class ItemDetailComponent {
  readonly spec = input.required<ItemDetailSpec>();
  readonly actionClicked = output<{ action: string; item_id?: string }>();
  readonly suggestionClicked = output<string>();

  readonly displayFields = computed(() => {
    const s = this.spec();
    const data = s.item?.data || {};
    const fields = s.fields?.length
      ? s.fields
      : Object.keys(data).filter(k => !['image', 'image_url', 'thumbnail'].includes(k));
    return fields.map(k => ({ key: k, value: data[k] }));
  });

  readonly imageUrl = computed(() => {
    const data = this.spec().item?.data || {};
    return (data['image'] || data['image_url'] || data['thumbnail'] || '') as string;
  });

  readonly title = computed(() => {
    const data = this.spec().item?.data || {};
    return (data['name'] || data['title'] || data['product_name'] || 'Item') as string;
  });

  formatValue(value: unknown): string {
    if (value == null) return '—';
    if (Array.isArray(value)) return value.join(', ');
    return String(value);
  }

  formatLabel(key: string): string {
    return key.replace(/_/g, ' ').replace(/\b\w/g, c => c.toUpperCase());
  }

  onAction(action: string) {
    this.actionClicked.emit({ action, item_id: this.spec().item?.item_id });
  }

  onSuggestion(prompt: string) {
    this.suggestionClicked.emit(prompt);
  }
}
