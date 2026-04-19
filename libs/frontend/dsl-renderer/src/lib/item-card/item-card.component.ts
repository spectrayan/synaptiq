/**
 * ItemCardComponent — Material card for catalog items (T7.5 / T7C — REQ-6.1)
 *
 * Uses mat-card as the base, with contextual AI suggestion chips.
 * Variants: standard, compact, featured
 *
 * Angular signals API: input() / output() / computed()
 */
import { Component, computed, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { ItemCardSpec } from '@synaptiq/constants';
import { SuggestionBarComponent } from '../suggestion-bar/suggestion-bar.component';

@Component({
  selector: 'syn-item-card',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatChipsModule,
    MatIconModule,
    SuggestionBarComponent,
  ],
  templateUrl: './item-card.component.html',
  styleUrl: './item-card.component.scss',
})
export class ItemCardComponent {
  readonly spec = input.required<ItemCardSpec>();
  readonly actionClicked = output<{ action: string; item_id?: string }>();
  readonly suggestionClicked = output<string>();
  readonly itemClicked = output<string>();

  readonly title = computed(() => this._findDesignator('primary_label') || this._findFirst('string') || '');
  readonly imageUrl = computed(() => this._findDesignator('image') || '');
  readonly price = computed(() => {
    const v = this._findDesignator('price');
    return v ? `$${v}` : '';
  });
  readonly description = computed(() => (this.spec().item?.data?.['description'] as string) || '');
  readonly badges = computed(() => {
    const tags = this.spec().item?.data?.['tags'] || this.spec().item?.data?.['category'];
    if (Array.isArray(tags)) return tags.map(String).slice(0, 3);
    if (tags) return [String(tags)];
    return [];
  });

  onAction(action: string, event?: Event) {
    if (event) {
      event.stopPropagation();
    }
    this.actionClicked.emit({ action, item_id: this.spec().item?.item_id });
  }

  onSuggestion(prompt: string) {
    this.suggestionClicked.emit(prompt);
  }

  onCardClick() {
    if (this.spec().clickable && this.spec().item?.item_id) {
      this.itemClicked.emit(this.spec().item.item_id);
    }
  }

  private _findDesignator(designator: string): string {
    const data = this.spec().item?.data || {};
    const map: Record<string, string[]> = {
      primary_label: ['name', 'title', 'product_name', 'label'],
      image: ['image', 'image_url', 'thumbnail', 'photo', 'img'],
      price: ['price', 'cost', 'amount', 'msrp'],
    };
    for (const key of map[designator] || []) {
      if (data[key] != null && data[key] !== '') return String(data[key]);
    }
    return '';
  }

  private _findFirst(type: string): string {
    const data = this.spec().item?.data || {};
    for (const val of Object.values(data)) {
      if (typeof val === type) return String(val);
    }
    return '';
  }
}
