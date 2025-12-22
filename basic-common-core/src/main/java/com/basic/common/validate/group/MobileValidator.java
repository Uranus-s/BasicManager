package com.basic.common.validate.group;

import com.basic.common.validate.annotation.Mobile;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

public class MobileValidator implements ConstraintValidator<Mobile, CharSequence> {

    /**
     * 是否必填
     */
    private boolean required;

    /**
     * 中国大陆手机号校验
     */
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    @Override
    public void initialize(Mobile annotation) {
        this.required = annotation.required();
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {

        // 必填校验
        if (required) {
            return isMobile(value);
        }

        // 非必填：有值才校验
        if (StringUtils.hasText(value)) {
            return isMobile(value);
        }

        // 非必填 + 无值
        return true;
    }

    /**
     * 手机号格式校验
     */
    private boolean isMobile(CharSequence value) {
        if (value == null) {
            return false;
        }
        return MOBILE_PATTERN.matcher(value).matches();
    }
}
