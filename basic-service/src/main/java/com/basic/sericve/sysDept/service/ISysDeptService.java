package com.basic.sericve.sysDept.service;

import com.basic.api.dto.sysDept.DeptAddDTO;
import com.basic.api.dto.sysDept.DeptQueryDTO;
import com.basic.api.dto.sysDept.DeptUpdateDTO;
import com.basic.api.vo.sysDept.DeptTreeVO;
import com.basic.api.vo.sysDept.DeptVO;
import com.basic.api.vo.sysUser.UserListVO;
import com.basic.common.result.PageResult;
import com.basic.dao.sysDept.entity.SysDept;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 部门表 服务类
 * </p>
 *
 * @author Gas
 */
public interface ISysDeptService extends IService<SysDept> {

    /**
     * 新增部门
     *
     * @param dto 部门新增DTO
     * @return 部门ID
     */
    Long addDept(DeptAddDTO dto);

    /**
     * 更新部门
     *
     * @param dto 部门更新DTO
     */
    void updateDept(DeptUpdateDTO dto);

    /**
     * 删除部门
     *
     * @param id 部门ID
     */
    void deleteDept(Long id);

    /**
     * 获取部门详情
     *
     * @param id 部门ID
     * @return 部门详情
     */
    DeptVO getDeptById(Long id);

    /**
     * 分页查询部门列表
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    PageResult<DeptVO> getDeptList(DeptQueryDTO dto);

    /**
     * 获取部门树
     *
     * @return 部门树
     */
    List<DeptTreeVO> getDeptTree();

    /**
     * 获取所有部门列表
     *
     * @return 部门列表
     */
    List<DeptVO> getAllDepts();

    /**
     * 获取部门关联的用户列表
     *
     * @param deptId 部门ID
     * @return 用户列表
     */
    List<UserListVO> getUsersByDeptId(Long deptId);

    /**
     * 给部门新增用户
     *
     * @param deptId  部门ID
     * @param userIds 用户ID列表
     */
    void addUsersToDept(Long deptId, List<Long> userIds);

    /**
     * 删除部门下的用户
     *
     * @param deptId  部门ID
     * @param userIds 用户ID列表
     */
    void removeUsersFromDept(Long deptId, List<Long> userIds);
}
