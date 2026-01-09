package com.basic.core.redis.annotation;

import java.lang.annotation.*;

/**
 * Redis 自动缓存注解
 * <p>
 * 使用方式：
 *
 * @RedisCache(key = "user:", expire = 60)
 * public User getUser(Long id)
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisCache {

    /**
     * key 支持 SpEL 表达式，例如 "user:#id"
     */
    String key() default "";

    /**
     * 过期时间，单位秒
     */
    long expire() default 3600;

    /**
     * 是否缓存 null 值，避免缓存穿透
     */
    boolean cacheNull() default true;

    /**
     * 是否使用 Hash 存储
     */
    boolean hash() default false;

    /**
     * hashField 支持 SpEL 表达式，例如 "#user.id"
     */
    String hashField() default "";
}