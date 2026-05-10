package com.spectrayan.synaptiq.integration.adapter.rest;

import com.spectrayan.synaptiq.integration.adapter.RouteAdapter;
import com.spectrayan.synaptiq.integration.model.ConnectionTestResult;
import com.spectrayan.synaptiq.integration.model.RouteConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Validates and tests connectivity for REST API integrations.
 * <p>
 * Route construction is handled by the {@code rest-api-poll} Camel RouteTemplate
 * defined in {@link com.spectrayan.synaptiq.integration.template.IntegrationRouteTemplates}.
 */
@Slf4j
@Component
public class RestRouteAdapter implements RouteAdapter {

    @Override
    public String supportedType() {
        return "REST_API";
    }

    @Override
    public Mono<Void> validateConfig(RouteConfig config) {
        return Mono.fromCallable(() -> {
            String url = config.getParameters().get("url");
            if (url == null || url.isBlank()) {
                throw new IllegalArgumentException("Parameter 'url' is required for REST_API connector");
            }
            try {
                URI.create(url);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid URL: " + url);
            }
            return true;
        }).then();
    }

    @Override
    public Mono<ConnectionTestResult> testConnection(RouteConfig config) {
        return Mono.fromCallable(() -> {
            long start = System.currentTimeMillis();
            String url = config.getParameters().get("url");
            try {
                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .build();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .method("HEAD", HttpRequest.BodyPublishers.noBody())
                        .timeout(Duration.ofSeconds(10))
                        .build();
                HttpResponse<Void> response = client.send(request,
                        HttpResponse.BodyHandlers.discarding());
                long duration = System.currentTimeMillis() - start;

                if (response.statusCode() < 400) {
                    return ConnectionTestResult.success(
                            "HTTP " + response.statusCode() + " OK", duration);
                } else {
                    return ConnectionTestResult.failure(
                            "HTTP " + response.statusCode(), duration);
                }
            } catch (Exception e) {
                return ConnectionTestResult.failure(
                        e.getMessage(), System.currentTimeMillis() - start);
            }
        });
    }
}
