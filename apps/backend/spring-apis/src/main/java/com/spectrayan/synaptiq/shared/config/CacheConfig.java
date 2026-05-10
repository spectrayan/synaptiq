package com.spectrayan.synaptiq.shared.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;
import java.util.List;

/**
 * Cache configuration for Synaptiq.
 * <p>
 * <b>Default mode:</b> Caffeine in-memory cache (zero-config, no external dependency).
 * <p>
 * <b>Redis mode:</b> When {@code synaptiq.cache.type=redis} and a Redis connection
 * is available, the {@link RedisCacheConfig} activates instead.
 * <p>
 * Spring Boot 4 / Framework 7 natively supports {@code @Cacheable} on methods
 * returning {@code Mono<T>} and {@code Flux<T>} — the framework subscribes,
 * caches the materialized value, and rewraps on cache hit. No manual
 * {@code CacheMono} plumbing needed.
 *
 * @see CacheNames
 */
@Slf4j
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "synaptiq.cache.type", havingValue = "caffeine", matchIfMissing = true)
public class CacheConfig {

    @Bean
    @Primary
    public CacheManager cacheManager() {
        log.info("🔧 Initializing Caffeine in-memory cache manager");

        var manager = new SimpleCacheManager();
        manager.setCaches(List.of(
            buildCache(CacheNames.TENANTS, Duration.ofMinutes(30), 200),
            buildCache(CacheNames.APPLICATIONS, Duration.ofMinutes(15), 500),
            buildCache(CacheNames.APPLICATIONS_BY_TENANT, Duration.ofMinutes(10), 200),
            buildCache(CacheNames.DEFAULT_APPLICATION, Duration.ofMinutes(15), 200),
            buildCache(CacheNames.SCHEMAS, Duration.ofHours(1), 100),
            buildCache(CacheNames.ROLES, Duration.ofHours(1), 100)
        ));

        return manager;
    }

    private CaffeineCache buildCache(String name, Duration ttl, int maxSize) {
        return new CaffeineCache(name,
            Caffeine.newBuilder()
                .expireAfterWrite(ttl)
                .maximumSize(maxSize)
                .recordStats()
                .build());
    }
}
