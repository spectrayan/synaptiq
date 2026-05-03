// ---------------------------------------------------------------------------
// ChatAnalyticsService — analytics dashboard builder
// ---------------------------------------------------------------------------

import { Injectable, inject } from '@angular/core';
import { type ComponentSpec } from '@synaptiq/constants';
import {
  AnalyticsService,
  type AnalyticsSummary,
  type TokenUsageSummary,
} from '@synaptiq/chat';
import { ChatMessage } from '../chat-message.model';
import {
  CMD_ANALYTICS_DASHBOARD,
  CMD_CONFIG_PERSONA,
  CMD_CONFIG_PROVIDER,
  MSG_ANALYTICS_LOADING,
} from '../chat-shell.constants';

/** Bundled analytics data attached to a chat message. */
export interface AnalyticsData {
  summary: AnalyticsSummary;
  tokens: TokenUsageSummary;
}

export interface AnalyticsDashboardCallbacks {
  updateMessages: (fn: (msgs: ChatMessage[]) => ChatMessage[]) => void;
  appendMessages: (msgs: ChatMessage[]) => void;
}

@Injectable({ providedIn: 'root' })
export class ChatAnalyticsOrchestrator {
  private readonly analyticsService = inject(AnalyticsService);

  /** Build DSL components from raw analytics data. */
  buildDashboardComponents(summary: AnalyticsSummary, tokens: TokenUsageSummary): ComponentSpec[] {
    const components: ComponentSpec[] = [
      {
        type: 'info_banner' as const,
        title: '📊 Usage Overview (Last 30 Days)',
        body: [
          `**${summary.total_conversations}** conversations · **${summary.total_messages}** messages`,
          `**${summary.unique_users}** unique users · **${summary.avg_messages_per_session}** avg msgs/session`,
          `**${summary.total_actions}** actions performed`,
        ].join('\n'),
        style: 'info' as const,
        suggestions: [
          { label: '🔄 Refresh', prompt: CMD_ANALYTICS_DASHBOARD },
          { label: '🤖 AI Config', prompt: CMD_CONFIG_PERSONA },
          { label: '🔧 Provider', prompt: CMD_CONFIG_PROVIDER },
        ],
      },
      {
        type: 'comparison_table' as const,
        items: [
          {
            item_id: 'token-usage',
            data: {
              'Input Tokens': tokens.total_tokens_input.toLocaleString(),
              'Output Tokens': tokens.total_tokens_output.toLocaleString(),
              'Total Tokens': tokens.total_tokens.toLocaleString(),
              'Est. Cost': `$${tokens.estimated_cost_usd.toFixed(4)}`,
              'Plan Limit': tokens.plan_token_limit ? tokens.plan_token_limit.toLocaleString() : 'Unlimited',
              'Usage': tokens.plan_token_limit ? `${tokens.usage_percent}%` : 'N/A',
            },
          },
        ],
        fields: ['Input Tokens', 'Output Tokens', 'Total Tokens', 'Est. Cost', 'Plan Limit', 'Usage'],
        suggestions: [],
      },
    ];

    // Add action breakdown if present
    if (Object.keys(summary.action_rates).length > 0) {
      components.push({
        type: 'comparison_table' as const,
        items: Object.entries(summary.action_rates).map(([action, count]) => ({
          item_id: action,
          data: { Action: action, Count: count },
        })),
        fields: ['Action', 'Count'],
        suggestions: [],
      });
    }

    return components;
  }

  /** Open the analytics dashboard inline in the chat stream. */
  async openDashboard(callbacks: AnalyticsDashboardCallbacks): Promise<void> {
    const msgId = crypto.randomUUID();

    // Show loading state
    callbacks.appendMessages([
      {
        id: msgId,
        role: 'assistant',
        content: MSG_ANALYTICS_LOADING,
        timestamp: new Date(),
      },
    ]);

    try {
      const [summary, tokens] = await Promise.all([
        this.analyticsService.getSummary(),
        this.analyticsService.getTokenUsage(),
      ]);

      const analyticsComponents = this.buildDashboardComponents(summary, tokens);

      callbacks.updateMessages((msgs) =>
        msgs.map((m) =>
          m.id === msgId
            ? {
                ...m,
                content: '📊 **Analytics Dashboard**',
                analyticsData: { summary, tokens },
                uiComponents: analyticsComponents,
              }
            : m,
        ),
      );
    } catch (err: unknown) {
      const errorMsg = err instanceof Error ? err.message : 'Failed to load analytics.';
      callbacks.updateMessages((msgs) =>
        msgs.map((m) =>
          m.id === msgId
            ? {
                ...m,
                content: '',
                uiComponents: [
                  {
                    type: 'info_banner' as const,
                    title: 'Analytics Error',
                    body: errorMsg,
                    style: 'error' as const,
                    suggestions: [{ label: 'Retry', prompt: CMD_ANALYTICS_DASHBOARD }],
                  },
                ],
              }
            : m,
        ),
      );
    }
  }
}
