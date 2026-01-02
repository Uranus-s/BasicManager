package com.basic.core.mybatis.base;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BaseMapperPlus<T> extends BaseMapper<T> {
    /**
     * 根据单个 ID 执行逻辑删除
     */
    default int deleteByIdLogical(Long id) {
        if (id == null) {
            return 0;
        }
        UpdateWrapper<T> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", id).set("deleted", 1);
        return this.update(null, wrapper);
    }

    /**
     * 根据 ID 列表批量逻辑删除
     */
    default int deleteBatchIdsLogical(@Param("ids") List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        UpdateWrapper<T> wrapper = new UpdateWrapper<>();
        wrapper.in("id", ids).set("deleted", 1);
        return this.update(null, wrapper);
    }
}
