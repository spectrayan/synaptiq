package com.spectrayan.synaptiq.integration.stub;

import com.spectrayan.synaptiq.integration.model.RouteConfig;
import com.spectrayan.synaptiq.integration.model.RouteStatus;
import com.spectrayan.synaptiq.integration.spi.RouteConfigProvider;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory {@link RouteConfigProvider} for testing.
 * Stores route configs in a ConcurrentHashMap — no database required.
 */
public class InMemoryRouteConfigProvider implements RouteConfigProvider {

    private final Map<String, RouteConfig> store = new ConcurrentHashMap<>();

    public void clear() {
        store.clear();
    }

    public int size() {
        return store.size();
    }

    @Override
    public Mono<RouteConfig> findByRouteConfigId(String routeConfigId) {
        RouteConfig config = store.get(routeConfigId);
        return config != null ? Mono.just(config) : Mono.empty();
    }

    @Override
    public Flux<RouteConfig> findActiveByTenantId(String tenantId) {
        return Flux.fromIterable(store.values())
                .filter(c -> tenantId.equals(c.getTenantId()))
                .filter(c -> c.getStatus() == RouteStatus.ACTIVE);
    }

    @Override
    public Flux<RouteConfig> findAllByTenantId(String tenantId) {
        return Flux.fromIterable(store.values())
                .filter(c -> tenantId.equals(c.getTenantId()));
    }

    @Override
    public Flux<RouteConfig> findAllActive() {
        return Flux.fromIterable(store.values())
                .filter(c -> c.getStatus() == RouteStatus.ACTIVE);
    }

    @Override
    public Mono<RouteConfig> save(RouteConfig config) {
        store.put(config.getRouteConfigId(), config);
        return Mono.just(config);
    }

    @Override
    public Mono<Void> deleteByRouteConfigId(String routeConfigId) {
        store.remove(routeConfigId);
        return Mono.empty();
    }

    @Override
    public Mono<RouteConfig> updateStatus(String routeConfigId, RouteStatus status, String errorMessage) {
        RouteConfig config = store.get(routeConfigId);
        if (config == null) {
            return Mono.empty();
        }
        config.setStatus(status);
        config.setLastError(errorMessage);
        return Mono.just(config);
    }
}
