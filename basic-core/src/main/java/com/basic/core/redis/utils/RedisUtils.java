package com.basic.core.redis.utils;

import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtils {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisUtils(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // ==================== String / Object ====================

    /**
     * 普通 set
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 带过期时间 set
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * get
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除 key
     */
    public boolean delete(String key) {
        Boolean result = redisTemplate.delete(key);
        return result != null && result;
    }

    /**
     * 判断 key 是否存在
     */
    public boolean hasKey(String key) {
        Boolean result = redisTemplate.hasKey(key);
        return result != null && result;
    }

    /**
     * 根据模式查询 key
     */
    public Set<String> keys(String pattern) {
        Set<String> keys = new LinkedHashSet<>();
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(1000).build();
        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            cursor.forEachRemaining(keys::add);
        }
        return keys;
    }

    /**
     * 设置过期时间
     */
    public boolean expire(String key, long timeout, TimeUnit unit) {
        Boolean result = redisTemplate.expire(key, timeout, unit);
        return result != null && result;
    }

    /**
     * 获取剩余过期时间
     */
    public long getExpire(String key, TimeUnit unit) {
        Long expire = redisTemplate.getExpire(key, unit);
        return expire == null ? -1 : expire;
    }

    // ==================== Hash ====================

    /**
     * Hash set
     */
    public void hset(String key, String hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    /**
     * Hash get
     */
    public Object hget(String key, String hashKey) {
        return redisTemplate.opsForHash().get(key, hashKey);
    }

    /**
     * Hash delete
     */
    public boolean hdel(String key, String hashKey) {
        Long result = redisTemplate.opsForHash().delete(key, hashKey);
        return result != null && result > 0;
    }
}
