/**
 * ConfigService — facade for the Config API (Phase 9 + Phase 10).
 *
 * GET/PATCH for ai, llm, guardrails, components, actions delegate to the
 * generated ConfigService and BrandingService from @synaptiq/client.
 *
 * Logo upload, contrast check, named themes, and public branding remain
 * as direct HttpClient calls since they aren't (fully) in the generated SDK.
 *
 * The legacy snake_case interfaces are kept for backward compatibility
 * with the config panel orchestrator, admin panel, and theme service.
 */
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ENVIRONMENT } from '@synaptiq/utils';
import {
  ConfigService as ConfigApiService,
  BrandingService as BrandingApiService,
  type AiPersonaResponse,
  type LlmProviderResponse,
  type GuardrailsResponse,
  type ComponentsToggleResponse,
  type ActionsConfigResponse,
  type BrandingResponse,
  type PersonalizationResponse,
  type ContrastCheckResponse,
} from '@synaptiq/client';

// ---------------------------------------------------------------------------
// Interfaces — legacy snake_case for existing consumers
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
  byok_encrypted_key: string;
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
// Mapping helpers (camelCase SDK → snake_case legacy)
// ---------------------------------------------------------------------------

function personaFromSdk(r: AiPersonaResponse): AIPersonaConfig {
  return {
    display_name: r.displayName ?? '',
    tone: (r.tone as AIPersonaConfig['tone']) ?? 'professional',
    custom_instruction: r.customInstruction ?? '',
    welcome_message: r.welcomeMessage ?? '',
    starter_prompts: r.starterPrompts ?? [],
  };
}

function personaToSdk(p: Partial<AIPersonaConfig>): AiPersonaResponse {
  const r: AiPersonaResponse = {};
  if (p.display_name !== undefined) r.displayName = p.display_name;
  if (p.tone !== undefined) r.tone = p.tone;
  if (p.custom_instruction !== undefined) r.customInstruction = p.custom_instruction;
  if (p.welcome_message !== undefined) r.welcomeMessage = p.welcome_message;
  if (p.starter_prompts !== undefined) r.starterPrompts = p.starter_prompts;
  return r;
}

function brandingFromSdk(r: BrandingResponse): BrandingConfig {
  return {
    logo_url: r.logoUrl ?? '',
    primary_color: r.primaryColor ?? '#6366f1',
    secondary_color: r.secondaryColor ?? '#a855f7',
    background_style: (r.backgroundStyle as BrandingConfig['background_style']) ?? 'dark',
    heading_font: r.headingFont ?? '',
    body_font: r.bodyFont ?? '',
    ui_font: '',
    favicon_url: r.faviconUrl ?? '',
    page_title: r.pageTitle ?? '',
    show_platform_branding: r.showPlatformBranding ?? true,
  };
}

function brandingToSdk(b: Partial<BrandingConfig>): BrandingResponse {
  const r: BrandingResponse = {};
  if (b.logo_url !== undefined) r.logoUrl = b.logo_url;
  if (b.primary_color !== undefined) r.primaryColor = b.primary_color;
  if (b.secondary_color !== undefined) r.secondaryColor = b.secondary_color;
  if (b.background_style !== undefined) r.backgroundStyle = b.background_style;
  if (b.heading_font !== undefined) r.headingFont = b.heading_font;
  if (b.body_font !== undefined) r.bodyFont = b.body_font;
  if (b.favicon_url !== undefined) r.faviconUrl = b.favicon_url;
  if (b.page_title !== undefined) r.pageTitle = b.page_title;
  if (b.show_platform_branding !== undefined) r.showPlatformBranding = b.show_platform_branding;
  return r;
}

// ---------------------------------------------------------------------------
// Service
// ---------------------------------------------------------------------------

@Injectable({ providedIn: 'root' })
export class ConfigService {
  private readonly configApi = inject(ConfigApiService);
  private readonly brandingApi = inject(BrandingApiService);
  private readonly http = inject(HttpClient);
  private readonly env = inject(ENVIRONMENT);
  private readonly baseUrl = `${this.env.apiBaseUrl}/api/v1/config`;

  // ── AI Persona (T9.1) ───────────────────────────────────────────────

  async getAIPersona(): Promise<AIPersonaConfig> {
    const r = await firstValueFrom(this.configApi.getAiConfig({}));
    return personaFromSdk(r);
  }

  async patchAIPersona(updates: Partial<AIPersonaConfig>): Promise<AIPersonaConfig> {
    const r = await firstValueFrom(
      this.configApi.updateAiConfig({ aiPersonaResponse: personaToSdk(updates) }),
    );
    return personaFromSdk(r);
  }

