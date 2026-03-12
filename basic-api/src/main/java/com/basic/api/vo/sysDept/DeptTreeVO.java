package com.basic.api.vo.sysDept;

import lombok.Data;

import java.util.List;

/**
 * 部门树VO
 *
 * @author Gas
 */
@Data
public class DeptTreeVO {

    /**
     * 部门ID
     */
    private Long id;

    /**
     * 父部门ID
     */
    private Long parentId;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 负责人
     */
    private String leader;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 子部门列表
     */
    private List<DeptTreeVO> children;
}
