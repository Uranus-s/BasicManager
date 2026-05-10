package com.basic.core.mybatis.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.basic.core.security.model.LoginUser;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;

/**
 * 通用自动填充处理器
 * 负责 insert/update 自动填充 createTime/updateTime/createBy/updateBy
 * 不依赖 @Component，由 MybatisPlusConfig 注册
 */
public class CommonMetaObjectHandler implements MetaObjectHandler {

    /**
     * 获取当前登录用户ID。无认证上下文时返回系统用户ID。
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return 0L;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof LoginUser loginUser && loginUser.getUserId() != null) {
            return loginUser.getUserId();
        }

        return 0L;
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "createBy", Long.class, getCurrentUserId());
        this.strictInsertFill(metaObject, "updateBy", Long.class, getCurrentUserId());
        // 默认未删除
        this.strictInsertFill(metaObject, "deleted", Integer.class, 0);
        // 默认 version=1
        this.strictInsertFill(metaObject, "version", Integer.class, 1);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updateBy", Long.class, getCurrentUserId());
    }
}
