package com.basic.api.controller.sys;

import com.basic.api.dto.sysDept.DeptAddDTO;
import com.basic.api.dto.sysDept.DeptQueryDTO;
import com.basic.api.dto.sysDept.DeptUpdateDTO;
import com.basic.api.vo.sysDept.DeptTreeVO;
import com.basic.api.vo.sysDept.DeptVO;
import com.basic.common.result.PageResult;
import com.basic.common.result.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理API接口
 *
 * @author Gas
 */
public interface SysDeptApi {

    /**
     * 新增部门
     *
     * @param dto 部门新增DTO
     * @return 操作结果
     */
    @PostMapping
    Result<?> addDept(@Valid @RequestBody DeptAddDTO dto);

    /**
     * 更新部门
     *
     * @param dto 部门更新DTO
     * @return 操作结果
     */
    @PutMapping
    Result<?> updateDept(@Valid @RequestBody DeptUpdateDTO dto);

    /**
     * 删除部门
     *
     * @param id 部门ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    Result<?> deleteDept(@PathVariable Long id);

    /**
     * 获取部门详情
     *
     * @param id 部门ID
     * @return 部门详情
     */
    @GetMapping("/{id}")
    Result<DeptVO> getDeptById(@PathVariable Long id);

    /**
     * 分页查询部门列表
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    @GetMapping("/list")
    Result<PageResult<DeptVO>> getDeptList(DeptQueryDTO dto);

    /**
     * 获取部门树
     *
     * @return 部门树
     */
    @GetMapping("/tree")
    Result<List<DeptTreeVO>> getDeptTree();

    /**
     * 获取所有部门列表（下拉选择用）
     *
     * @return 部门列表
     */
    @GetMapping("/all")
    Result<List<DeptVO>> getAllDepts();
}
