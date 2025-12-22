package com.basic.common.validate.annotation;

import com.basic.common.validate.group.MobileValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = MobileValidator.class)
public @interface Mobile {

    /**
     * 是否必填
     */
    boolean required() default true;

    /**
     * 校验失败提示信息
     */
    String message() default "不是一个合法的手机号码";

    /**
     * 分组校验（JSR-380 规范要求）
     */
    Class<?>[] groups() default {};

    /**
     * 负载信息（JSR-380 规范要求）
     */
    Class<? extends Payload>[] payload() default {};
}
