import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { ProgressTrackerSpec } from '@synaptiq/constants';

@Component({
  selector: 'syn-progress-tracker',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  template: `
    <div class="progress-tracker" [class]="'orientation-' + (spec().orientation || 'horizontal')">
      @if (spec().title) {
        <h3 class="tracker-title">{{ spec().title }}</h3>
      }
      <div class="steps-container">
        @for (step of spec().steps; track step.step_id; let i = $index; let last = $last) {
          <div class="step" [class]="'step-' + step.status"
               [style.animation-delay.ms]="i * 120">
            <div class="step-indicator">
              <div class="step-circle">
                @switch (step.status) {
                  @case ('completed') { <mat-icon class="step-icon">check</mat-icon> }
                  @case ('active') { <div class="step-pulse"></div> }
                  @case ('failed') { <mat-icon class="step-icon">close</mat-icon> }
                  @case ('skipped') { <mat-icon class="step-icon">skip_next</mat-icon> }
                  @default { <span class="step-num">{{ i + 1 }}</span> }
                }
              </div>
              @if (!last) {
                <div class="step-connector" [class.filled]="step.status === 'completed'"></div>
              }
            </div>
            <div class="step-content">
              <span class="step-label">{{ step.label }}</span>
              @if (step.description) {
                <span class="step-desc">{{ step.description }}</span>
              }
              @if (step.assignee) {
                <span class="step-assignee">
                  <mat-icon class="assignee-icon">person</mat-icon>
                  {{ step.assignee }}
                </span>
              }
            </div>
          </div>
        }
      </div>
    </div>
  `,
  styleUrl: './progress-tracker.component.scss',
})
export class ProgressTrackerComponent {
  readonly spec = input.required<ProgressTrackerSpec>();
}
