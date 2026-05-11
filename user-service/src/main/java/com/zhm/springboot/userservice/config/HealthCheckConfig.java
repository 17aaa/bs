package com.zhm.springboot.userservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.Request;

import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;

/**
 * 健康检查配置
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class HealthCheckConfig {

    private final DataSource dataSource;
    private final RedisTemplate<String, String> redisTemplate;
    private final Web3j web3j;

    /**
     * 数据库健康检查
     */
    @Configuration
    class DatabaseHealthIndicator implements HealthIndicator {

        @Override
        public Health health() {
            try {
                JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
                jdbcTemplate.queryForObject("SELECT 1", Integer.class);
                return Health.up().withDetail("database", "MySQL is running").build();
            } catch (Exception e) {
                log.error("Database health check failed", e);
                return Health.down().withDetail("database", "MySQL is down: " + e.getMessage()).build();
            }
        }
    }

    /**
     * Redis 健康检查
     */
    @Configuration
    class RedisHealthIndicator implements HealthIndicator {

        @Override
        public Health health() {
            try {
                redisTemplate.opsForValue().get("health:check");
                return Health.up().withDetail("redis", "Redis is running").build();
            } catch (Exception e) {
                log.error("Redis health check failed", e);
                return Health.down().withDetail("redis", "Redis is down: " + e.getMessage()).build();
            }
        }
    }

    /**
     * 区块链节点健康检查
     */
    @Configuration
    class BlockchainHealthIndicator implements HealthIndicator {

        @Override
        public Health health() {
            try {
                var response = web3j.web3ClientVersion().sendAsync().get(5, TimeUnit.SECONDS);
                if (response.hasError()) {
                    return Health.down().withDetail("blockchain", "Node error: " + response.getError().getMessage()).build();
                }
                return Health.up()
                        .withDetail("blockchain", "Node is connected")
                        .withDetail("clientVersion", response.getWeb3ClientVersion())
                        .build();
            } catch (Exception e) {
                log.error("Blockchain health check failed", e);
                return Health.down().withDetail("blockchain", "Node is down: " + e.getMessage()).build();
            }
        }
    }
}
