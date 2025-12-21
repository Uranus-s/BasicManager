package com.basic.common.utils;

import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 对象转换工具类
 *
 * @author bugpool
 */
public class BeanConvertUtils extends BeanUtils {

    private BeanConvertUtils() {
        // 工具类禁止实例化
    }

    /**
     * 单对象转换（无回调）
     */
    public static <S, T> T convertTo(S source, Supplier<T> targetSupplier) {
        return convertTo(source, targetSupplier, null);
    }

    /**
     * 单对象转换
     *
     * @param source         源对象
     * @param targetSupplier 目标对象供应方
     * @param callBack       回调方法
     * @param <S>            源对象类型
     * @param <T>            目标对象类型
     * @return 目标对象
     */
    public static <S, T> T convertTo(S source, Supplier<T> targetSupplier, ConvertCallBack<S, T> callBack) {
        if (source == null || targetSupplier == null) {
            return null;
        }

        T target = targetSupplier.get();
        copyProperties(source, target);

        if (callBack != null) {
            callBack.callBack(source, target);
        }

        return target;
    }

    /**
     * List 对象转换（无回调）
     */
    public static <S, T> List<T> convertListTo(List<S> sources, Supplier<T> targetSupplier) {
        return convertListTo(sources, targetSupplier, null);
    }

    /**
     * List 对象转换
     *
     * @param sources        源对象 List
     * @param targetSupplier 目标对象供应方
     * @param callBack       回调方法
     * @param <S>            源对象类型
     * @param <T>            目标对象类型
     * @return 目标对象 List
     */
    public static <S, T> List<T> convertListTo(List<S> sources, Supplier<T> targetSupplier, ConvertCallBack<S, T> callBack) {
        if (sources == null || targetSupplier == null) {
            return null;
        }

        List<T> list = new ArrayList<>(sources.size());
        for (S source : sources) {
            T target = targetSupplier.get();
            copyProperties(source, target);

            if (callBack != null) {
                callBack.callBack(source, target);
            }

            list.add(target);
        }
        return list;
    }

    /**
     * 转换回调接口
     *
     * @param <S> 源对象类型
     * @param <T> 目标对象类型
     */
    @FunctionalInterface
    public interface ConvertCallBack<S, T> {
        void callBack(S source, T target);
    }
}
