import { Component, computed, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MetricTableSpec, MetricTableColumn } from '@synaptiq/constants';

/** Default number of rows per page */
const PAGE_SIZE = 10;

@Component({
  selector: 'syn-metric-table',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatButtonModule],
  template: `
    <div class="metric-table-container">
      @if (spec().title) {
        <div class="table-header">
          <h3 class="table-title">{{ spec().title }}</h3>
          @if (spec().exportable) {
            <button mat-icon-button (click)="exportClicked.emit()">
              <mat-icon>download</mat-icon>
            </button>
          }
        </div>
      }
      <div class="table-scroll">
        <table class="metric-table">
          <thead>
            <tr>
              @for (col of spec().columns; track col.field) {
                <th [style.width]="col.width || 'auto'"
                    [class.sortable]="col.sortable"
                    (click)="col.sortable ? toggleSort(col.field) : null">
                  {{ col.label }}
                  @if (col.sortable) {
                    <mat-icon class="sort-icon" [class.active]="sortField() === col.field">
                      {{ sortField() === col.field ? (sortDir() === 'asc' ? 'arrow_upward' : 'arrow_downward') : 'unfold_more' }}
                    </mat-icon>
                  }
                </th>
              }
            </tr>
          </thead>
          <tbody>
            @for (row of paginatedRows(); track $index; let i = $index) {
              <tr [style.animation-delay.ms]="i * 30">
                @for (col of spec().columns; track col.field) {
                  <td [class]="'col-type-' + (col.type || 'text')">
                    @switch (col.type) {
                      @case ('currency') {
                        <span class="cell-currency">{{ col.currency_code || '$' }}{{ row[col.field] }}</span>
                      }
                      @case ('percentage') {
                        <span class="cell-percentage">{{ row[col.field] }}%</span>
                      }
                      @case ('badge') {
                        <span class="cell-badge">{{ row[col.field] }}</span>
                      }
                      @default {
                        {{ row[col.field] }}
                      }
                    }
                  </td>
                }
              </tr>
            }
          </tbody>
          @if (spec().show_aggregates) {
            <tfoot>
              <tr>
                @for (col of spec().columns; track col.field) {
                  <td class="aggregate-cell">
                    @if (col.aggregate) {
                      <span class="aggregate-value">{{ computeAggregate(col) }}</span>
                    }
                  </td>
                }
              </tr>
            </tfoot>
          }
        </table>
      </div>

      <!-- Pagination controls -->
      @if (showPagination()) {
        <div class="table-pagination">
          <span class="pagination-range">{{ rangeLabel() }}</span>
          <div class="pagination-buttons">
            <button mat-icon-button [disabled]="currentPage() === 0" (click)="goToPage(0)" aria-label="First page">
              <mat-icon>first_page</mat-icon>
            </button>
            <button mat-icon-button [disabled]="currentPage() === 0" (click)="prevPage()" aria-label="Previous page">
              <mat-icon>chevron_left</mat-icon>
            </button>
            <span class="pagination-page">Page {{ currentPage() + 1 }} of {{ totalPages() }}</span>
            <button mat-icon-button [disabled]="currentPage() >= totalPages() - 1" (click)="nextPage()" aria-label="Next page">
              <mat-icon>chevron_right</mat-icon>
            </button>
            <button mat-icon-button [disabled]="currentPage() >= totalPages() - 1" (click)="goToPage(totalPages() - 1)" aria-label="Last page">
              <mat-icon>last_page</mat-icon>
            </button>
          </div>
        </div>
      }
    </div>
  `,
  styleUrl: './metric-table.component.scss',
})
export class MetricTableComponent {
  readonly spec = input.required<MetricTableSpec>();
  readonly exportClicked = output<void>();

  readonly sortField = signal<string>('');
  readonly sortDir = signal<'asc' | 'desc'>('asc');

  /** Current page index (0-based) */
  readonly currentPage = signal(0);
  readonly pageSize = PAGE_SIZE;

  readonly sortedRows = computed(() => {
    const rows = [...(this.spec().rows || [])];
    const field = this.sortField();
    if (!field) return rows;
    const dir = this.sortDir() === 'asc' ? 1 : -1;
    return rows.sort((a, b) => {
      const va = a[field];
      const vb = b[field];
      if (typeof va === 'number' && typeof vb === 'number') return (va - vb) * dir;
      return String(va ?? '').localeCompare(String(vb ?? '')) * dir;
    });
  });

  /** Total number of rows (after sorting) */
  readonly totalRows = computed(() => this.sortedRows().length);

  /** Total number of pages */
  readonly totalPages = computed(() => Math.max(1, Math.ceil(this.totalRows() / this.pageSize)));

  /** Whether pagination should be shown (more than one page of data) */
  readonly showPagination = computed(() => this.totalRows() > this.pageSize);

  /** Current page of sorted rows */
  readonly paginatedRows = computed(() => {
    const rows = this.sortedRows();
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

  toggleSort(field: string) {
    if (this.sortField() === field) {
      this.sortDir.set(this.sortDir() === 'asc' ? 'desc' : 'asc');
    } else {
      this.sortField.set(field);
      this.sortDir.set('asc');
    }
    // Reset to first page when sort changes
    this.currentPage.set(0);
  }

  // ── Pagination controls ───────────────────────────────────────────

  goToPage(page: number): void {
    this.currentPage.set(Math.max(0, Math.min(page, this.totalPages() - 1)));
  }

  nextPage(): void {
    this.goToPage(this.currentPage() + 1);
  }

  prevPage(): void {
    this.goToPage(this.currentPage() - 1);
  }

  computeAggregate(col: MetricTableColumn): string {
    const rows = this.spec().rows || [];
    const values = rows.map(r => Number(r[col.field])).filter(v => !isNaN(v));
    if (!values.length) return '';

    switch (col.aggregate) {
      case 'sum': return this.formatNum(values.reduce((a, b) => a + b, 0));
      case 'avg': return this.formatNum(values.reduce((a, b) => a + b, 0) / values.length);
      case 'min': return this.formatNum(Math.min(...values));
      case 'max': return this.formatNum(Math.max(...values));
      case 'count': return String(values.length);
      default: return '';
    }
  }

  private formatNum(n: number): string {
    return n % 1 === 0 ? n.toLocaleString() : n.toFixed(2);
  }
}
