package com.basic.common.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，用于标记需要忽略响应处理增强的类或方法
 *
 * 该注解可以应用于类级别或方法级别，被标记的元素将不会受到
 * 全局响应处理增强机制的影响，允许其返回原始的响应数据格式
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreResponseAdvice {
}
