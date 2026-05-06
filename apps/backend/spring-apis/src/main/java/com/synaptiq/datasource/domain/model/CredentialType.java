package com.synaptiq.datasource.domain.model;

/**
 * Credential storage strategy — pluggable between cloud providers,
 * external vaults, MongoDB-encrypted storage, or OAuth2 flows.
 */
public enum CredentialType {
    /** Google Cloud Platform Secret Manager. */
    GCP_SECRET_MANAGER,
    /** External secret manager (HashiCorp Vault, AWS Secrets Manager, etc.). */
    EXTERNAL_SECRET_MANAGER,
    /** AES-256 encrypted credential stored in MongoDB. */
    MONGODB_ENCRYPTED,
    /** OAuth2 token flow (Salesforce, Google, Microsoft, etc.). */
    OAUTH2
}
