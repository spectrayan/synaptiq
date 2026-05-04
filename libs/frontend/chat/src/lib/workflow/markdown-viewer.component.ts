/**
 * MarkdownViewerComponent — Renders markdown content with syntax highlighting.
 *
 * Features:
 *   - Renders markdown with headings, lists, code blocks, tables
 *   - Toggle between rendered/raw view
 *   - Copy-to-clipboard
 *   - Collapsible long content
 */
import {
  Component,
  input,
  signal,
  computed,
  ChangeDetectionStrategy,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';

const COLLAPSE_THRESHOLD = 1500; // Characters

@Component({
  selector: 'sq-markdown-viewer',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatTooltipModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="md-viewer" [class.md-viewer--expanded]="expanded()">
      <div class="md-viewer-toolbar">
        <button
          class="md-btn"
          [class.md-btn--active]="mode() === 'rendered'"
          (click)="mode.set('rendered')"
          matTooltip="Rendered view">
          <mat-icon>article</mat-icon>
        </button>
        <button
          class="md-btn"
          [class.md-btn--active]="mode() === 'raw'"
          (click)="mode.set('raw')"
          matTooltip="Raw text">
          <mat-icon>code</mat-icon>
        </button>
        <span class="md-spacer"></span>
        @if (copyFeedback()) {
          <span class="md-copy-feedback">✓ Copied</span>
        }
        <button class="md-btn" (click)="copyToClipboard()" matTooltip="Copy to clipboard">
          <mat-icon>content_copy</mat-icon>
        </button>
        @if (isLong()) {
          <button class="md-btn" (click)="expanded.update(v => !v)"
                  [matTooltip]="expanded() ? 'Collapse' : 'Expand'">
            <mat-icon>{{ expanded() ? 'unfold_less' : 'unfold_more' }}</mat-icon>
          </button>
        }
      </div>

      @if (mode() === 'raw') {
        <pre class="md-raw">{{ displayContent() }}</pre>
      } @else {
        <div class="md-rendered" [innerHTML]="renderedHtml()"></div>
      }

      @if (isLong() && !expanded()) {
        <div class="md-fade-overlay" tabindex="0" role="button" (click)="expanded.set(true)" (keyup.enter)="expanded.set(true)" (keyup.space)="expanded.set(true)">
          <span class="md-show-more">Show more ↓</span>
        </div>
      }
    </div>
  `,
  styles: [`
    .md-viewer {
      position: relative;
      border: 1px solid var(--sq-border, rgba(148,163,184,0.15));
      border-radius: 8px;
      overflow: hidden;
      background: var(--sq-surface, #1e293b);
      max-height: 400px;
      overflow-y: auto;
    }
    .md-viewer--expanded {
      max-height: none;
    }

    .md-viewer-toolbar {
      display: flex;
      align-items: center;
      gap: 2px;
      padding: 4px 8px;
      border-bottom: 1px solid var(--sq-border, rgba(148,163,184,0.1));
      background: rgba(0,0,0,0.15);
    }

    .md-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 28px;
      height: 28px;
      border: none;
      border-radius: 6px;
      background: transparent;
      color: var(--sq-text-secondary, #94a3b8);
      cursor: pointer;
      transition: all 0.15s ease;
    }
    .md-btn mat-icon { font-size: 16px; width: 16px; height: 16px; }
    .md-btn:hover { background: rgba(255,255,255,0.08); color: var(--sq-text-primary, #e2e8f0); }
    .md-btn--active { background: rgba(99,102,241,0.15); color: #818cf8; }

    .md-spacer { flex: 1; }

    .md-copy-feedback {
      font-size: 11px;
      color: #22c55e;
      margin-right: 4px;
      animation: fadeIn 0.2s ease;
    }

    .md-raw {
      margin: 0;
      padding: 12px 16px;
      font-size: 12px;
      line-height: 1.6;
      color: var(--sq-text-primary, #e2e8f0);
      white-space: pre-wrap;
      word-break: break-word;
      font-family: 'JetBrains Mono', 'Fira Code', monospace;
    }

    .md-rendered {
      padding: 12px 16px;
      font-size: 13px;
      line-height: 1.7;
      color: var(--sq-text-primary, #e2e8f0);
    }

    /* Markdown element styles */
    :host ::ng-deep .md-rendered {
      h1, h2, h3, h4 {
        margin: 16px 0 8px;
        font-weight: 600;
        color: var(--sq-text-primary, #f1f5f9);
      }
      h1 { font-size: 18px; border-bottom: 1px solid var(--sq-border, rgba(148,163,184,0.15)); padding-bottom: 6px; }
      h2 { font-size: 16px; }
      h3 { font-size: 14px; }
      h4 { font-size: 13px; }

      p { margin: 8px 0; }

      ul, ol { margin: 8px 0; padding-left: 20px; }
      li { margin: 4px 0; }

      code {
        background: rgba(99,102,241,0.1);
        padding: 2px 6px;
        border-radius: 4px;
        font-size: 12px;
        font-family: 'JetBrains Mono', 'Fira Code', monospace;
        color: #c084fc;
      }

      pre {
        background: rgba(0,0,0,0.3);
        border-radius: 6px;
        padding: 12px;
        overflow-x: auto;
        margin: 8px 0;
      }
      pre code {
        background: none;
        padding: 0;
        color: var(--sq-text-primary, #e2e8f0);
      }

      blockquote {
        border-left: 3px solid #6366f1;
        margin: 8px 0;
        padding: 4px 12px;
        color: var(--sq-text-secondary, #94a3b8);
      }

      table {
        width: 100%;
        border-collapse: collapse;
        margin: 8px 0;
      }
      th, td {
        border: 1px solid var(--sq-border, rgba(148,163,184,0.15));
        padding: 6px 10px;
        text-align: left;
        font-size: 12px;
      }
      th { background: rgba(99,102,241,0.08); font-weight: 600; }

      strong { color: var(--sq-text-primary, #f1f5f9); }
      em { color: var(--sq-text-secondary, #94a3b8); }

      a { color: #818cf8; text-decoration: none; }
      a:hover { text-decoration: underline; }

      hr {
        border: none;
        border-top: 1px solid var(--sq-border, rgba(148,163,184,0.15));
        margin: 16px 0;
      }
    }

    .md-fade-overlay {
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      height: 60px;
      background: linear-gradient(transparent, var(--sq-surface, #1e293b));
      display: flex;
      align-items: flex-end;
      justify-content: center;
      padding-bottom: 8px;
      cursor: pointer;
    }

    .md-show-more {
      font-size: 12px;
      color: #818cf8;
      font-weight: 500;
    }

    @keyframes fadeIn {
      from { opacity: 0; }
      to { opacity: 1; }
    }
  `],
})
export class MarkdownViewerComponent {
  readonly content = input<string>('');
  readonly mode = signal<'rendered' | 'raw'>('rendered');
  readonly expanded = signal(false);
  readonly copyFeedback = signal(false);

  readonly isLong = computed(() => (this.content()?.length ?? 0) > COLLAPSE_THRESHOLD);

  readonly displayContent = computed(() => {
    const raw = this.content() ?? '';
    if (!this.expanded() && this.isLong()) {
      return raw.slice(0, COLLAPSE_THRESHOLD) + '…';
    }
    return raw;
  });

  readonly renderedHtml = computed(() => {
    const raw = this.displayContent();
    return this.parseMarkdown(raw);
  });

  copyToClipboard(): void {
    navigator.clipboard.writeText(this.content() ?? '').then(() => {
      this.copyFeedback.set(true);
      setTimeout(() => this.copyFeedback.set(false), 2000);
    });
  }

  /**
   * Lightweight markdown parser — handles headings, bold, italic, code blocks,
   * inline code, lists, blockquotes, links, tables, and horizontal rules.
   * No external dependency required.
   */
  private parseMarkdown(md: string): string {
    if (!md) return '';

    let html = md;

    // Escape HTML entities (prevent XSS)
    html = html
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;');

    // Fenced code blocks (```...```)
    html = html.replace(/```(\w*)\n([\s\S]*?)```/g, (_m, _lang, code) =>
      `<pre><code>${code.trim()}</code></pre>`
    );

    // Inline code (`...`)
    html = html.replace(/`([^`\n]+)`/g, '<code>$1</code>');

    // Headings (#### before ### before ## before #)
    html = html.replace(/^#### (.+)$/gm, '<h4>$1</h4>');
    html = html.replace(/^### (.+)$/gm, '<h3>$1</h3>');
    html = html.replace(/^## (.+)$/gm, '<h2>$1</h2>');
    html = html.replace(/^# (.+)$/gm, '<h1>$1</h1>');

    // Horizontal rule
    html = html.replace(/^---$/gm, '<hr>');

    // Blockquotes
    html = html.replace(/^> (.+)$/gm, '<blockquote>$1</blockquote>');

    // Bold + italic
    html = html.replace(/\*\*\*(.+?)\*\*\*/g, '<strong><em>$1</em></strong>');
    html = html.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>');
    html = html.replace(/\*(.+?)\*/g, '<em>$1</em>');
    html = html.replace(/_(.+?)_/g, '<em>$1</em>');

    // Links [text](url)
    html = html.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank" rel="noopener">$1</a>');

    // Unordered lists
    html = html.replace(/^[\s]*[-*] (.+)$/gm, '<li>$1</li>');
    html = html.replace(/(<li>[\s\S]*?<\/li>)/g, '<ul>$1</ul>');
    // Collapse adjacent <ul> tags
    html = html.replace(/<\/ul>\s*<ul>/g, '');

    // Ordered lists
    html = html.replace(/^\d+\. (.+)$/gm, '<li>$1</li>');

    // Tables
    html = html.replace(/^\|(.+)\|$/gm, (_match, row: string) => {
      const cells = row.split('|').map((c: string) => c.trim()).filter(Boolean);
      if (cells.every((c: string) => /^[-:]+$/.test(c))) return ''; // separator row
      const tag = 'td';
      return '<tr>' + cells.map((c: string) => `<${tag}>${c}</${tag}>`).join('') + '</tr>';
    });
    html = html.replace(/(<tr>[\s\S]*?<\/tr>)/g, '<table>$1</table>');
    html = html.replace(/<\/table>\s*<table>/g, '');

    // Paragraphs — wrap standalone lines
    html = html.replace(/^(?!<[a-z])(.+)$/gm, '<p>$1</p>');
    // Clean up empty paragraphs
    html = html.replace(/<p>\s*<\/p>/g, '');

    return html;
  }
}
