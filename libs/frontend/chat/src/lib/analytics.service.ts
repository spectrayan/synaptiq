/**
 * AnalyticsService — re-exports from the generated @synaptiq/client SDK.
 *
 * The hand-coded service has been replaced by the OpenAPI-generated
 * AnalyticsService from @synaptiq/client. This barrel re-exports the
 * SDK service and model types so existing consumers don't need to change
 * their import paths.
 *
 * The auth interceptor automatically attaches Authorization and X-Tenant-ID
 * headers, so SDK callers don't need to manage tokens manually.
 */
export {
  AnalyticsService,
  type AnalyticsSummaryResponse as AnalyticsSummary,
  type TokenUsageResponse as TokenUsageSummary,
  type BillingResponse as BillingReport,
  type PlatformRollupResponse as PlatformRollup,
} from '@synaptiq/client';
