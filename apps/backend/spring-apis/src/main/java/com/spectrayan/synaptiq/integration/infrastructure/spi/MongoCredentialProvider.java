package com.spectrayan.synaptiq.integration.infrastructure.spi;

import com.spectrayan.synaptiq.integration.spi.CredentialProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Simple credential provider implementation.
 * <p>
 * For now, returns the secret ref as-is (suitable for dev/testing).
 * In production, integrate with GCP Secret Manager or HashiCorp Vault.
 */
@Slf4j
@Component
public class MongoCredentialProvider implements CredentialProvider {

    @Override
    public Mono<String> resolve(String secretRef) {
        // TODO: Integrate with actual secret manager (GCP, Vault, MongoDB-encrypted)
        log.debug("Resolving credential: {}", secretRef);
        return Mono.justOrEmpty(secretRef);
    }

    @Override
    public Mono<String> resolveOAuth2Token(String provider, String tenantId) {
        // TODO: Implement OAuth2 token flow
        log.warn("OAuth2 token resolution not yet implemented for provider: {}", provider);
        return Mono.error(new UnsupportedOperationException(
                "OAuth2 token resolution not yet implemented for: " + provider));
    }
}
