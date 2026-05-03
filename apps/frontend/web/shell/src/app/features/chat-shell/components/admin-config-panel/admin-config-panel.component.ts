import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import {
  type AIPersonaConfig,
  type LLMProviderConfig,
  type AIGuardrailsConfig,
  type ComponentEnablement,
  type ActionsConfig,
  type BrandingConfig,
  type ThemePreset,
  type PersonalizationConfig,
  type ContrastCheck,
} from '@synaptiq/chat';

export type ConfigPanelType = 'persona' | 'provider' | 'guardrails' | 'components' | 'actions' | 'branding' | 'themes' | 'personalization';

export interface ConfigFieldSaveEvent {
  panel: ConfigPanelType;
  field: string;
  value: unknown;
  messageId: string;
}

@Component({
  selector: 'sq-admin-config-panel',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  templateUrl: './admin-config-panel.component.html',
  styleUrl: './admin-config-panel.component.scss',
})
export class AdminConfigPanelComponent {
  readonly configPanel = input.required<ConfigPanelType>();
  readonly configData = input.required<AIPersonaConfig | LLMProviderConfig | AIGuardrailsConfig | ComponentEnablement | ActionsConfig | BrandingConfig | ThemePreset[] | PersonalizationConfig>();
  readonly contrastCheck = input<ContrastCheck | undefined>(undefined);
  readonly messageId = input.required<string>();

  readonly saveField = output<ConfigFieldSaveEvent>();
  readonly triggerLogoUpload = output<string>();

  /** Emit a field save event */
  onSaveField(field: string, value: unknown): void {
    this.saveField.emit({
      panel: this.configPanel(),
      field,
      value,
      messageId: this.messageId(),
    });
  }

  /** Toggle an action's enabled state (immutable list update for template) */
  toggleAction(
    actions: Array<{ action_id: string; enabled: boolean; label: string }>,
    actionId: string,
  ): Array<{ action_id: string; enabled: boolean; label: string }> {
    return actions.map((a) =>
      a.action_id === actionId ? { ...a, enabled: !a.enabled } : a,
    );
  }

  /** Update an action's label (immutable list update for template) */
  updateActionLabel(
    actions: Array<{ action_id: string; enabled: boolean; label: string }>,
    actionId: string,
    newLabel: string,
  ): Array<{ action_id: string; enabled: boolean; label: string }> {
    return actions.map((a) =>
      a.action_id === actionId ? { ...a, label: newLabel } : a,
    );
  }

  /** Component fields for the components panel iteration */
  readonly componentFields = ['item_card', 'item_grid', 'item_detail', 'comparison_table', 'filter_summary', 'result_count', 'empty_state', 'info_banner'];

  /** Helper to format a field name for display */
  formatFieldName(field: string): string {
    return field
      .split('_')
      .map(w => w.charAt(0).toUpperCase() + w.slice(1))
      .join(' ');
  }
}
