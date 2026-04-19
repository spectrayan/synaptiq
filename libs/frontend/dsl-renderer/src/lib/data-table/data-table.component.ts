/**
 * DataTableComponent — Material table with selection + actions (T7C — REQ-6.10)
 *
 * Uses mat-table for proper accessibility and column management.
 * Supports row selection, per-row actions, and AI suggestion chips.
 *
 * Angular signals API: input() / output() / computed()
 */
import { Component, computed, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SelectionModel } from '@angular/cdk/collections';
import { MatTableModule } from '@angular/material/table';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { DataTableSpec } from '@synaptiq/constants';
import { SuggestionBarComponent } from '../suggestion-bar/suggestion-bar.component';

@Component({
  selector: 'syn-data-table',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatCheckboxModule,
    MatButtonModule,
    MatIconModule,
    SuggestionBarComponent,
  ],
  templateUrl: './data-table.component.html',
  styleUrl: './data-table.component.scss',
})
export class DataTableComponent {
  readonly spec = input.required<DataTableSpec>();
  readonly actionClicked = output<{ action: string; item_id?: string }>();
  readonly suggestionClicked = output<string>();

  readonly selection = new SelectionModel<number>(true, []);

  readonly displayedColumns = computed(() => {
    const s = this.spec();
    const cols: string[] = [];
    if (s.selectable) cols.push('select');
    cols.push(...s.columns.map(c => c.field));
    if (s.actions?.length) cols.push('actions');
    return cols;
  });

  isAllSelected(): boolean {
    return this.selection.selected.length === this.spec().rows.length;
  }

  toggleAllRows() {
    if (this.isAllSelected()) {
      this.selection.clear();
    } else {
      this.spec().rows.forEach((_, i) => this.selection.select(i));
    }
  }

  onAction(actionId: string, row: Record<string, unknown>) {
    this.actionClicked.emit({
      action: actionId,
      item_id: (row['item_id'] || row['_id'] || '') as string,
    });
  }

  onSuggestion(prompt: string) {
    this.suggestionClicked.emit(prompt);
  }

  formatCell(value: unknown, type?: string): string {
    if (value == null) return '—';
    if (type === 'currency') return `$${value}`;
    if (Array.isArray(value)) return value.join(', ');
    return String(value);
  }
}
