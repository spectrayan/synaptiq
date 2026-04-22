import { Component, input, output, signal, forwardRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTabsModule } from '@angular/material/tabs';
import { ViewSpec, ComponentSpec } from '@synaptiq/constants';
import { DslRendererComponent } from '../dsl-renderer/dsl-renderer';
import { FormSubmitEvent } from '../form-input/form-input.component';

@Component({
  selector: 'syn-view',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatButtonModule, MatTabsModule, forwardRef(() => DslRendererComponent)],
  template: `
    <div class="view-container" [class]="'layout-' + spec().layout">
      <div class="view-header">
        @if (spec().icon) {
          <mat-icon class="view-icon">{{ spec().icon }}</mat-icon>
        }
        <h3 class="view-title">{{ spec().title }}</h3>
        <div class="view-actions">
          @if (spec().pinned !== undefined) {
            <button mat-icon-button class="pin-btn" (click)="pinToggled.emit(spec().view_id)">
              <mat-icon>{{ spec().pinned ? 'push_pin' : 'push_pin' }}</mat-icon>
            </button>
          }
          <button mat-icon-button class="collapse-btn" (click)="collapsed.set(!collapsed())">
            <mat-icon>{{ collapsed() ? 'expand_more' : 'expand_less' }}</mat-icon>
          </button>
        </div>
      </div>

      @if (!collapsed()) {
        @switch (spec().layout) {
          @case ('tabs') {
            <mat-tab-group class="view-tabs" animationDuration="200ms"
                           (selectedIndexChange)="activeTab.set($event)">
              @for (child of spec().children; track $index; let i = $index) {
                <mat-tab [label]="tabLabel(i)">
                  <div class="tab-content">
                    <syn-dsl-renderer [spec]="child"
                      (actionClicked)="actionClicked.emit($event)"
                      (itemClicked)="itemClicked.emit($event)"
                      (suggestionClicked)="suggestionClicked.emit($event)"
                      (formSubmitted)="formSubmitted.emit($event)"
                    />
                  </div>
                </mat-tab>
              }
            </mat-tab-group>
          }
          @case ('columns') {
            <div class="view-columns" [style.grid-template-columns]="columnWidths()">
              @for (child of spec().children; track $index) {
                <div class="view-column">
                  <syn-dsl-renderer [spec]="child"
                    (actionClicked)="actionClicked.emit($event)"
                    (itemClicked)="itemClicked.emit($event)"
                    (suggestionClicked)="suggestionClicked.emit($event)"
                    (formSubmitted)="formSubmitted.emit($event)"
                  />
                </div>
              }
            </div>
          }
          @case ('grid') {
            <div class="view-grid" [style.--grid-cols]="spec().layout_config?.columns || 2">
              @for (child of spec().children; track $index) {
                <div class="view-grid-item">
                  <syn-dsl-renderer [spec]="child"
                    (actionClicked)="actionClicked.emit($event)"
                    (itemClicked)="itemClicked.emit($event)"
                    (suggestionClicked)="suggestionClicked.emit($event)"
                    (formSubmitted)="formSubmitted.emit($event)"
                  />
                </div>
              }
            </div>
          }
          @case ('sidebar') {
            <div class="view-sidebar-layout"
                 [style.--sidebar-w]="spec().layout_config?.sidebar_width || '280px'">
              @if (spec().children.length > 0) {
                <div class="view-sidebar">
                  <syn-dsl-renderer [spec]="spec().children[0]"
                    (actionClicked)="actionClicked.emit($event)"
                    (itemClicked)="itemClicked.emit($event)"
                    (suggestionClicked)="suggestionClicked.emit($event)"
                    (formSubmitted)="formSubmitted.emit($event)"
                  />
                </div>
              }
              <div class="view-main">
                @for (child of spec().children.slice(1); track $index) {
                  <syn-dsl-renderer [spec]="child"
                    (actionClicked)="actionClicked.emit($event)"
                    (itemClicked)="itemClicked.emit($event)"
                    (suggestionClicked)="suggestionClicked.emit($event)"
                    (formSubmitted)="formSubmitted.emit($event)"
                  />
                }
              </div>
            </div>
          }
          @default {
            <!-- stack layout (default) -->
            <div class="view-stack" [style.gap]="spec().layout_config?.gap || '16px'">
              @for (child of spec().children; track $index) {
                <syn-dsl-renderer [spec]="child"
                  (actionClicked)="actionClicked.emit($event)"
                  (itemClicked)="itemClicked.emit($event)"
                  (suggestionClicked)="suggestionClicked.emit($event)"
                  (formSubmitted)="formSubmitted.emit($event)"
                />
              }
            </div>
          }
        }
      }
    </div>
  `,
  styleUrl: './view-renderer.component.scss',
})
export class ViewRendererComponent {
  readonly spec = input.required<ViewSpec>();

  readonly actionClicked = output<{ action: string; item_id?: string }>();
  readonly itemClicked = output<string>();
  readonly suggestionClicked = output<string>();
  readonly formSubmitted = output<FormSubmitEvent>();
  readonly pinToggled = output<string>();

  readonly collapsed = signal(false);
  readonly activeTab = signal(0);

  columnWidths(): string {
    const widths = this.spec().layout_config?.column_widths;
    if (widths?.length) return widths.join(' ');
    const count = this.spec().children.length;
    return `repeat(${count}, 1fr)`;
  }

  tabLabel(index: number): string {
    const labels = this.spec().layout_config?.tab_labels;
    return labels?.[index] || `Tab ${index + 1}`;
  }
}
