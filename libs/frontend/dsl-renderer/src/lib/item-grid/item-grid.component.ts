/**
 * ItemGridComponent — responsive card grid (T7.6 / T7C — REQ-6.2)
 *
 * Renders CatalogItemData[] using ItemCardComponent in a CSS Grid layout.
 * Includes a shared SuggestionBar for grid-level AI suggestions.
 *
 * Angular signals API: input() / output()
 */
import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ItemCardComponent } from '../item-card/item-card.component';
import { SuggestionBarComponent } from '../suggestion-bar/suggestion-bar.component';
import { ItemGridSpec } from '@synaptiq/constants';

@Component({
  selector: 'syn-item-grid',
  standalone: true,
  imports: [CommonModule, ItemCardComponent, SuggestionBarComponent],
  templateUrl: './item-grid.component.html',
  styleUrl: './item-grid.component.scss',
})
export class ItemGridComponent {
  readonly spec = input.required<ItemGridSpec>();
  readonly itemClicked = output<string>();
  readonly suggestionClicked = output<string>();

  onItemAction(event: { action: string; item_id?: string }) {
    if (event.item_id) {
      // Actually, actionClicked shouldn't implicitly emit itemClicked, 
      // but maybe it was designed this way before. Let's keep it to not break existing usage,
      // but we also add onItemClick for the explicit card click.
      this.itemClicked.emit(event.item_id);
    }
  }

  onItemClick(item_id: string) {
    this.itemClicked.emit(item_id);
  }

  onSuggestion(prompt: string) {
    this.suggestionClicked.emit(prompt);
  }
}
