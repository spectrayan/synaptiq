/**
 * DataTableComponent — Material table with selection, actions, and pagination (T7C — REQ-6.10)
 *
 * Uses mat-table for proper accessibility and column management.
 * Supports row selection, per-row actions, AI suggestion chips,
 * and client-side pagination for large datasets.
 *
 * Angular signals API: input() / output() / computed() / signal()
 */
import { Component, computed, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SelectionModel } from '@angular/cdk/collections';
import { MatTableModule } from '@angular/material/table';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { DataTableSpec } from '@synaptiq/constants';
import { SuggestionBarComponent } from '../suggestion-bar/suggestion-bar.component';

/** Default number of rows per page */
const PAGE_SIZE = 10;

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

  /** Current page index (0-based) */
  readonly currentPage = signal(0);
  /** Page size */
  readonly pageSize = PAGE_SIZE;

  /** Total number of rows */
  readonly totalRows = computed(() => this.spec().rows?.length ?? 0);

  /** Total number of pages */
  readonly totalPages = computed(() => Math.max(1, Math.ceil(this.totalRows() / this.pageSize)));

  /** Whether pagination should be shown (more than one page of data) */
  readonly showPagination = computed(() => this.totalRows() > this.pageSize);

  /** Current page of rows for the table */
  readonly paginatedRows = computed(() => {
    const rows = this.spec().rows ?? [];
    const start = this.currentPage() * this.pageSize;
    return rows.slice(start, start + this.pageSize);
  });

  /** Display range text (e.g. "1–10 of 90") */
  readonly rangeLabel = computed(() => {
    const total = this.totalRows();
    if (total === 0) return '0 items';
    const start = this.currentPage() * this.pageSize + 1;
    const end = Math.min(start + this.pageSize - 1, total);
    return `${start}–${end} of ${total}`;
  });

  readonly displayedColumns = computed(() => {
    const s = this.spec();
    const cols: string[] = [];
    if (s.selectable) cols.push('select');
    cols.push(...s.columns.map(c => c.field));
    if (s.actions?.length) cols.push('actions');
    return cols;
  });

  isAllSelected(): boolean {
    return this.selection.selected.length === this.paginatedRows().length;
  }

  toggleAllRows() {
    if (this.isAllSelected()) {
      this.selection.clear();
    } else {
      this.paginatedRows().forEach((_, i) => this.selection.select(i));
    }
  }

  // ── Pagination controls ───────────────────────────────────────────

  goToPage(page: number): void {
    const clamped = Math.max(0, Math.min(page, this.totalPages() - 1));
    this.currentPage.set(clamped);
    this.selection.clear();
  }

  nextPage(): void {
    this.goToPage(this.currentPage() + 1);
  }

  prevPage(): void {
    this.goToPage(this.currentPage() - 1);
  }

  // ── Actions ───────────────────────────────────────────────────────

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
