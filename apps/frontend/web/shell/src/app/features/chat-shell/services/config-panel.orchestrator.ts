// ---------------------------------------------------------------------------
// ConfigPanelOrchestrator — admin config panel open/save logic
// ---------------------------------------------------------------------------

import { Injectable, inject } from '@angular/core';
import {
  ConfigService,
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
import { ThemeService } from '../../../core/theme.service';
import { ChatMessage } from '../chat-message.model';
import {
  CONFIG_PANEL_TITLES,
  type ConfigPanelType,
  buildConfigSuggestions,
} from '../chat-shell.constants';

/** All possible config data types returned by panel loaders. */
export type ConfigData =
  | AIPersonaConfig
  | LLMProviderConfig
  | AIGuardrailsConfig
  | ComponentEnablement
  | ActionsConfig
  | BrandingConfig
  | ThemePreset[]
  | PersonalizationConfig;

/** Result of loading a config panel. */
export interface ConfigLoadResult {
  configData: ConfigData;
  contrastCheck?: ContrastCheck;
}

/** Result of saving a config field. */
export interface ConfigSaveResult {
  updated: ConfigData;
  contrastCheck?: ContrastCheck;
}

// ── Callbacks ───────────────────────────────────────────────────────────────

export interface ConfigPanelCallbacks {
  updateMessages: (fn: (msgs: ChatMessage[]) => ChatMessage[]) => void;
  appendMessages: (msgs: ChatMessage[]) => void;
  applyBranding?: (branding: BrandingConfig) => void;
}

@Injectable({ providedIn: 'root' })
export class ConfigPanelOrchestrator {
  private readonly configService = inject(ConfigService);
  private readonly themeService = inject(ThemeService);

  // ── Load ────────────────────────────────────────────────────────────────

  /** Load config data for the specified panel. */
  async loadConfig(panel: ConfigPanelType): Promise<ConfigLoadResult> {
    let configData: ConfigData;
    let contrastCheck: ContrastCheck | undefined;

    switch (panel) {
      case 'persona':
        configData = await this.configService.getAIPersona();
        break;
      case 'provider':
        configData = await this.configService.getLLMProvider();
        break;
      case 'guardrails':
        configData = await this.configService.getAIGuardrails();
        break;
      case 'components':
        configData = await this.configService.getComponents();
        break;
      case 'actions':
        configData = await this.configService.getActions();
        break;
      case 'branding': {
        const brandResp = await this.configService.getBranding();
        configData = brandResp;
        try {
          contrastCheck = await this.configService.checkContrast(
            brandResp.primary_color,
            brandResp.background_style === 'light' ? 'light' : 'dark',
          );
        } catch { /* non-critical */ }
        break;
      }
      case 'themes':
        configData = await this.configService.getThemes();
        break;
      case 'personalization':
        configData = await this.configService.getPersonalization();
        break;
    }

    return { configData, contrastCheck };
  }

  // ── Save ────────────────────────────────────────────────────────────────

  /** Save a single field on the specified config panel. */
  async saveField(
    panel: ConfigPanelType,
    field: string,
    value: unknown,
  ): Promise<ConfigSaveResult> {
    let updated: ConfigData;
    let contrastCheck: ContrastCheck | undefined;

    switch (panel) {
      case 'persona':
        updated = await this.configService.patchAIPersona({ [field]: value } as Partial<AIPersonaConfig>);
        break;
      case 'provider':
        updated = await this.configService.patchLLMProvider({ [field]: value } as Partial<LLMProviderConfig>);
        break;
      case 'guardrails':
        updated = await this.configService.patchAIGuardrails({ [field]: value } as Partial<AIGuardrailsConfig>);
        break;
      case 'components':
        updated = await this.configService.patchComponents({ [field]: value } as Partial<ComponentEnablement>);
        break;
      case 'actions':
        updated = await this.configService.patchActions({ [field]: value } as Partial<ActionsConfig>);
        break;
      case 'branding': {
        const resp = await this.configService.patchBranding({ [field]: value } as Partial<BrandingConfig>);
        contrastCheck = resp.contrast_check;
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        const { contrast_check: _cc, ...brandingOnly } = resp;
        updated = brandingOnly as BrandingConfig;
        // Live-reload branding in the theme service
        this.themeService.branding.set(updated as BrandingConfig);
        break;
      }
      case 'themes':
        updated = await this.configService.getThemes(); // refresh full list
        break;
      case 'personalization':
        updated = await this.configService.patchPersonalization({ [field]: value } as Partial<PersonalizationConfig>);
        break;
    }

    return { updated, contrastCheck };
  }

  // ── Full panel open flow ────────────────────────────────────────────────

  /** Open a config panel inline in the chat (loading → loaded/error). */
  async openPanel(panel: ConfigPanelType, callbacks: ConfigPanelCallbacks): Promise<void> {
    const msgId = crypto.randomUUID();
    const title = CONFIG_PANEL_TITLES[panel];

    // Show loading state
    callbacks.appendMessages([
      {
        id: msgId,
        role: 'assistant',
        content: `Loading ${title}...`,
        timestamp: new Date(),
        configPanel: panel,
      },
    ]);

    try {
      const { configData, contrastCheck } = await this.loadConfig(panel);

      callbacks.updateMessages((msgs) =>
        msgs.map((m) =>
          m.id === msgId
            ? {
                ...m,
                content: title,
                configData,
                contrastCheck,
                uiComponents: [
                  {
                    type: 'info_banner' as const,
                    title,
                    body: 'Current settings loaded. Use the panel below to modify.',
                    style: 'info' as const,
                    suggestions: buildConfigSuggestions(panel),
                  },
                ],
              }
            : m,
        ),
      );
    } catch (err: unknown) {
      const errorMsg = err instanceof Error ? err.message : 'Failed to load configuration.';
      callbacks.updateMessages((msgs) =>
        msgs.map((m) =>
          m.id === msgId
            ? {
                ...m,
                content: '',
                configPanel: undefined,
                uiComponents: [
                  {
                    type: 'info_banner' as const,
                    title: 'Config Error',
                    body: errorMsg,
                    style: 'error' as const,
                    suggestions: [{ label: 'Retry', prompt: `__CONFIG_${panel.toUpperCase()}__` }],
                  },
                ],
              }
            : m,
        ),
      );
    }
  }

  /** Save a field and update the chat messages accordingly. */
  async saveFieldAndNotify(
    panel: ConfigPanelType,
    field: string,
    value: unknown,
    messageId: string,
    callbacks: ConfigPanelCallbacks,
  ): Promise<void> {
    try {
      const { updated, contrastCheck } = await this.saveField(panel, field, value);

      // Update the config data in the message
      callbacks.updateMessages((msgs) =>
        msgs.map((m) => m.id === messageId ? { ...m, configData: updated, contrastCheck } : m),
      );

      // Add a success confirmation
      callbacks.appendMessages([
        {
          id: crypto.randomUUID(),
          role: 'assistant',
          content: `✅ Updated **${field}** successfully.`,
          timestamp: new Date(),
          uiComponents: [
            {
              type: 'info_banner' as const,
              title: 'Config Saved',
              body: `The ${field} setting has been updated.`,
              style: 'success' as const,
              suggestions: buildConfigSuggestions(panel),
            },
          ],
        },
      ]);
    } catch (err: unknown) {
      const errorMsg = err instanceof Error ? err.message : 'Failed to save configuration.';
      callbacks.appendMessages([
        {
          id: crypto.randomUUID(),
          role: 'assistant',
          content: '',
          timestamp: new Date(),
          uiComponents: [
            {
              type: 'info_banner' as const,
              title: 'Save Failed',
              body: errorMsg,
              style: 'error' as const,
              suggestions: [{ label: 'Retry', prompt: `__CONFIG_${panel.toUpperCase()}__` }],
            },
          ],
        },
      ]);
    }
  }

  // ── Public aliases (match the names used by ChatShellComponent) ─────────

  /** Alias for `openPanel` — matches the component's naming convention. */
  async openConfigPanel(panel: ConfigPanelType, callbacks: ConfigPanelCallbacks): Promise<void> {
    return this.openPanel(panel, callbacks);
  }

  /** Alias for `saveFieldAndNotify` — matches the component's naming convention. */
  async saveConfigField(
    panel: ConfigPanelType,
    field: string,
    value: unknown,
    messageId: string,
    callbacks: ConfigPanelCallbacks,
  ): Promise<void> {
    return this.saveFieldAndNotify(panel, field, value, messageId, callbacks);
  }

  /** Get suggestion chips for a config panel, excluding the current panel. */
  getConfigSuggestions(currentPanel: string): Array<{ label: string; prompt: string }> {
    return buildConfigSuggestions(currentPanel);
  }
}
