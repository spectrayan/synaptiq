package com.spectrayan.synaptiq.integration.stub;

import com.spectrayan.synaptiq.integration.spi.CredentialProvider;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory {@link CredentialProvider} for testing.
 */
public class InMemoryCredentialProvider implements CredentialProvider {

    private final Map<String, String> secrets = new ConcurrentHashMap<>();
    private final Map<String, String> oauthTokens = new ConcurrentHashMap<>();

    public void addSecret(String secretRef, String value) {
        secrets.put(secretRef, value);
    }

    public void addOAuthToken(String key, String token) {
        oauthTokens.put(key, token);
    }

    public void clear() {
        secrets.clear();
        oauthTokens.clear();
    }

    @Override
    public Mono<String> resolve(String secretRef) {
        String value = secrets.get(secretRef);
        return value != null ? Mono.just(value)
                : Mono.error(new IllegalArgumentException("Secret not found: " + secretRef));
    }

    @Override
    public Mono<String> resolveOAuth2Token(String provider, String tenantId) {
        String key = provider + ":" + tenantId;
        String token = oauthTokens.get(key);
        return token != null ? Mono.just(token)
                : Mono.error(new IllegalArgumentException("OAuth2 token not found: " + key));
    }
}
