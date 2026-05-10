package com.spectrayan.synaptiq.shared.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

/**
 * Redis-backed cache configuration.
 * <p>
 * Activated when {@code synaptiq.cache.type=redis} is set.
 * Uses String serialization for keys and the default JDK serializer for values.
 * <p>
 * If Redis is unavailable at startup, Spring will fail to create the
 * {@link RedisConnectionFactory} bean and the application will fall back
 * to the Caffeine config (which has {@code matchIfMissing = true}).
 * <p>
 * To enable: set these environment variables:
 * <pre>
 *   SYNAPTIQ_CACHE_TYPE=redis
 *   SPRING_DATA_REDIS_HOST=localhost
 *   SPRING_DATA_REDIS_PORT=6379
 * </pre>
 *
 * @see CacheConfig
 * @see CacheNames
 */
@Slf4j
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "synaptiq.cache.type", havingValue = "redis")
public class RedisCacheConfig {

    @Bean
    @Primary
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        log.info("🔧 Initializing Redis cache manager");

        var defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .entryTtl(Duration.ofMinutes(15))
            .disableCachingNullValues();

        var cacheConfigs = Map.of(
            CacheNames.TENANTS,
                defaultConfig.entryTtl(Duration.ofMinutes(30)),
            CacheNames.APPLICATIONS,
                defaultConfig.entryTtl(Duration.ofMinutes(15)),
            CacheNames.APPLICATIONS_BY_TENANT,
                defaultConfig.entryTtl(Duration.ofMinutes(10)),
            CacheNames.DEFAULT_APPLICATION,
                defaultConfig.entryTtl(Duration.ofMinutes(15)),
            CacheNames.SCHEMAS,
                defaultConfig.entryTtl(Duration.ofHours(1)),
            CacheNames.ROLES,
                defaultConfig.entryTtl(Duration.ofHours(1))
        );

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigs)
            .build();
    }
}
