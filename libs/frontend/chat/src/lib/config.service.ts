/**
 * ConfigService — HTTP client for the Config API (Phase 9 + Phase 10).
 *
 * Provides typed access to all tenant configuration sections:
 *   - AI Persona (T9.1)
 *   - LLM Provider / BYOK (T9.2)
 *   - AI Guardrails (T9.3)
 *   - Component Enablement (T9.4)
 *   - Action Settings (T9.5)
 *   - Branding & Theming (T10.1–T10.5)
 *   - Personalization (T10.8–T10.9)
 */
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ENVIRONMENT } from '@synaptiq/utils';

// ---------------------------------------------------------------------------
// Interfaces — mirror the backend Pydantic models
// ---------------------------------------------------------------------------

export interface AIPersonaConfig {
  display_name: string;
  tone: 'professional' | 'friendly' | 'concise' | 'enthusiastic' | 'formal';
  custom_instruction: string;
  welcome_message: string;
  starter_prompts: string[];
}

export interface LLMProviderConfig {
  provider: 'platform_managed' | 'openai' | 'gemini' | 'anthropic';
  model_id: string;
  is_byok: boolean;
  byok_encrypted_key: string; // Masked from server
  has_key: boolean;
}

export interface AIGuardrailsConfig {
  out_of_scope_message: string;
  recommendation_mode: boolean;
  language: string;
}

export interface ComponentEnablement {
  item_card: boolean;
  item_grid: boolean;
  item_detail: boolean;
  comparison_table: boolean;
  filter_summary: boolean;
  result_count: boolean;
  empty_state: boolean;
  action_confirm: boolean;
  info_banner: boolean;
}

export interface ActionConfigItem {
  action_id: string;
  enabled: boolean;
  label: string;
}

export interface ActionsConfig {
  actions: ActionConfigItem[];
  enquiry_webhook_url: string;
  enquiry_email: string;
}

// Phase 10 — Branding & Theming
export interface BrandingConfig {
  logo_url: string;
  primary_color: string;
  secondary_color: string;
  background_style: 'light' | 'dark' | 'auto';
  heading_font: string;
  body_font: string;
  ui_font: string;
  favicon_url: string;
  page_title: string;
  show_platform_branding: boolean;
}

export interface BrandingPatchResponse extends BrandingConfig {
  contrast_check: ContrastCheck;
}

export interface ContrastCheck {
  passes: boolean;
  ratio: number;
  foreground: string;
  background: string;
  message: string;
}

export interface ThemePreset {
  theme_id: string;
  name: string;
  primary_color: string;
  secondary_color: string;
  background_style: 'light' | 'dark' | 'auto';
  heading_font: string;
  body_font: string;
  is_default: boolean;
}

export interface PersonalizationConfig {
  allow_theme_switch: boolean;
  allow_font_switch: boolean;
  allow_bubble_style: boolean;
}

export interface PublicAIPersona {
  display_name: string;
  welcome_message: string;
  starter_prompts: string[];
}

export interface PublicBrandingResponse {
  branding: BrandingConfig;
  personalization: PersonalizationConfig;
  themes: ThemePreset[];
  default_theme: ThemePreset | null;
  ai_persona: PublicAIPersona;
}

// ---------------------------------------------------------------------------
// Service
// ---------------------------------------------------------------------------

@Injectable({ providedIn: 'root' })
export class ConfigService {
  private readonly http = inject(HttpClient);
  private readonly env = inject(ENVIRONMENT);
  private readonly baseUrl = `${this.env.apiBaseUrl}/api/v1/config`;

  // ── AI Persona (T9.1) ───────────────────────────────────────────────

  async getAIPersona(): Promise<AIPersonaConfig> {
    return firstValueFrom(
      this.http.get<AIPersonaConfig>(`${this.baseUrl}/ai`),
    );
  }

  async patchAIPersona(updates: Partial<AIPersonaConfig>): Promise<AIPersonaConfig> {
    return firstValueFrom(
      this.http.patch<AIPersonaConfig>(`${this.baseUrl}/ai`, updates),
    );
  }

  // ── LLM Provider (T9.2) ─────────────────────────────────────────────

  async getLLMProvider(): Promise<LLMProviderConfig> {
    return firstValueFrom(
      this.http.get<LLMProviderConfig>(`${this.baseUrl}/ai/provider`),
    );
  }

  async patchLLMProvider(updates: Partial<LLMProviderConfig>): Promise<LLMProviderConfig> {
    return firstValueFrom(
      this.http.patch<LLMProviderConfig>(`${this.baseUrl}/ai/provider`, updates),
    );
  }

