import { ComponentSpec } from '@synaptiq/constants';
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
  type AnalyticsSummary,
  type TokenUsageSummary,
} from '@synaptiq/chat';

export interface ChatMessage {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: Date;
  /** DSL components rendered inline (e.g. item cards, forms, tables). */
  uiComponents?: ComponentSpec[];
  /** True after a form within this message has been submitted. */
  formSubmitted?: boolean;
  /** Status message shown during streaming (e.g., "Using keyword search..."). */
  statusText?: string;
  /** Inline auth form embedded in this message. */
  authForm?: 'signin' | 'signup';
  /** Admin config panel type for inline rendering (T9.6). */
  configPanel?: 'persona' | 'provider' | 'guardrails' | 'components' | 'actions' | 'branding' | 'themes' | 'personalization';
  /** Loaded config data for an admin config panel. */
  configData?: AIPersonaConfig | LLMProviderConfig | AIGuardrailsConfig | ComponentEnablement | ActionsConfig | BrandingConfig | ThemePreset[] | PersonalizationConfig;
  /** WCAG contrast check result (branding panel). */
  contrastCheck?: ContrastCheck;
  /** Analytics dashboard data (Phase 12). */
  analyticsData?: { summary?: AnalyticsSummary; tokens?: TokenUsageSummary };
  /** Whether this is a typing indicator placeholder. */
  isTyping?: boolean;
}
