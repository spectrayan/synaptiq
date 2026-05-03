/**
 * PromptEditorComponent — Dual-pane system prompt editor with live markdown preview.
 *
 * Features:
 *   - Edit pane (left) with monospace textarea
 *   - Live markdown preview (right) using MarkdownViewerComponent
 *   - Template variable highlighting ({{variable}})
 *   - Approximate token count
 *   - Full-screen toggle
 *   - Copy to clipboard
 */
import {
  Component,
  input,
  output,
  signal,
  computed,
  ChangeDetectionStrategy,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MarkdownViewerComponent } from './markdown-viewer.component';

@Component({
  selector: 'sq-prompt-editor',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, MatTooltipModule, MarkdownViewerComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="prompt-editor" [class.prompt-editor--fullscreen]="isFullscreen()">
      <!-- Header -->
      <div class="prompt-editor-header">
        <div class="prompt-editor-title">
          <mat-icon>edit_note</mat-icon>
          <span>{{ label() }}</span>
        </div>
        <div class="prompt-editor-meta">
          <span class="prompt-meta-item" matTooltip="Character count">
            {{ content().length }} chars
          </span>
          <span class="prompt-meta-item" matTooltip="Approximate token count (~4 chars/token)">
            ~{{ tokenEstimate() }} tokens
          </span>
          @if (templateVars().length) {
            <span class="prompt-meta-item prompt-meta-vars" matTooltip="Template variables detected">
              {{ templateVars().length }} vars
            </span>
          }
        </div>
        <div class="prompt-editor-actions">
          <button class="prompt-action-btn" (click)="copyToClipboard()" matTooltip="Copy prompt">
            <mat-icon>{{ copySuccess() ? 'check' : 'content_copy' }}</mat-icon>
          </button>
          <button class="prompt-action-btn" (click)="isFullscreen.update(v => !v)"
                  [matTooltip]="isFullscreen() ? 'Exit fullscreen' : 'Fullscreen'">
            <mat-icon>{{ isFullscreen() ? 'fullscreen_exit' : 'fullscreen' }}</mat-icon>
          </button>
          @if (isFullscreen()) {
            <button class="prompt-action-btn prompt-action-close" (click)="isFullscreen.set(false)">
              <mat-icon>close</mat-icon>
            </button>
          }
        </div>
      </div>

      <!-- Dual Pane -->
      <div class="prompt-editor-body">
        <!-- Edit pane -->
        <div class="prompt-pane prompt-pane-edit">
          <div class="prompt-pane-label">Edit</div>
          <textarea
            class="prompt-textarea"
            [ngModel]="content()"
            (ngModelChange)="onContentChange($event)"
            placeholder="Enter system prompt…"
            spellcheck="false"
          ></textarea>
        </div>

        <!-- Preview pane -->
        <div class="prompt-pane prompt-pane-preview">
          <div class="prompt-pane-label">Preview</div>
          <div class="prompt-preview-content">
            @if (content().trim()) {
              <sq-markdown-viewer [content]="content()" />
            } @else {
              <div class="prompt-preview-empty">
                <mat-icon>visibility</mat-icon>
                <p>Start typing to see a live preview</p>
              </div>
            }
          </div>
        </div>
      </div>

      <!-- Template variables bar -->
      @if (templateVars().length) {
        <div class="prompt-vars-bar">
          <mat-icon>data_object</mat-icon>
          <span class="prompt-vars-label">Variables:</span>
          @for (v of templateVars(); track v) {
            <span class="prompt-var-chip">{{ v }}</span>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    .prompt-editor {
      display: flex;
      flex-direction: column;
      border: 1px solid var(--sq-border, rgba(148, 163, 184, 0.2));
      border-radius: 10px;
      overflow: hidden;
      background: var(--sq-surface, rgba(30, 41, 59, 0.95));

      &--fullscreen {
        position: fixed;
        top: 0; left: 0; right: 0; bottom: 0;
        z-index: 1000;
        border-radius: 0;
        border: none;
      }
    }

    .prompt-editor-header {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px 12px;
      border-bottom: 1px solid var(--sq-border, rgba(148, 163, 184, 0.1));
      background: rgba(0, 0, 0, 0.15);
    }

    .prompt-editor-title {
      display: flex; align-items: center; gap: 6px;
      font-size: 13px; font-weight: 600;
      color: var(--sq-text-primary, #f1f5f9);
      mat-icon { font-size: 18px; width: 18px; height: 18px; color: #818cf8; }
    }

    .prompt-editor-meta {
      display: flex; gap: 8px; margin-left: auto;
    }

    .prompt-meta-item {
      font-size: 11px; font-weight: 500;
      color: var(--sq-text-secondary, #94a3b8);
      font-family: 'JetBrains Mono', 'Fira Code', monospace;
    }

    .prompt-meta-vars { color: #f59e0b; }

    .prompt-editor-actions {
      display: flex; gap: 2px; margin-left: 8px;
    }

    .prompt-action-btn {
      display: flex; align-items: center; justify-content: center;
      width: 28px; height: 28px; border: none; border-radius: 6px;
      background: transparent; color: var(--sq-text-secondary, #94a3b8);
      cursor: pointer; transition: all 0.15s ease;
      mat-icon { font-size: 16px; width: 16px; height: 16px; }
      &:hover { background: rgba(148, 163, 184, 0.1); color: var(--sq-text-primary, #f1f5f9); }
    }

    .prompt-action-close { &:hover { background: rgba(239, 68, 68, 0.15); color: #ef4444; } }

    .prompt-editor-body {
      display: flex; flex: 1; min-height: 200px;
    }

    .prompt-pane {
      flex: 1; display: flex; flex-direction: column;
      &-edit { border-right: 1px solid var(--sq-border, rgba(148, 163, 184, 0.1)); }
    }

    .prompt-pane-label {
      font-size: 10px; font-weight: 700; text-transform: uppercase;
      letter-spacing: 0.5px; color: var(--sq-text-secondary, #94a3b8);
      padding: 6px 12px; background: rgba(0, 0, 0, 0.1);
      border-bottom: 1px solid var(--sq-border, rgba(148, 163, 184, 0.06));
    }

    .prompt-textarea {
      flex: 1; width: 100%; border: none; outline: none; resize: none;
      padding: 12px; background: transparent;
      color: var(--sq-text-primary, #f1f5f9);
      font-size: 13px; line-height: 1.6;
      font-family: 'JetBrains Mono', 'Fira Code', monospace;
      &::placeholder { color: var(--sq-text-secondary, rgba(148, 163, 184, 0.4)); }
    }

    .prompt-preview-content {
      flex: 1; padding: 12px; overflow-y: auto;
    }

    .prompt-preview-empty {
      display: flex; flex-direction: column; align-items: center;
      justify-content: center; height: 100%;
      color: var(--sq-text-secondary, #94a3b8); opacity: 0.5;
      mat-icon { font-size: 32px; width: 32px; height: 32px; margin-bottom: 8px; }
      p { margin: 0; font-size: 12px; }
    }

    .prompt-vars-bar {
      display: flex; align-items: center; gap: 6px;
      padding: 6px 12px;
      border-top: 1px solid var(--sq-border, rgba(148, 163, 184, 0.1));
      background: rgba(245, 158, 11, 0.04);
      mat-icon { font-size: 14px; width: 14px; height: 14px; color: #f59e0b; }
    }

    .prompt-vars-label {
      font-size: 11px; font-weight: 600; color: var(--sq-text-secondary, #94a3b8);
    }

    .prompt-var-chip {
      font-size: 11px; font-weight: 600; padding: 2px 8px;
      border-radius: 6px; background: rgba(245, 158, 11, 0.12);
      color: #f59e0b; font-family: 'JetBrains Mono', 'Fira Code', monospace;
    }
  `],
})
export class PromptEditorComponent {
  readonly label = input('System Prompt');
  readonly content = input('');
  readonly contentChange = output<string>();

  readonly isFullscreen = signal(false);
  readonly copySuccess = signal(false);

  /** Approximate token count (chars / 4). */
  readonly tokenEstimate = computed(() => Math.ceil(this.content().length / 4));

  /** Extract {{variable}} template placeholders. */
  readonly templateVars = computed(() => {
    const matches = this.content().match(/\{\{(\w+)\}\}/g);
    if (!matches) return [];
    return [...new Set(matches.map(m => m.replace(/[{}]/g, '')))];
  });

  onContentChange(value: string): void {
    this.contentChange.emit(value);
  }

  async copyToClipboard(): Promise<void> {
    try {
      await navigator.clipboard.writeText(this.content());
      this.copySuccess.set(true);
      setTimeout(() => this.copySuccess.set(false), 2000);
    } catch {
      // Fallback — copy not supported in all contexts
    }
  }
}
