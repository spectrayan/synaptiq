package com.spectrayan.synaptiq.integration.model;

/**
 * Supported integration connector types.
 */
public enum ConnectorType {
    /** Generic HTTP/REST API call. */
    REST_API,
    /** Inbound/outbound webhook. */
    WEBHOOK,
    /** Direct database query (JDBC). */
    DATABASE,
    /** Slack messaging. */
    SLACK,
    /** Email (SMTP). */
    EMAIL,
    /** MCP server bridge. */
    MCP_SERVER,
    /** Apache Kafka / message queue. */
    MESSAGE_QUEUE,
    /** File storage (S3, GCS). */
    FILE_STORAGE,
    /** Custom Camel YAML route. */
    CUSTOM_YAML
}
