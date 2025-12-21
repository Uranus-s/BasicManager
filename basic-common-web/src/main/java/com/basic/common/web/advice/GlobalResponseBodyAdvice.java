package com.basic.common.web.advice;

import com.basic.common.result.Result;
import com.basic.common.web.annotation.IgnoreResponseAdvice;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * 全局响应体处理切面类，用于统一包装控制器返回的数据格式
 * 该类通过实现ResponseBodyAdvice接口，在响应体写出之前进行统一处理
 */
@RestControllerAdvice(basePackages = "com.basic")
public class GlobalResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 判断当前请求是否支持响应体增强处理
     *
     * @param returnType    控制器方法的返回类型信息
     * @param converterType 消息转换器类型
     * @return true表示支持处理，false表示不支持处理
     */
    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {

        // 如果方法或类上标记了@IgnoreResponseAdvice注解，则不进行统一包装处理
        return !returnType.hasMethodAnnotation(IgnoreResponseAdvice.class)
                && !returnType.getDeclaringClass()
                .isAnnotationPresent(IgnoreResponseAdvice.class);
    }

    /**
     * 在响应体写入之前进行统一包装处理
     *
     * @param body                  原始响应体内容
     * @param returnType            控制器方法的返回类型信息
     * @param selectedContentType   选定的内容类型
     * @param selectedConverterType 选定的消息转换器类型
     * @param request               HTTP请求对象
     * @param response              HTTP响应对象
     * @return 处理后的响应体内容
     */
    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {

        // 只处理JSON格式的响应
        if (!MediaType.APPLICATION_JSON.includes(selectedContentType)) {
            return body;
        }

        // 如果响应体为空，返回成功的空结果
        if (body == null) {
            return Result.success(null);
        }

        // 如果已经是统一结果格式，直接返回
        if (body instanceof Result) {
            return body;
        }

        // ResponseEntity和Resource类型不进行包装处理
        if (body instanceof ResponseEntity || body instanceof Resource) {
            return body;
        }

        // 字符串类型需要特殊处理，避免类型转换问题
        if (body instanceof String) {
            try {
                return objectMapper.writeValueAsString(Result.success(body));
//            } catch (JsonProcessingException e) {
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // 其他类型统一包装成成功结果
        return Result.success(body);
    }
}

