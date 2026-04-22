import { Component, computed, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { KPICardSpec } from '@synaptiq/constants';

@Component({
  selector: 'syn-kpi-card',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  template: `
    <div class="kpi-card" [class.has-sparkline]="spec().sparkline?.length">
      <div class="kpi-header">
        @if (spec().icon) {
          <mat-icon class="kpi-icon">{{ spec().icon }}</mat-icon>
        }
        <span class="kpi-label">{{ spec().label }}</span>
      </div>
      <div class="kpi-value">{{ spec().value }}</div>
      <div class="kpi-footer">
        @if (spec().trend) {
          <span class="kpi-trend" [class]="'trend-' + spec().trend">
            <mat-icon class="trend-icon">
              {{ spec().trend === 'up' ? 'trending_up' : spec().trend === 'down' ? 'trending_down' : 'trending_flat' }}
            </mat-icon>
            @if (spec().trend_value) {
              <span class="trend-value">{{ spec().trend_value }}</span>
            }
          </span>
        }
        @if (spec().period) {
          <span class="kpi-period">{{ spec().period }}</span>
        }
      </div>
      @if (spec().sparkline?.length) {
        <div class="kpi-sparkline">
          <svg [attr.viewBox]="sparklineViewBox()" preserveAspectRatio="none">
            <polyline [attr.points]="sparklinePoints()" fill="none" stroke="currentColor" stroke-width="1.5" />
          </svg>
        </div>
      }
    </div>
  `,
  styleUrl: './kpi-card.component.scss',
})
export class KPICardComponent {
  readonly spec = input.required<KPICardSpec>();

  readonly sparklineViewBox = computed(() => {
    const pts = this.spec().sparkline || [];
    return `0 0 ${pts.length - 1} ${Math.max(...pts) - Math.min(...pts) || 1}`;
  });

  readonly sparklinePoints = computed(() => {
    const pts = this.spec().sparkline || [];
    const min = Math.min(...pts);
    const max = Math.max(...pts);
    const range = max - min || 1;
    return pts.map((v, i) => `${i},${range - (v - min)}`).join(' ');
  });
}
