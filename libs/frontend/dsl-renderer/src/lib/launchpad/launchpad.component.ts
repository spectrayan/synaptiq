import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { LaunchpadSpec } from '@synaptiq/constants';

@Component({
  selector: 'syn-launchpad',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  template: `
    <div class="launchpad">
      @if (spec().greeting) {
        <p class="launchpad-greeting">{{ spec().greeting }}</p>
      }
      @if (spec().title) {
        <h2 class="launchpad-title">{{ spec().title }}</h2>
      }
      @for (section of spec().sections; track section.label) {
        <div class="launchpad-section">
          <h3 class="section-label">{{ section.label }}</h3>
          <div class="tiles-grid">
            @for (tile of section.tiles; track tile.tile_id; let i = $index) {
              <button class="tile" [style.animation-delay.ms]="i * 60"
                      [style.--tile-color]="tileColor(tile.color_index)"
                      (click)="tileClicked.emit(tile.prompt)">
                <div class="tile-icon-wrap">
                  <mat-icon class="tile-icon">{{ tile.icon || 'dashboard' }}</mat-icon>
                  @if (tile.badge) {
                    <span class="tile-badge">{{ tile.badge }}</span>
                  }
                </div>
                <div class="tile-content">
                  <span class="tile-title">{{ tile.title }}</span>
                  @if (tile.description) {
                    <span class="tile-desc">{{ tile.description }}</span>
                  }
                </div>
                @if (tile.admin_promoted) {
                  <mat-icon class="promoted-badge" matTooltip="Suggested by admin">star</mat-icon>
                }
              </button>
            }
          </div>
        </div>
      }
    </div>
  `,
  styleUrl: './launchpad.component.scss',
})
export class LaunchpadComponent {
  readonly spec = input.required<LaunchpadSpec>();
  readonly tileClicked = output<string>();

  /** Map color_index to theme chip token */
  tileColor(index?: number): string {
    const i = (index || 0) % 8 + 1;
    return `var(--sq-chip-${i}-bg)`;
  }
}
