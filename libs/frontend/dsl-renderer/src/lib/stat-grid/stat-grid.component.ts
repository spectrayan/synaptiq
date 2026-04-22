import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { StatGridSpec } from '@synaptiq/constants';

@Component({
  selector: 'syn-stat-grid',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  template: `
    <div class="stat-grid-container">
      @if (spec().title) {
        <h3 class="stat-grid-title">{{ spec().title }}</h3>
      }
      <div class="stat-grid" [style.--stat-cols]="spec().columns || spec().stats.length">
        @for (stat of spec().stats; track stat.label; let i = $index) {
          <div class="stat-item" [style.animation-delay.ms]="i * 80">
            <div class="stat-header">
              @if (stat.icon) {
                <mat-icon class="stat-icon">{{ stat.icon }}</mat-icon>
              }
              <span class="stat-label">{{ stat.label }}</span>
            </div>
            <div class="stat-value">{{ stat.value }}</div>
            @if (stat.trend) {
              <div class="stat-trend" [class]="'trend-' + stat.trend">
                <mat-icon class="trend-icon">
                  {{ stat.trend === 'up' ? 'trending_up' : stat.trend === 'down' ? 'trending_down' : 'trending_flat' }}
                </mat-icon>
                @if (stat.trend_value) {
                  <span>{{ stat.trend_value }}</span>
                }
                @if (stat.period) {
                  <span class="stat-period">{{ stat.period }}</span>
                }
              </div>
            }
          </div>
        }
      </div>
    </div>
  `,
  styleUrl: './stat-grid.component.scss',
})
export class StatGridComponent {
  readonly spec = input.required<StatGridSpec>();
}
