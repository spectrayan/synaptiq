/**
 * AnalyticsService — HTTP client for the Analytics API (Phase 12).
 *
 * Provides typed access to:
 *   - Analytics Summary (T12.2)
 *   - Token Usage (T12.4)
 *   - Billing Report (T12.5)
 *   - Platform Rollup (T12.6)
 */
import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ENVIRONMENT } from '@synaptiq/utils';

// ---------------------------------------------------------------------------
// Interfaces — mirror the backend Pydantic models
// ---------------------------------------------------------------------------

export interface DailyMetric {
  date: string; // YYYY-MM-DD
  value: number;
}

export interface TopItem {
  item_id: string;
  name: string;
  query_count: number;
}

export interface TopIntent {
  intent: string;
  count: number;
}

export interface AnalyticsSummary {
  total_conversations: number;
  total_messages: number;
  total_tokens_input: number;
  total_tokens_output: number;
  total_actions: number;
  unique_users: number;
  avg_messages_per_session: number;
  daily_conversations: DailyMetric[];
  daily_messages: DailyMetric[];
  top_queried_items: TopItem[];
  top_intents: TopIntent[];
  zero_result_queries: string[];
  action_rates: Record<string, number>;
}

export interface TokenUsageSummary {
  total_tokens_input: number;
  total_tokens_output: number;
  total_tokens: number;
  estimated_cost_usd: number;
  plan_token_limit: number;
  usage_percent: number;
}

export interface BillingReport {
  seat_count: number;
  total_tokens: number;
  estimated_cost_usd: number;
  by_provider: Record<string, {
    tokens_input: number;
    tokens_output: number;
    total_tokens: number;
    estimated_cost_usd: number;
    requests: number;
  }>;
}

export interface PlatformRollup {
  total_tenants: number;
  active_tenants: number;
  total_conversations: number;
  total_messages: number;
  total_tokens: number;
  total_estimated_cost_usd: number;
  byok_tenants: number;
  platform_managed_tenants: number;
  per_tenant: Array<{
    tenant_id: string;
    conversations: number;
    messages: number;
    tokens: number;
    estimated_cost_usd: number;
    is_byok: boolean;
  }>;
}

// ---------------------------------------------------------------------------
// Service
// ---------------------------------------------------------------------------

@Injectable({ providedIn: 'root' })
export class AnalyticsService {
  private readonly http = inject(HttpClient);
  private readonly env = inject(ENVIRONMENT);
  private readonly baseUrl = `${this.env.apiBaseUrl}/api/v1/analytics`;

  // ── Analytics Summary (T12.2) ─────────────────────────────────────

  async getSummary(from?: string, to?: string): Promise<AnalyticsSummary> {
    let params = new HttpParams();
    if (from) params = params.set('from', from);
    if (to) params = params.set('to', to);

    return firstValueFrom(
      this.http.get<AnalyticsSummary>(`${this.baseUrl}/summary`, { params }),
    );
  }

  // ── Token Usage (T12.4) ───────────────────────────────────────────

  async getTokenUsage(from?: string, to?: string): Promise<TokenUsageSummary> {
    let params = new HttpParams();
    if (from) params = params.set('from', from);
    if (to) params = params.set('to', to);

    return firstValueFrom(
      this.http.get<TokenUsageSummary>(`${this.baseUrl}/tokens`, { params }),
    );
  }

  // ── Billing Report (T12.5) ────────────────────────────────────────

  async getBillingReport(from?: string, to?: string): Promise<BillingReport> {
    let params = new HttpParams();
    if (from) params = params.set('from', from);
    if (to) params = params.set('to', to);

    return firstValueFrom(
      this.http.get<BillingReport>(`${this.baseUrl}/billing`, { params }),
    );
  }

  // ── Platform Rollup (T12.6) ───────────────────────────────────────

  async getPlatformRollup(from?: string, to?: string): Promise<PlatformRollup> {
    let params = new HttpParams();
    if (from) params = params.set('from', from);
    if (to) params = params.set('to', to);

    return firstValueFrom(
      this.http.get<PlatformRollup>(`${this.baseUrl}/platform`, { params }),
    );
  }
}
