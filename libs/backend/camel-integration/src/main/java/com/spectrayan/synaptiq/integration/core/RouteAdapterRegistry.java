package com.spectrayan.synaptiq.integration.core;

import com.spectrayan.synaptiq.integration.adapter.RouteAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Registry of all available {@link RouteAdapter} implementations.
 * <p>
 * Uses the strategy pattern to look up the correct adapter
 * for a given connector type string. Adapters are optional —
 * if none is registered for a type, the engine proceeds without
 * specialized validation/testing.
 */
@Slf4j
@Component
public class RouteAdapterRegistry {

    private final Map<String, RouteAdapter> adapters;

    public RouteAdapterRegistry(List<RouteAdapter> adapterList) {
        this.adapters = adapterList.stream()
                .collect(Collectors.toMap(RouteAdapter::supportedType, Function.identity()));
        log.info("Registered {} route adapters: {}", adapters.size(), adapters.keySet());
    }

    /**
     * Find the adapter for a given connector type.
     */
    public Optional<RouteAdapter> findAdapter(String connectorType) {
        return Optional.ofNullable(adapters.get(connectorType));
    }

    /**
     * Get the adapter or throw if not found.
     */
    public RouteAdapter getAdapter(String connectorType) {
        return findAdapter(connectorType)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No route adapter registered for connector type: " + connectorType));
    }

    /**
     * List all registered connector types.
     */
    public List<String> registeredTypes() {
        return List.copyOf(adapters.keySet());
    }
}
