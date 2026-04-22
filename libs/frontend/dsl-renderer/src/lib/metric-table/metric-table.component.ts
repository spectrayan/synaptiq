import { Component, computed, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MetricTableSpec, MetricTableColumn } from '@synaptiq/constants';

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
            @for (row of sortedRows(); track $index; let i = $index) {
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
    </div>
  `,
  styleUrl: './metric-table.component.scss',
})
export class MetricTableComponent {
  readonly spec = input.required<MetricTableSpec>();
  readonly exportClicked = output<void>();

  readonly sortField = signal<string>('');
  readonly sortDir = signal<'asc' | 'desc'>('asc');

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

  toggleSort(field: string) {
    if (this.sortField() === field) {
      this.sortDir.set(this.sortDir() === 'asc' ? 'desc' : 'asc');
    } else {
      this.sortField.set(field);
      this.sortDir.set('asc');
    }
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
