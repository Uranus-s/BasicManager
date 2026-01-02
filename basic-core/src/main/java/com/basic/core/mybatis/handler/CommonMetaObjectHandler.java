package com.basic.core.mybatis.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 通用自动填充处理器
 * 负责 insert/update 自动填充 createTime/updateTime/createBy/updateBy
 * 不依赖 @Component，由 MybatisPlusConfig 注册
 */
public class CommonMetaObjectHandler implements MetaObjectHandler {

    // 模拟当前用户获取逻辑，实际可接入 SecurityContext 或自定义线程变量
    private Long getCurrentUserId() {
        // TODO: 替换为真实用户逻辑
        return 0L;
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "createBy", Long.class, getCurrentUserId());
        this.strictInsertFill(metaObject, "updateBy", Long.class, getCurrentUserId());
        this.strictInsertFill(metaObject, "deleted", Integer.class, 0); // 默认未删除
        this.strictInsertFill(metaObject, "version", Integer.class, 1); // 默认 version=1
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updateBy", Long.class, getCurrentUserId());
    }
}
