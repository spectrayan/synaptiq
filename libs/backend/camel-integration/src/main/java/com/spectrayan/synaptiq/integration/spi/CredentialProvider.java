package com.spectrayan.synaptiq.integration.spi;

import reactor.core.publisher.Mono;

/**
 * SPI for resolving credentials/secrets at route execution time.
 * <p>
 * The consuming application implements this backed by GCP Secret Manager,
 * HashiCorp Vault, MongoDB-encrypted storage, or any secret provider.
 */
public interface CredentialProvider {

    /**
     * Resolve a secret by its reference identifier.
     *
     * @param secretRef the secret reference (e.g., "credential/tenant-abc/slack-webhook")
     * @return the resolved secret value
     */
    Mono<String> resolve(String secretRef);

    /**
     * Resolve an OAuth2 access token for a specific provider and tenant.
     *
     * @param provider the OAuth2 provider name (e.g., "salesforce", "google")
     * @param tenantId the tenant requesting the token
     * @return a valid access token
     */
    Mono<String> resolveOAuth2Token(String provider, String tenantId);
}
