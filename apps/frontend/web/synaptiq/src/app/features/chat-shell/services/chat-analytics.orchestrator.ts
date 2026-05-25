// ---------------------------------------------------------------------------
// ChatAnalyticsOrchestrator — analytics dashboard builder (facade pattern)
// ---------------------------------------------------------------------------
// Uses the generated @synaptiq/client SDK services.
// The auth interceptor handles Authorization + X-Tenant-ID headers.

import { Injectable, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { type ComponentSpec } from '@synaptiq/constants';
import {
  AnalyticsService as AnalyticsApiService,
  type AnalyticsSummaryResponse,
  type TokenUsageResponse,
} from '@synaptiq/client';
import { ChatMessage } from '../chat-message.model';
import {
  CMD_ANALYTICS_DASHBOARD,
  CMD_CONFIG_PERSONA,
  CMD_CONFIG_PROVIDER,
  MSG_ANALYTICS_LOADING,
} from '../chat-shell.constants';

/** Bundled analytics data attached to a chat message. */
export interface AnalyticsData {
  summary: AnalyticsSummaryResponse;
  tokens: TokenUsageResponse;
}

export interface AnalyticsDashboardCallbacks {
  updateMessages: (fn: (msgs: ChatMessage[]) => ChatMessage[]) => void;
  appendMessages: (msgs: ChatMessage[]) => void;
}

@Injectable({ providedIn: 'root' })
export class ChatAnalyticsOrchestrator {
  private readonly api = inject(AnalyticsApiService);

  /** Build DSL components from raw analytics data. */
  buildDashboardComponents(summary: AnalyticsSummaryResponse, tokens: TokenUsageResponse): ComponentSpec[] {
    const components: ComponentSpec[] = [
      {
        type: 'info_banner' as const,
        title: '📊 Usage Overview (Last 30 Days)',
        body: [
          `**${summary.totalConversations ?? 0}** conversations · **${summary.totalMessages ?? 0}** messages`,
          `**${summary.uniqueUsers ?? 0}** unique users · **${summary.avgMessagesPerSession ?? 0}** avg msgs/session`,
          `**${summary.totalActions ?? 0}** actions performed`,
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
              'Input Tokens': (tokens.totalTokensInput ?? 0).toLocaleString(),
              'Output Tokens': (tokens.totalTokensOutput ?? 0).toLocaleString(),
              'Total Tokens': (tokens.totalTokens ?? 0).toLocaleString(),
              'Est. Cost': `$${(tokens.estimatedCostUsd ?? 0).toFixed(4)}`,
              'Plan Limit': tokens.planTokenLimit ? tokens.planTokenLimit.toLocaleString() : 'Unlimited',
              'Usage': tokens.planTokenLimit ? `${tokens.usagePercent}%` : 'N/A',
            },
          },
        ],
        fields: ['Input Tokens', 'Output Tokens', 'Total Tokens', 'Est. Cost', 'Plan Limit', 'Usage'],
        suggestions: [],
      },
    ];

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
        firstValueFrom(this.api.getAnalyticsSummary()),
        firstValueFrom(this.api.getTokenUsage()),
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
