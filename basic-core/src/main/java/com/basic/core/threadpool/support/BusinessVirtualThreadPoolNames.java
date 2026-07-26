package com.basic.core.threadpool.support;

import java.util.regex.Pattern;

/**
 * 集中约束业务虚拟线程池的业务标识、Bean 名称与线程名称前缀，避免各调用方产生不一致的命名。
 */
public final class BusinessVirtualThreadPoolNames {

    private static final int MAX_NAME_LENGTH = 32;
    private static final Pattern BUSINESS_NAME =
            Pattern.compile("^[a-z][a-z0-9]*(?:-[a-z0-9]+)*$");
    private static final String BEAN_NAME_SUFFIX = "VirtualTaskExecutor";
    private static final String THREAD_NAME_PREFIX = "basic-vt-";

    private BusinessVirtualThreadPoolNames() {
    }

    public static void validate(String businessName) {
        if (businessName == null
                || businessName.length() > MAX_NAME_LENGTH
                || !BUSINESS_NAME.matcher(businessName).matches()) {
            throw new IllegalArgumentException(
                    "业务标识必须以小写字母开头，仅包含小写字母、数字和单个短横线分段，"
                            + "且长度不能超过 32 个字符");
        }
    }

    public static String beanName(String businessName) {
        validate(businessName);
        String[] parts = businessName.split("-");
        StringBuilder name = new StringBuilder(parts[0]);
        for (int index = 1; index < parts.length; index++) {
            name.append(Character.toUpperCase(parts[index].charAt(0)))
                    .append(parts[index], 1, parts[index].length());
        }
        return name.append(BEAN_NAME_SUFFIX).toString();
    }

    public static String threadNamePrefix(String businessName) {
        validate(businessName);
        return THREAD_NAME_PREFIX + businessName + "-";
    }
}
