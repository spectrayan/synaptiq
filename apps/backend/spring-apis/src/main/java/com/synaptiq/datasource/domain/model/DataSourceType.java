package com.synaptiq.datasource.domain.model;

/**
 * Supported data source connection types.
 */
public enum DataSourceType {
    /** Data stored in Synaptiq's own tenant MongoDB (today's catalog_items). */
    SYNAPTIQ_NATIVE,
    /** External MCP server (e.g., Salesforce MCP, Slack MCP). */
    MCP_SERVER,
    /** External REST API (webhook/HTTP integration). */
    REST_API,
    /** Direct connection to an external database. */
    DATABASE_DIRECT,
    /** One-time file upload (CSV, JSON, Excel). */
    FILE_UPLOAD
}
