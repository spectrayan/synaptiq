// ---------------------------------------------------------------------------
// ChatShell — Constants, magic strings, static data
// ---------------------------------------------------------------------------

// ── Internal command tokens ─────────────────────────────────────────────────
// Used as prompt values in suggestion chips to trigger special actions.

export const CMD_SIGN_IN = '__SIGN_IN__';
export const CMD_SIGN_UP = '__SIGN_UP__';
export const CMD_ANALYTICS_DASHBOARD = '__ANALYTICS_DASHBOARD__';
export const CMD_CONFIG_PERSONA = '__CONFIG_PERSONA__';
export const CMD_CONFIG_PROVIDER = '__CONFIG_PROVIDER__';
export const CMD_CONFIG_GUARDRAILS = '__CONFIG_GUARDRAILS__';
export const CMD_CONFIG_COMPONENTS = '__CONFIG_COMPONENTS__';
export const CMD_CONFIG_ACTIONS = '__CONFIG_ACTIONS__';
export const CMD_CONFIG_BRANDING = '__CONFIG_BRANDING__';
export const CMD_CONFIG_THEMES = '__CONFIG_THEMES__';
export const CMD_CONFIG_PERSONALIZATION = '__CONFIG_PERSONALIZATION__';

/** Mapping from config panel key to the command token that opens it. */
export const CONFIG_CMD_MAP: Record<string, string> = {
  persona: CMD_CONFIG_PERSONA,
  provider: CMD_CONFIG_PROVIDER,
  guardrails: CMD_CONFIG_GUARDRAILS,
  components: CMD_CONFIG_COMPONENTS,
  actions: CMD_CONFIG_ACTIONS,
  branding: CMD_CONFIG_BRANDING,
  themes: CMD_CONFIG_THEMES,
  personalization: CMD_CONFIG_PERSONALIZATION,
} as const;

// ── Config panel types ──────────────────────────────────────────────────────

export type ConfigPanelType =
  | 'persona'
  | 'provider'
  | 'guardrails'
  | 'components'
  | 'actions'
  | 'branding'
  | 'themes'
  | 'personalization';

// ── Config panel titles ─────────────────────────────────────────────────────

export const CONFIG_PANEL_TITLES: Record<ConfigPanelType, string> = {
  persona: '🤖 AI Persona Configuration',
  provider: '🔧 LLM Provider Settings',
  guardrails: '🛡️ AI Guardrails',
  components: '🧩 Component Enablement',
  actions: '⚡ Action Settings',
  branding: '🎨 Branding & Colors',
  themes: '🎭 Theme Presets',
  personalization: '👤 End-User Personalization',
} as const;

// ── Navigation suggestion chips ─────────────────────────────────────────────

interface SuggestionChip {
  label: string;
  prompt: string;
}

interface TaggedSuggestionChip extends SuggestionChip {
  key: string;
}

/** Full set of admin navigation chips (analytics + all config panels). */
export const ALL_ADMIN_SUGGESTION_CHIPS: TaggedSuggestionChip[] = [
  { label: '📊 Analytics', prompt: CMD_ANALYTICS_DASHBOARD, key: 'analytics' },
  { label: '🤖 AI Config', prompt: CMD_CONFIG_PERSONA, key: 'persona' },
  { label: '🔧 Provider', prompt: CMD_CONFIG_PROVIDER, key: 'provider' },
  { label: '🛡️ Guardrails', prompt: CMD_CONFIG_GUARDRAILS, key: 'guardrails' },
  { label: '🧩 Components', prompt: CMD_CONFIG_COMPONENTS, key: 'components' },
  { label: '⚡ Actions', prompt: CMD_CONFIG_ACTIONS, key: 'actions' },
  { label: '🎨 Branding', prompt: CMD_CONFIG_BRANDING, key: 'branding' },
  { label: '🎭 Themes', prompt: CMD_CONFIG_THEMES, key: 'themes' },
  { label: '👤 Personalization', prompt: CMD_CONFIG_PERSONALIZATION, key: 'personalization' },
];

/** Build context-sensitive suggestion chips, excluding the current panel. */
export function buildConfigSuggestions(currentPanel: string): SuggestionChip[] {
  return ALL_ADMIN_SUGGESTION_CHIPS
    .filter((s) => s.key !== currentPanel)
    .map(({ label, prompt }) => ({ label, prompt }));
}

// ── Admin welcome message suggestion chips ──────────────────────────────────

export const ADMIN_WELCOME_SUGGESTIONS: SuggestionChip[] = [
  { label: '📊 Analytics', prompt: CMD_ANALYTICS_DASHBOARD },
  { label: '🤖 AI Config', prompt: CMD_CONFIG_PERSONA },
  { label: '🔧 Provider', prompt: CMD_CONFIG_PROVIDER },
  { label: '🛡️ Guardrails', prompt: CMD_CONFIG_GUARDRAILS },
  { label: '🧩 Components', prompt: CMD_CONFIG_COMPONENTS },
  { label: '⚡ Actions', prompt: CMD_CONFIG_ACTIONS },
  { label: '🎨 Branding', prompt: CMD_CONFIG_BRANDING },
  { label: '🎭 Themes', prompt: CMD_CONFIG_THEMES },
  { label: '👤 Personalization', prompt: CMD_CONFIG_PERSONALIZATION },
  { label: '📋 View Schema', prompt: 'Show my schema' },
];

// ── Workflow intent detection ───────────────────────────────────────────────

export const WORKFLOW_INTENT_KEYWORDS: string[] = [
  'workflow', 'agent flow', 'build a flow', 'create a workflow',
  'design a workflow', 'agent pipeline', 'multi-agent',
  'supervisor agent', 'build agents', 'create agents', 'agent team',
  'discussing the goals', 'generate a plan', 'agents discussing',
];

/** Returns true if the user message signals workflow creation intent. */
export function isWorkflowIntent(msg: string): boolean {
  const lower = msg.toLowerCase();
  return WORKFLOW_INTENT_KEYWORDS.some((k) => lower.includes(k));
}

/** Translates internal command tokens to human-readable text for the UI. */
export function resolveCommandToDisplayLabel(msg: string): string {
  const trimmed = msg.trim();
  if (trimmed === CMD_SIGN_IN) return 'Sign In';
  if (trimmed === CMD_SIGN_UP) return 'Sign Up';
  
  const chip = ALL_ADMIN_SUGGESTION_CHIPS.find(c => c.prompt === trimmed);
  if (chip) {
    // Strip emojis if desired, or return as-is
    return chip.label;
  }
  
  return msg;
}

// ── Storage keys ────────────────────────────────────────────────────────────

export const PINNED_VIEWS_KEY = 'synaptiq:pinned_views';

// ── Defaults ────────────────────────────────────────────────────────────────

export const DEFAULT_PERSONA_NAME = 'Synaptiq';

// ── Static message strings ──────────────────────────────────────────────────

export const MSG_SESSION_LOADED = 'Conversation loaded. How can I help?';
export const MSG_SESSION_LOAD_ERROR = 'Could not load the conversation. Starting fresh.';
export const MSG_AUTH_SIGNUP = '📝 Create your account below to save conversations and unlock all features.';
export const MSG_AUTH_SIGNIN = '🔐 Sign in to your account below.';
export const MSG_BACKEND_OFFLINE = 'The API server is not connected. Please start the backend to use Synaptiq.';
export const MSG_BACKEND_UNREACHABLE = 'Backend is offline. Please start the API server and try again.';
export const MSG_ANALYTICS_LOADING = '📊 Loading analytics dashboard…';
