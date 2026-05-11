package com.zhm.springboot.userservice.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis缓存配置
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 默认缓存配置（5分钟）
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues()
                .prefixCacheNameWith("nft:");

        // 不同缓存的TTL配置
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // 用户信息缓存 - 30分钟
        cacheConfigurations.put("user", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // NFT详情缓存 - 10分钟
        cacheConfigurations.put("nft", defaultConfig.entryTtl(Duration.ofMinutes(10)));

        // NFT列表缓存 - 5分钟
        cacheConfigurations.put("nftList", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // 市场订单缓存 - 2分钟
        cacheConfigurations.put("marketOrder", defaultConfig.entryTtl(Duration.ofMinutes(2)));

        // Gas价格缓存 - 30秒
        cacheConfigurations.put("gasPrice", defaultConfig.entryTtl(Duration.ofSeconds(30)));

        // 粉丝代币缓存 - 5分钟
        cacheConfigurations.put("fanToken", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
}