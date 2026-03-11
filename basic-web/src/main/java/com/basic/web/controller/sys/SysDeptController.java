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

    @Override
    @PostMapping
    public Result<?> addDept(@Valid @RequestBody DeptAddDTO dto) {
        sysDeptService.addDept(dto);
        return Result.success();
    }

    @Override
    @PutMapping
    public Result<?> updateDept(@Valid @RequestBody DeptUpdateDTO dto) {
        sysDeptService.updateDept(dto);
        return Result.success();
    }

    @Override
    @DeleteMapping("/{id}")
    public Result<?> deleteDept(@PathVariable Long id) {
        sysDeptService.deleteDept(id);
        return Result.success();
    }

    @Override
    @GetMapping("/{id}")
    public Result<DeptVO> getDeptById(@PathVariable Long id) {
        return Result.success(sysDeptService.getDeptById(id));
    }

    @Override
    @GetMapping("/list")
    public Result<PageResult<DeptVO>> getDeptList(DeptQueryDTO dto) {
        return Result.success(sysDeptService.getDeptList(dto));
    }

    @Override
    @GetMapping("/tree")
    public Result<List<DeptTreeVO>> getDeptTree() {
        return Result.success(sysDeptService.getDeptTree());
    }

    @Override
    @GetMapping("/all")
    public Result<List<DeptVO>> getAllDepts() {
        return Result.success(sysDeptService.getAllDepts());
    }
}
