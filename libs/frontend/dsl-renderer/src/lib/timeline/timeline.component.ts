import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { TimelineSpec } from '@synaptiq/constants';

@Component({
  selector: 'syn-timeline',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  template: `
    <div class="timeline-container">
      @if (spec().title) {
        <h3 class="timeline-title">{{ spec().title }}</h3>
      }
      <div class="timeline">
        @for (entry of sortedEntries(); track entry.entry_id; let i = $index; let last = $last) {
          <div class="timeline-entry" [class]="'entry-type-' + (entry.entry_type || 'event')"
               [style.animation-delay.ms]="i * 80">
            <div class="entry-marker">
              <div class="marker-dot">
                <mat-icon class="marker-icon">
                  {{ entryIcon(entry.entry_type) }}
                </mat-icon>
              </div>
              @if (!last) {
                <div class="marker-line"></div>
              }
            </div>
            <div class="entry-content">
              <div class="entry-header">
                <span class="entry-title">{{ entry.title }}</span>
                <span class="entry-time">{{ entry.timestamp }}</span>
              </div>
              @if (entry.description) {
                <p class="entry-desc">{{ entry.description }}</p>
              }
              @if (entry.actor) {
                <span class="entry-actor">
                  <mat-icon class="actor-icon">person</mat-icon>
                  {{ entry.actor }}
                </span>
              }
            </div>
          </div>
        }
      </div>
    </div>
  `,
  styleUrl: './timeline.component.scss',
})
export class TimelineComponent {
  readonly spec = input.required<TimelineSpec>();

  readonly sortedEntries = () => {
    const entries = [...(this.spec().entries || [])];
    if (this.spec().ascending) {
      return entries; // already in chronological order
    }
    return entries.reverse();
  };

  entryIcon(type?: string): string {
    switch (type) {
      case 'milestone': return 'flag';
      case 'alert': return 'warning';
      case 'note': return 'sticky_note_2';
      default: return 'circle';
    }
  }
}
