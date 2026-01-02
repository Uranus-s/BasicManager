package com.basic.core.mybatis.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.*;
import com.basic.core.mybatis.handler.CommonMetaObjectHandler;
import com.basic.core.mybatis.interceptor.DataScopeInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * MyBatis-Plus 配置类
 * 配置 MyBatis-Plus 的拦截器，包括分页、乐观锁和防全表更新/删除功能
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 创建 MyBatis-Plus 拦截器
     * 配置分页拦截器、乐观锁拦截器和防全表攻击拦截器
     *
     * @return MybatisPlusInterceptor 配置好的拦截器实例
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 分页
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));

        // 乐观锁
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // 防全表更新/删除
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        // 数据权限
        interceptor.addInnerInterceptor(new DataScopeInterceptor());

        return interceptor;
    }

    /**
     * 通用自动填充处理器
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new CommonMetaObjectHandler();
    }
}
