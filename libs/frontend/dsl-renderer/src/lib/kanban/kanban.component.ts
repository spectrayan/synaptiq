import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { KanbanSpec } from '@synaptiq/constants';

@Component({
  selector: 'syn-kanban',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatChipsModule],
  template: `
    <div class="kanban-board">
      @if (spec().title) {
        <h3 class="kanban-title">{{ spec().title }}</h3>
      }
      <div class="kanban-columns">
        @for (col of spec().columns; track col.column_id; let ci = $index) {
          <div class="kanban-column" [class]="'status-' + (col.status || 'default')"
               [style.animation-delay.ms]="ci * 100">
            <div class="column-header">
              <span class="column-title">{{ col.title }}</span>
              <span class="column-count">{{ col.cards.length }}</span>
              @if (col.limit) {
                <span class="column-limit">/ {{ col.limit }}</span>
              }
            </div>
            <div class="column-cards">
              @for (card of col.cards; track card.card_id; let ki = $index) {
                <div class="kanban-card" [style.animation-delay.ms]="ci * 100 + ki * 60"
                     (click)="cardClicked.emit(card.card_id)">
                  <div class="card-title">{{ card.title }}</div>
                  @if (card.description) {
                    <div class="card-desc">{{ card.description }}</div>
                  }
                  <div class="card-meta">
                    @if (card.priority) {
                      <span class="priority-badge" [class]="'priority-' + card.priority">
                        {{ card.priority }}
                      </span>
                    }
                    @if (card.assignee) {
                      <span class="card-assignee">
                        <mat-icon class="assignee-icon">person</mat-icon>
                        {{ card.assignee }}
                      </span>
                    }
                    @if (card.due_date) {
                      <span class="card-due">
                        <mat-icon class="due-icon">schedule</mat-icon>
                        {{ card.due_date }}
                      </span>
                    }
                  </div>
                  @if (card.tags?.length) {
                    <div class="card-tags">
                      @for (tag of card.tags; track tag) {
                        <span class="card-tag">{{ tag }}</span>
                      }
                    </div>
                  }
                </div>
              }
            </div>
          </div>
        }
      </div>
    </div>
  `,
  styleUrl: './kanban.component.scss',
})
export class KanbanComponent {
  readonly spec = input.required<KanbanSpec>();
  readonly cardClicked = output<string>();
}