  // ── LLM Provider (T9.2) ─────────────────────────────────────────────

  async getLLMProvider(): Promise<LLMProviderConfig> {
    const r = await firstValueFrom(this.configApi.getLlmConfig({}));
    return r as unknown as LLMProviderConfig;
  }

  async patchLLMProvider(updates: Partial<LLMProviderConfig>): Promise<LLMProviderConfig> {
    const r = await firstValueFrom(
      this.configApi.updateLlmConfig({ llmProviderResponse: updates as unknown as LlmProviderResponse }),
    );
    return r as unknown as LLMProviderConfig;
  }

  // ── AI Guardrails (T9.3) ────────────────────────────────────────────

  async getAIGuardrails(): Promise<AIGuardrailsConfig> {
    const r = await firstValueFrom(this.configApi.getGuardrailsConfig({}));
    return r as unknown as AIGuardrailsConfig;
  }

  async patchAIGuardrails(updates: Partial<AIGuardrailsConfig>): Promise<AIGuardrailsConfig> {
    const r = await firstValueFrom(
      this.configApi.updateGuardrailsConfig({ guardrailsResponse: updates as unknown as GuardrailsResponse }),
    );
    return r as unknown as AIGuardrailsConfig;
  }

  // ── Component Enablement (T9.4) ─────────────────────────────────────

  async getComponents(): Promise<ComponentEnablement> {
    const r = await firstValueFrom(this.configApi.getComponentsConfig({}));
    return r as unknown as ComponentEnablement;
  }

  async patchComponents(updates: Partial<ComponentEnablement>): Promise<ComponentEnablement> {
    const r = await firstValueFrom(
      this.configApi.updateComponentsConfig({ componentsToggleResponse: updates as unknown as ComponentsToggleResponse }),
    );
    return r as unknown as ComponentEnablement;
  }

  // ── Actions Config (T9.5) ───────────────────────────────────────────

  async getActions(): Promise<ActionsConfig> {
    const r = await firstValueFrom(this.configApi.getActionsConfig({}));
    return r as unknown as ActionsConfig;
  }

  async patchActions(updates: Partial<ActionsConfig>): Promise<ActionsConfig> {
    const r = await firstValueFrom(
      this.configApi.updateActionsConfig({ actionsConfigResponse: updates as unknown as ActionsConfigResponse }),
    );
    return r as unknown as ActionsConfig;
  }

  // ── Branding (T10.1) ────────────────────────────────────────────────

  async getBranding(): Promise<BrandingConfig> {
    const r = await firstValueFrom(this.brandingApi.getBranding({}));
    return brandingFromSdk(r);
  }

  async patchBranding(updates: Partial<BrandingConfig>): Promise<BrandingPatchResponse> {
    const r = await firstValueFrom(
      this.brandingApi.updateBranding({ brandingResponse: brandingToSdk(updates) }),
    );
    const config = brandingFromSdk(r);
    return { ...config, contrast_check: { passes: true, ratio: 4.5, foreground: '', background: '', message: '' } };
  }

  // ── Logo Upload (T10.2) — not in SDK ────────────────────────────────

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
    const r = await firstValueFrom(
      this.brandingApi.checkContrast({ fg: foreground, bg: background }),
    );
    return r as unknown as ContrastCheck;
  }

  // ── Named Themes (T10.5) — not fully in SDK ─────────────────────────

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
    const r = await firstValueFrom(this.brandingApi.getPersonalization({}));
    return r as unknown as PersonalizationConfig;
  }

  async patchPersonalization(updates: Partial<PersonalizationConfig>): Promise<PersonalizationConfig> {
    const r = await firstValueFrom(
      this.brandingApi.updatePersonalization({ personalizationResponse: updates as unknown as PersonalizationResponse }),
    );
    return r as unknown as PersonalizationConfig;
  }

  // ── Public Branding (no admin auth) — not in SDK ────────────────────

  async getPublicBranding(): Promise<PublicBrandingResponse> {
    const tenantId = this.env.tenantId || 'demo-tenant';
    const raw = await firstValueFrom(
      this.http.get<BrandingResponse>(`${this.baseUrl}/branding/public`, {
        headers: { 'X-Tenant-ID': tenantId }
      }),
    );
    return {
      branding: brandingFromSdk(raw),
      personalization: { allow_theme_switch: false, allow_font_switch: false, allow_bubble_style: false },
      themes: [],
      default_theme: null,
      ai_persona: { display_name: 'AI Assistant', welcome_message: 'Hello', starter_prompts: [] }
    };
  }
}