  // ── AI Guardrails (T9.3) ────────────────────────────────────────────

  async getAIGuardrails(): Promise<AIGuardrailsConfig> {
    return firstValueFrom(
      this.http.get<AIGuardrailsConfig>(`${this.baseUrl}/ai/guardrails`),
    );
  }

  async patchAIGuardrails(updates: Partial<AIGuardrailsConfig>): Promise<AIGuardrailsConfig> {
    return firstValueFrom(
      this.http.patch<AIGuardrailsConfig>(`${this.baseUrl}/ai/guardrails`, updates),
    );
  }

  // ── Component Enablement (T9.4) ─────────────────────────────────────

  async getComponents(): Promise<ComponentEnablement> {
    return firstValueFrom(
      this.http.get<ComponentEnablement>(`${this.baseUrl}/components`),
    );
  }

  async patchComponents(updates: Partial<ComponentEnablement>): Promise<ComponentEnablement> {
    return firstValueFrom(
      this.http.patch<ComponentEnablement>(`${this.baseUrl}/components`, updates),
    );
  }

  // ── Actions Config (T9.5) ───────────────────────────────────────────

  async getActions(): Promise<ActionsConfig> {
    return firstValueFrom(
      this.http.get<ActionsConfig>(`${this.baseUrl}/actions`),
    );
  }

  async patchActions(updates: Partial<ActionsConfig>): Promise<ActionsConfig> {
    return firstValueFrom(
      this.http.patch<ActionsConfig>(`${this.baseUrl}/actions`, updates),
    );
  }

  // ── Branding (T10.1) ────────────────────────────────────────────────

  async getBranding(): Promise<BrandingConfig> {
    return firstValueFrom(
      this.http.get<BrandingConfig>(`${this.baseUrl}/branding`),
    );
  }

  async patchBranding(updates: Partial<BrandingConfig>): Promise<BrandingPatchResponse> {
    return firstValueFrom(
      this.http.patch<BrandingPatchResponse>(`${this.baseUrl}/branding`, updates),
    );
  }

  // ── Logo Upload (T10.2) ─────────────────────────────────────────────

  async uploadLogo(file: File): Promise<{ logo_url: string; filename: string; size_bytes: string }> {
    const formData = new FormData();
    formData.append('file', file);
    return firstValueFrom(
      this.http.post<{ logo_url: string; filename: string; size_bytes: string }>(
        `${this.baseUrl}/branding/logo`,
        formData,
      ),
    );
  }

  // ── Contrast Check (T10.3) ──────────────────────────────────────────

  async checkContrast(foreground: string, background = 'dark'): Promise<ContrastCheck> {
    return firstValueFrom(
      this.http.get<ContrastCheck>(`${this.baseUrl}/branding/contrast-check`, {
        params: { foreground, background },
      }),
    );
  }

  // ── Named Themes (T10.5) ────────────────────────────────────────────

  async getThemes(): Promise<ThemePreset[]> {
    return firstValueFrom(
      this.http.get<ThemePreset[]>(`${this.baseUrl}/themes`),
    );
  }

  async createTheme(theme: Omit<ThemePreset, 'is_default'> & { is_default?: boolean }): Promise<ThemePreset> {
    return firstValueFrom(
      this.http.post<ThemePreset>(`${this.baseUrl}/themes`, theme),
    );
  }

  async patchTheme(themeId: string, updates: Partial<ThemePreset>): Promise<ThemePreset> {
    return firstValueFrom(
      this.http.patch<ThemePreset>(`${this.baseUrl}/themes/${themeId}`, updates),
    );
  }

  async deleteTheme(themeId: string): Promise<void> {
    return firstValueFrom(
      this.http.delete<void>(`${this.baseUrl}/themes/${themeId}`),
    );
  }

  // ── Personalization (T10.8) ─────────────────────────────────────────

  async getPersonalization(): Promise<PersonalizationConfig> {
    return firstValueFrom(
      this.http.get<PersonalizationConfig>(`${this.baseUrl}/personalization`),
    );
  }

  async patchPersonalization(updates: Partial<PersonalizationConfig>): Promise<PersonalizationConfig> {
    return firstValueFrom(
      this.http.patch<PersonalizationConfig>(`${this.baseUrl}/personalization`, updates),
    );
  }

  // ── Public Branding (no admin auth) ─────────────────────────────────

  async getPublicBranding(): Promise<PublicBrandingResponse> {
    return firstValueFrom(
      this.http.get<PublicBrandingResponse>(`${this.baseUrl}/branding/public`),
    );
  }
}
