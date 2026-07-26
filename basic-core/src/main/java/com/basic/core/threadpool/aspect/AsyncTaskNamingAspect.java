package com.basic.core.threadpool.aspect;

import com.basic.core.threadpool.context.TaskNamingContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;

/**
 * 在 Spring 原生异步代理提交的任务线程内追加可读的类名和方法名。
 */
@Aspect
@Order(Ordered.LOWEST_PRECEDENCE)
public final class AsyncTaskNamingAspect {

    @Around("@annotation(org.springframework.scheduling.annotation.Async) "
            + "|| @within(org.springframework.scheduling.annotation.Async)")
    /**
     * 为 {@code @Async} 异步任务设置可读的线程和 MDC 上下文名称。
     * <p>拦截所有标注 {@code @Async} 的方法或类，提取目标类的简单类名与方法名
     * 拼接为任务名（格式：{@code ClassName.methodName}），
     * 通过 {@link TaskNamingContext#open(String)} 建立命名上下文，
     * 待目标方法执行完毕后自动恢复原线程名与 MDC 状态。
     *
     * @param joinPoint 当前被拦截的方法的连接点
     * @return 目标方法的执行结果
     * @throws Throwable 目标方法抛出的任何异常
     */
    public Object nameAsyncTask(ProceedingJoinPoint joinPoint) throws Throwable {
        Class<?> targetClass = ClassUtils.getUserClass(joinPoint.getTarget());
        Method signatureMethod =
                ((MethodSignature) joinPoint.getSignature()).getMethod();
        Method specificMethod =
                AopUtils.getMostSpecificMethod(signatureMethod, targetClass);
        String taskName =
                targetClass.getSimpleName() + "." + specificMethod.getName();

        try (TaskNamingContext.Scope ignored = TaskNamingContext.open(taskName)) {
            return joinPoint.proceed();
        }
    }
}
