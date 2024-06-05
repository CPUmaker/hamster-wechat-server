package com.hamsterwhat.wechat.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    private static final Logger logger = LoggerFactory.getLogger(RedissonConfig.class);

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private String port;

    @Value("${spring.data.redis.password}")
    private String password;

    @Bean
    public RedissonClient redissonClient() {
        try {
            Config config = new Config();
            config.useSingleServer()
                    .setAddress("redis://" + host + ":" + port)
                    .setPassword(password);
            return Redisson.create(config);
        } catch (Exception e) {
            logger.info("Failed to create redisson client", e);
            return null;
        }
    }
}
