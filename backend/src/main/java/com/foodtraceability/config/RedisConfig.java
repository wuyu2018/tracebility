package com.foodtraceability.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置
 */
@Configuration
public class RedisConfig {

    /**
     * 配置用于存储登录失败次数的 RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Integer> failedAttemptsTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Integer> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new org.springframework.data.redis.serializer.GenericToStringSerializer<>(Integer.class));
        return template;
    }

    /**
     * 配置用于存储锁定账号的 RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Long> lockedAccountsTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Long> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new org.springframework.data.redis.serializer.GenericToStringSerializer<>(Long.class));
        return template;
    }
}
