package com.basic.sericve.sysDept.impl;

import com.basic.api.dto.sysDept.DeptAddDTO;
import com.basic.api.dto.sysDept.DeptQueryDTO;
import com.basic.api.dto.sysDept.DeptUpdateDTO;
import com.basic.api.vo.sysDept.DeptTreeVO;
import com.basic.api.vo.sysDept.DeptVO;
import com.basic.common.exception.BusinessException;
import com.basic.common.result.PageResult;
import com.basic.common.result.ResultEnum;
import com.basic.dao.sysDept.entity.SysDept;
import com.basic.dao.sysDept.mapper.SysDeptMapper;
import com.basic.sericve.sysDept.service.ISysDeptService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 部门表 服务实现类
 * </p>
 *
 * @author Gas
 */
@Service
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements ISysDeptService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addDept(DeptAddDTO dto) {
        // 创建部门
        SysDept dept = new SysDept();
        BeanUtils.copyProperties(dto, dept);
        if (dept.getSort() == null) {
            dept.setSort(0);
        }
        save(dept);
        return dept.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDept(DeptUpdateDTO dto) {
        SysDept dept = getById(dto.getId());
        if (dept == null) {
            throw new BusinessException(ResultEnum.DATA_NOT_EXIST);
        }

        // 检查是否将部门设置为自己或自己的子部门
        if (dto.getId().equals(dto.getParentId())) {
            throw new BusinessException(ResultEnum.PARAM_ILLEGAL);
        }

        BeanUtils.copyProperties(dto, dept);
        updateById(dept);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDept(Long id) {
        // 检查是否有子部门
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDept::getParentId, id);
        if (count(wrapper) > 0) {
            throw new BusinessException(ResultEnum.STATUS_NOT_ALLOWED);
        }

        SysDept dept = getById(id);
        if (dept == null) {
            throw new BusinessException(ResultEnum.DATA_NOT_EXIST);
        }
        // 逻辑删除
        removeById(id);
    }

    @Override
    public DeptVO getDeptById(Long id) {
        SysDept dept = getById(id);
        if (dept == null) {
            throw new BusinessException(ResultEnum.DATA_NOT_EXIST);
        }

        DeptVO vo = new DeptVO();
        BeanUtils.copyProperties(dept, vo);

        // 获取父部门名称
        if (dept.getParentId() != null && !dept.getParentId().equals(0L)) {
            SysDept parentDept = getById(dept.getParentId());
            if (parentDept != null) {
                vo.setParentName(parentDept.getDeptName());
            }
        }

        return vo;
    }

    @Override
    public PageResult<DeptVO> getDeptList(DeptQueryDTO dto) {
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getDeptName())) {
            wrapper.like(SysDept::getDeptName, dto.getDeptName());
        }
        if (StringUtils.hasText(dto.getLeader())) {
            wrapper.like(SysDept::getLeader, dto.getLeader());
        }
        if (dto.getParentId() != null) {
            wrapper.eq(SysDept::getParentId, dto.getParentId());
        }
        wrapper.orderByAsc(SysDept::getSort).orderByDesc(SysDept::getCreateTime);

        IPage<SysDept> page = page(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);

        List<DeptVO> voList = new ArrayList<>();
        for (SysDept dept : page.getRecords()) {
            DeptVO vo = new DeptVO();
            BeanUtils.copyProperties(dept, vo);

            // 获取父部门名称
            if (dept.getParentId() != null && !dept.getParentId().equals(0L)) {
                SysDept parentDept = getById(dept.getParentId());
                if (parentDept != null) {
                    vo.setParentName(parentDept.getDeptName());
                }
            }

            voList.add(vo);
        }

        return PageResult.of(page.getCurrent(), page.getSize(), page.getTotal(), voList);
    }

    @Override
    public List<DeptTreeVO> getDeptTree() {
        // 查询所有部门
        List<SysDept> allDepts = list(new LambdaQueryWrapper<SysDept>()
                .orderByAsc(SysDept::getSort));

        // 构建部门映射
        Map<Long, SysDept> deptMap = allDepts.stream()
                .collect(Collectors.toMap(SysDept::getId, d -> d));

        // 构建树形结构
        return buildDeptTree(0L, allDepts);
    }

    private List<DeptTreeVO> buildDeptTree(Long parentId, List<SysDept> allDepts) {
        List<DeptTreeVO> tree = new ArrayList<>();
        for (SysDept dept : allDepts) {
            if (dept.getParentId().equals(parentId)) {
                DeptTreeVO vo = new DeptTreeVO();
                BeanUtils.copyProperties(dept, vo);
                vo.setChildren(buildDeptTree(dept.getId(), allDepts));
                tree.add(vo);
            }
        }
        return tree;
    }

    @Override
    public List<DeptVO> getAllDepts() {
        List<SysDept> depts = list(new LambdaQueryWrapper<SysDept>()
                .orderByAsc(SysDept::getSort));

        return depts.stream()
                .map(dept -> {
                    DeptVO vo = new DeptVO();
                    BeanUtils.copyProperties(dept, vo);
                    return vo;
                })
                .collect(Collectors.toList());
    }
}
