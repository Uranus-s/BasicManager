package com.basic.web.controller.sys;

import com.basic.api.controller.sys.SysDeptApi;
import com.basic.api.dto.sysDept.DeptAddDTO;
import com.basic.api.dto.sysDept.DeptQueryDTO;
import com.basic.api.dto.sysDept.DeptUpdateDTO;
import com.basic.api.vo.sysDept.DeptTreeVO;
import com.basic.api.vo.sysDept.DeptVO;
import com.basic.common.result.PageResult;
import com.basic.common.result.Result;
import com.basic.sericve.sysDept.service.ISysDeptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理 Controller
 *
 * @author Gas
 */
@RestController
@RequestMapping("/system/dept")
@RequiredArgsConstructor
public class SysDeptController implements SysDeptApi {

    private final ISysDeptService sysDeptService;

    /**
     * 新增部门
     *
     * @param dto 部门信息
     * @return 操作结果
     */
    @Override
    @PostMapping
    public Result<?> addDept(@Valid @RequestBody DeptAddDTO dto) {
        sysDeptService.addDept(dto);
        return Result.success();
    }

    /**
     * 更新部门信息
     *
     * @param dto 部门信息
     * @return 操作结果
     */
    @Override
    @PutMapping
    public Result<?> updateDept(@Valid @RequestBody DeptUpdateDTO dto) {
        sysDeptService.updateDept(dto);
        return Result.success();
    }

    /**
     * 删除部门
     *
     * @param id 部门ID
     * @return 操作结果
     */
    @Override
    @DeleteMapping("/{id}")
    public Result<?> deleteDept(@PathVariable Long id) {
        sysDeptService.deleteDept(id);
        return Result.success();
    }

    /**
     * 根据ID获取部门详情
     *
     * @param id 部门ID
     * @return 部门信息
     */
    @Override
    @GetMapping("/{id}")
    public Result<DeptVO> getDeptById(@PathVariable Long id) {
        return Result.success(sysDeptService.getDeptById(id));
    }

    /**
     * 获取部门列表（分页）
     *
     * @param dto 查询条件
     * @return 部门列表（分页）
     */
    @Override
    @GetMapping("/list")
    public Result<PageResult<DeptVO>> getDeptList(DeptQueryDTO dto) {
        return Result.success(sysDeptService.getDeptList(dto));
    }

    /**
     * 获取部门树形结构
     *
     * @return 部门树形列表
     */
    @Override
    @GetMapping("/tree")
    public Result<List<DeptTreeVO>> getDeptTree() {
        return Result.success(sysDeptService.getDeptTree());
    }

    /**
     * 获取所有部门列表（不分页）
     *
     * @return 所有部门列表
     */
    @Override
    @GetMapping("/all")
    public Result<List<DeptVO>> getAllDepts() {
        return Result.success(sysDeptService.getAllDepts());
    }
}
