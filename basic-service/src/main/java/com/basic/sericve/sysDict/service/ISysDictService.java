package com.basic.sericve.sysDict.service;

import com.basic.api.dto.sysDict.DictAddDTO;
import com.basic.api.dto.sysDict.DictQueryDTO;
import com.basic.api.dto.sysDict.DictUpdateDTO;
import com.basic.api.vo.sysDict.DictVO;
import com.basic.common.result.PageResult;
import com.basic.dao.sysDict.entity.SysDict;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 系统字典表 服务类
 * </p>
 *
 * @author Gas
 */
public interface ISysDictService extends IService<SysDict> {

    /**
     * 新增字典
     *
     * @param dto 字典新增DTO
     * @return 字典ID
     */
    Long addDict(DictAddDTO dto);

    /**
     * 更新字典
     *
     * @param dto 字典更新DTO
     */
    void updateDict(DictUpdateDTO dto);

    /**
     * 删除字典
     *
     * @param id 字典ID
     */
    void deleteDict(Long id);

    /**
     * 获取字典详情
     *
     * @param id 字典ID
     * @return 字典详情
     */
    DictVO getDictById(Long id);

    /**
     * 分页查询字典列表
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    PageResult<DictVO> getDictList(DictQueryDTO dto);

    /**
     * 获取所有字典列表
     *
     * @return 字典列表
     */
    List<DictVO> getAllDicts();

    /**
     * 根据字典编码获取字典
     *
     * @param dictCode 字典编码
     * @return 字典
     */
    SysDict getByDictCode(String dictCode);
}
