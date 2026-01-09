package com.basic.core.redis.config;


import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.mode:single}")
    private String redisMode;

    @Value("${spring.data.redis.host:127.0.0.1}")
    private String host;

    @Value("${spring.data.redis.port:6379}")
    private int port;

    @Value("${spring.data.redis.password:}")
    private String password;

    @Value("${spring.data.redis.database:0}")
    private int database;

    @Autowired
    private ClusterConfigurationProperties clusterProperties;

    /**
     * RedisTemplate 配置
     * key 使用 String 序列化
     * value 使用 JSON 序列化
     *
     * @param factory Redis连接工厂
     * @return 配置好的RedisTemplate实例
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // key 用字符串
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // value 用 JacksonJsonRedisSerializer
        JacksonJsonRedisSerializer<Object> jacksonSerializer = new JacksonJsonRedisSerializer<>(Object.class);

        template.setValueSerializer(jacksonSerializer);
        template.setHashValueSerializer(jacksonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 创建Lettuce连接工厂
     *
     * @return LettuceConnectionFactory实例
     */
    @Bean
    public LettuceConnectionFactory connectionFactory() {
        if ("cluster".equalsIgnoreCase(redisMode) && clusterProperties.getNodes() != null && !clusterProperties.getNodes().isEmpty()) {
            RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration(clusterProperties.getNodes());
            if (password != null && !password.isEmpty()) {
                clusterConfig.setPassword(RedisPassword.of(password));
            }
            // 设置最大重定向次数
            clusterConfig.setMaxRedirects(clusterProperties.getMaxRedirects());

            LettuceClientConfiguration clientConfig =
                    LettuceClientConfiguration.builder()
                            .commandTimeout(Duration.ofSeconds(5))
                            .clientOptions(
                                    ClusterClientOptions.builder()
                                            .topologyRefreshOptions(
                                                    ClusterTopologyRefreshOptions.builder()
                                                            .enableAllAdaptiveRefreshTriggers()
                                                            .enablePeriodicRefresh(Duration.ofSeconds(60))
                                                            .build()
                                            )
                                            .build()
                            )
                            .build();

            return new LettuceConnectionFactory(clusterConfig, clientConfig);
        } else {
            RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration(host, port);
            standaloneConfig.setDatabase(database);
            if (password != null && !password.isEmpty()) {
                standaloneConfig.setPassword(RedisPassword.of(password));
            }
            return new LettuceConnectionFactory(standaloneConfig);
        }
    }

    /**
     * 配置Redis缓存管理器
     * 设置默认缓存配置：过期时间为5分钟，启用空闲时间过期
     *
     * @param connectionFactory Redis连接工厂
     * @return RedisCacheManager实例
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 设置默认缓存配置：过期时间为5分钟，启用空闲时间过期
        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .enableTimeToIdle();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaults)
                .build();
    }
}
