// ---------------------------------------------------------------------------
// WelcomeMessageBuilder — pure factory for role-based welcome messages
// ---------------------------------------------------------------------------

import { ChatMessage } from './chat-message.model';
import {
  CMD_SIGN_IN,
  CMD_SIGN_UP,
  DEFAULT_PERSONA_NAME,
  ADMIN_WELCOME_SUGGESTIONS,
} from './chat-shell.constants';

/** Context required to build the initial welcome message. */
export interface WelcomeMessageContext {
  isLoggedIn: boolean;
  isAdmin: boolean;
  isGuest: boolean;
  displayName?: string | null;
  email?: string | null;
  personaName: string;
  personaWelcome: string;
  personaStarters: string[];
}

/**
 * Build the welcome message(s) based on the user's auth role and persona config.
 *
 * This is a **pure function** — no side effects, no service dependencies.
 * The component passes in auth state + persona config and gets back ready-to-render messages.
 */
export function buildWelcomeMessages(ctx: WelcomeMessageContext): ChatMessage[] {
  const name = ctx.personaName || DEFAULT_PERSONA_NAME;

  // Build starter prompt chips from persona config
  const starterChips: Array<{ label: string; prompt: string }> = ctx.personaStarters.map(
    (prompt) => ({ label: prompt, prompt }),
  );

  // ── Not logged in ──
  if (!ctx.isLoggedIn) {
    const welcomeText = ctx.personaWelcome
      || `Hi! 👋 I'm **${name}** — your AI-powered assistant. Sign in to save your conversations and access all features, or start exploring right away!`;
    return [
      {
        id: '1',
        role: 'assistant',
        content: welcomeText,
        timestamp: new Date(),
        uiComponents: [
          {
            type: 'info_banner',
            title: 'Quick start',
            body: 'Try the suggestions below to explore, or sign in via the topbar.',
            style: 'info',
            suggestions: [
              { label: 'Sign in', prompt: CMD_SIGN_IN },
              { label: 'Sign up', prompt: CMD_SIGN_UP },
              ...starterChips,
            ],
          },
        ],
      },
    ];
  }

  // ── Admin ──
  if (ctx.isAdmin) {
    return [
      {
        id: '1',
        role: 'assistant',
        content:
          `Welcome back${ctx.displayName ? ', ' + ctx.displayName : ''}! I'm ${name} in **admin mode**. I can help you manage your data, configure your workspace, view analytics, and more — all through this chat.\n\nWhat would you like to do?`,
        timestamp: new Date(),
        uiComponents: [
          {
            type: 'info_banner',
            title: 'Admin Actions',
            body: 'Configure your workspace, manage data, or check usage analytics.',
            style: 'info',
            suggestions: ADMIN_WELCOME_SUGGESTIONS,
          },
        ],
      },
    ];
  }

  // ── Guest ──
  if (ctx.isGuest) {
    const welcomeText = ctx.personaWelcome
      || `👋 Hi! I'm **${name}** — your AI-powered assistant. You're currently browsing as a **guest**.\n\nYou can explore and chat right away. To save your conversations and unlock admin features, sign in anytime.`;
    return [
      {
        id: '1',
        role: 'assistant',
        content: welcomeText,
        timestamp: new Date(),
        uiComponents: [
          {
            type: 'info_banner',
            title: 'Quick start',
            body: 'Try any of these to get started:',
            style: 'info',
            suggestions: [
              { label: 'Sign in', prompt: CMD_SIGN_IN },
              { label: 'Sign up', prompt: CMD_SIGN_UP },
              ...starterChips,
            ],
          },
        ],
      },
    ];
  }

  // ── Authenticated non-admin user ──
  const welcomeText = ctx.personaWelcome
    || `Hi${ctx.displayName ? ' ' + ctx.displayName : ''}! 👋 I'm ${name}. Ask me anything — I can search, analyse, and visualise your data for you.`;
  return [
    {
      id: '1',
      role: 'assistant',
      content: welcomeText,
      timestamp: new Date(),
      uiComponents: [
        {
          type: 'info_banner',
          title: 'Quick start',
          body: 'Try the suggestions below to get started.',
          style: 'info',
          suggestions: starterChips,
        },
      ],
    },
  ];
}
