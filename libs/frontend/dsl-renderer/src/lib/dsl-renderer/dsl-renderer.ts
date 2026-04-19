/**
 * DslRendererComponent — main dispatcher (T7.4 — REQ-AI11)
 *
 * Takes a ComponentSpec and renders the appropriate child component
 * via a switch on the `type` discriminator field.
 *
 * Angular signals API: input() / output()
 */
import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ComponentSpec } from '@synaptiq/constants';

import { ItemCardComponent } from '../item-card/item-card.component';
import { ItemGridComponent } from '../item-grid/item-grid.component';
import { ItemDetailComponent } from '../item-detail/item-detail.component';
import { ComparisonTableComponent } from '../comparison-table/comparison-table.component';
import { FilterSummaryComponent } from '../filter-summary/filter-summary.component';
import { ResultCountComponent } from '../result-count/result-count.component';
import { EmptyStateComponent } from '../empty-state/empty-state.component';
import { ActionConfirmComponent } from '../action-confirm/action-confirm.component';
import { InfoBannerComponent } from '../info-banner/info-banner.component';
import { DataTableComponent } from '../data-table/data-table.component';
import { FormInputComponent, FormSubmitEvent } from '../form-input/form-input.component';

@Component({
  selector: 'syn-dsl-renderer',
  standalone: true,
  imports: [
    CommonModule,
    ItemCardComponent,
    ItemGridComponent,
    ItemDetailComponent,
    ComparisonTableComponent,
    FilterSummaryComponent,
    ResultCountComponent,
    EmptyStateComponent,
    ActionConfirmComponent,
    InfoBannerComponent,
    DataTableComponent,
    FormInputComponent,
  ],
  templateUrl: './dsl-renderer.html',
  styleUrl: './dsl-renderer.scss',
})
export class DslRendererComponent {
  readonly spec = input.required<ComponentSpec>();

  readonly actionClicked = output<{ action: string; item_id?: string }>();
  readonly itemClicked = output<string>();
  readonly filterRemoved = output<string>();
  readonly suggestionClicked = output<string>();
  readonly formSubmitted = output<FormSubmitEvent>();

  onAction(event: { action: string; item_id?: string }) {
    this.actionClicked.emit(event);
  }

  onItemClick(itemId: string) {
    this.itemClicked.emit(itemId);
  }

  onFilterRemove(field: string) {
    this.filterRemoved.emit(field);
  }

  onSuggestion(suggestion: string) {
    this.suggestionClicked.emit(suggestion);
  }

  onConfirm(event: { action: string; item_id?: string }) {
    this.actionClicked.emit(event);
  }

  onCancel() {
    // No-op — action cancelled
  }

  onFormSubmit(event: FormSubmitEvent) {
    this.formSubmitted.emit(event);
  }
}
