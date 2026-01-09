package com.basic.core.redis.aspect;

import com.basic.core.redis.annotation.RedisCache;
import com.basic.core.redis.utils.RedisUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * Redis缓存切面类，用于处理Redis缓存的AOP操作
 * 支持普通缓存和Hash缓存两种模式
 */
@Aspect
@Component
public class RedisCacheAspect {

    private final RedisUtils redisUtils;
    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    /**
     * 构造函数
     *
     * @param redisUtils Redis工具类实例
     */
    public RedisCacheAspect(RedisUtils redisUtils) {
        this.redisUtils = redisUtils;
    }

    /**
     * 环绕通知方法，处理Redis缓存逻辑
     *
     * @param joinPoint  连接点对象
     * @param redisCache Redis缓存注解对象
     * @return 方法执行结果
     * @throws Throwable 异常
     */
    @Around("@annotation(redisCache)")
    public Object around(ProceedingJoinPoint joinPoint, RedisCache redisCache) throws Throwable {
        Object[] args = joinPoint.getArgs();

        // 解析 key，支持 SpEL
        String key = parseSpel(redisCache.key(), joinPoint, args);

        if (redisCache.hash()) {
            // Hash 模式
            String field = parseSpel(redisCache.hashField(), joinPoint, args);
            Object cachedValue = redisUtils.hget(key, field);
            if (cachedValue != null) {
                return cachedValue;
            }

            Object result = joinPoint.proceed();

            if (result != null || redisCache.cacheNull()) {
                redisUtils.hset(key, field, result);
                redisUtils.expire(key, redisCache.expire(), TimeUnit.SECONDS);
            }

            return result;
        } else {
            // 普通模式
            Object cachedValue = redisUtils.get(key);
            if (cachedValue != null) {
                return cachedValue;
            }

            Object result = joinPoint.proceed();

            if (result != null || redisCache.cacheNull()) {
                redisUtils.set(key, result, redisCache.expire(), TimeUnit.SECONDS);
            }

            return result;
        }
    }

    /**
     * 解析 SpEL 表达式
     *
     * @param spel      SpEL表达式字符串
     * @param joinPoint 连接点对象
     * @param args      方法参数数组
     * @return 解析后的字符串结果
     */
    private String parseSpel(String spel, ProceedingJoinPoint joinPoint, Object[] args) {
        if (!StringUtils.hasText(spel)) {
            return "";
        }

        MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(joinPoint.getTarget(), ((org.aspectj.lang.reflect.MethodSignature) joinPoint.getSignature()).getMethod(), args, nameDiscoverer);

        StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
        evaluationContext.setRootObject(context);

        return parser.parseExpression(spel).getValue(context, String.class);
    }
}
